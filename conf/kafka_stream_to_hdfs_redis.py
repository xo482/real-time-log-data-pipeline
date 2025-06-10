from pyspark.sql import SparkSession
from pyspark.sql.functions import col, map_filter, count
from pyspark.sql.types import MapType, StringType
from pyspark.sql.functions import sum as spark_sum
from datetime import datetime
import redis
import json

# -------------------- Spark Session --------------------
spark = SparkSession.builder \
    .appName("KafkaToHDFSAndRedis") \
    .config("spark.executor.instances", "3") \
    .getOrCreate()

# -------------------- Kafka Stream --------------------
df = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "172.31.34.238:9092,172.31.44.175:9092,172.31.33.93:9092") \
    .option("subscribe", "spark-ingest-topic") \
    .option("startingOffsets", "latest") \
    .option("failOnDataLoss", "false") \
    .load() \
    .repartition(3)  # Spark 파티션 수 3개로 맞추기

json_df = df.selectExpr("CAST(value AS STRING) as json_value") \
    .selectExpr("from_json(json_value, 'map<string,string>') as json_map")

flattened_df = json_df.select(
    col("json_map.scenario_id").cast("long").alias("scenario_id"),
    col("json_map.success").cast("int").alias("success"),
    map_filter("json_map", lambda k, v: (k != "id") & (k != "success")).alias("data")
)

# -------------------- Write to HDFS --------------------
query_hadoop = flattened_df.writeStream \
    .format("parquet") \
    .option("path", "hdfs://172.31.38.93:9000/kafka_sink_output") \
    .option("checkpointLocation", "hdfs://172.31.38.93:9000/spark-checkpoint") \
    .outputMode("append") \
    .start()


# -------------------- Redis --------------------
def write_to_redis(batch_df, epoch_id):
    scenario_stats = batch_df.groupBy("scenario_id").agg(
        spark_sum("success").alias("success_count"),
        count("*").alias("total_count"))
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    r = redis.StrictRedis(host="172.31.43.19", port=6379, db=0)

    for row in scenario_stats.collect():
        sid = row["scenario_id"]
        total = row["total_count"]
        success = row["success_count"] or 0
        ratio = round(success / total, 4) if total else 0

        value = json.dumps({
            "success_ratio": ratio,
            "total_count": total,
            "success_count": success,
            "updated_at": now_str
        })

        r.set(f"scenario:{sid}:success_ratio", value)


# -------------------- Redis 저장 쿼리 --------------------
query_redis = flattened_df.select("scenario_id", "success").writeStream \
    .outputMode("append") \
    .trigger(processingTime="5 seconds") \
    .foreachBatch(write_to_redis) \
    .option("checkpointLocation", "hdfs://172.31.38.93:9000/checkpoint_success_ratio_pt") \
    .start()

query_hadoop.awaitTermination()
query_redis.awaitTermination()