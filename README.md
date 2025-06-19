# 로그데이터 수집을 통한 고객 행동 데이터 활용 프로젝트

## 프로젝트 소개

웹 애플리케이션 사용자들의 행동 데이터를 실시간으로 수집하고, Kafka 기반 스트리밍 처리 과정을 거쳐 데이터를 추출, 변환, 적재하여, 그 결과를 시각화하는 엔드-투-엔드 로그 파이프라인 시스템입니다.

단순한 로그 수집을 넘어, 사용자 행동의 흐름을 실시간으로 파악하고, 사용자가 정의한 시나리오 기반의 실시간 필터링 및 포맷 변환을 수행한 후 비즈니스 의사결정에 유의미한 데이터로 가공해냅니다. 또한 Kafka, Spark, Redis 기반의 비동기 아키텍처로 설계되어 대규모 트래픽 처리에도 유연하게 대응할 수 있습니다.

![시스템 아키텍처](https://github.com/xo482/real-time-log-data-pipeline/blob/dev/images/overview.png)

<br></br>

## 전송되는 요청 예시 및 파라미터

사용자가 웹 애플리케이션 내에서 의도한 동작을 했을 경우 고객의 행동 데이터는 쿼리 파라미터 형태로 발생되며, 접속 시점, 브라우저 정보, 화면 해상도, 클릭 이벤트 등 다양한 사용자 행동 데이터가 포함됩니다. 이 요청은 NGINX를 통해 수집되어 `access.log`에 기록되며, 이후 Fluentd가 이를 Kafka로 전달하는 파이프라인의 출발점이 됩니다. 예시는 다음과 같습니다:

```http
http://[NGINX_URL]/{scenario_id}?
e_c=buttonClick
&e_a=Clicked
&e_n=\uace0\uba74\ud560\ub2f9\ud558\uae30
&url=http://[Tracked_Page]/product_detail/product_name
&urlref=http://[Tracked_Page]/
&_idn=0
&cookie=1
&res=1920x1080
&memberId=123456
```

| 파라미터     | 설명                        |
| -------- | ------------------------- |
| e\_c     | 이벤트 카테고리 (예: buttonClick) |
| e\_a     | 이벤트 액션 (예: Clicked)       |
| e\_n     | 이벤트 이름 (예: 구매)            |
| url      | 현재 페이지 URL                |
| urlref   | 이전 페이지 URL (Referrer)     |
| \_idn    | 신규 방문 여부 (1이면 신규)         |
| cookie   | 쿠키 사용 여부                  |
| res      | 화면 해상도 (예: 1920x1080)     |
| memberId | 사용자 ID                    |

<br></br>
## 시나리오 기반 동적 처리 구조

사용자는 직접 시나리오를 생성하여 다음과 같은 설정을 할 수 있습니다

* 저장하고자 하는 파라미터 (e.g. e_c, e_a, url 등)
* 필터 조건 (예: age > 20, gender == MALE, h >= 9 등)
* 논리 연산 방식 (AND, OR)

이 시나리오는 DB에 저장되며, 이후 Kafka Consumer가 해당 설정을 기준으로 포맷 변환 및 필터링을 실시간으로 수행합니다.
<br></br>
#### 시나리오 정의 예시 (JSON)

```json
{
  "scenario_id": 5,
  "title": "9시 이후 구매 버튼을 클릭한 성인 남성 사용자 필터링",
  "manager": "admin_user",
  "log_format": {
    "e_c": 0,
    "e_a": 0,
    "memberId": 1,
    "h": 1,
    "res": 0,
    ...
  },
  "filters": [
    { "left": "age", "operator": ">", "right": "20" },
    { "left": "gender", "operator": "==", "right": "MALE" },
    { "left": "h", "operator": ">=", "right": "9" }
  ],
  "logical_operator": "AND"
}
```

<br></br>

## 시스템 구성 요소 및 데이터 흐름

### Nginx → Fluentd → Kafka 로그 전송

Fluentd는 아래와 같은 설정으로 Nginx의 access.log 파일을 tail하며 Kafka로 전송합니다.

```xml
<source>
  @type tail
  path /var/log/nginx/access.log
  pos_file /fluentd/status/nginx-access.pos
  tag nginx.access
  @label @LOGGING
  <parse>
    @type nginx
  </parse>
</source>
```
<br></br>
### Kafka → Spring 서비스: 시나리오 기반 포맷 처리 및 필터링

* Kafka에 저장된 로그는 `ScenarioProcessingService`가 수신합니다.
* 로그 내부의 `scenario_id`를 기준으로 시나리오를 조회합니다.
* 시나리오에는 출력 필드와 필터 조건이 포함되며, 이를 바탕으로 success 여부를 판별합니다.
* 포맷 필드가 0이면 해당 항목은 제거됩니다.
* 필터 조건을 모두 만족하면 success: 1, 그렇지 않으면 success: 0 으로 설정됩니다.
* 최종 JSON에 `success`, `scenario_id`, `date` 필드 포함 후 다음 토픽으로 전송됩니다.
  
<br></br>
### Spark Streaming 처리

* HDFS 저장

```python
# -------------------- HDFS 저장 --------------------
query_hadoop = flattened_df.writeStream \
    .format("parquet") \
    .option("path", f"hdfs://[HDFS_IP]:9000/kafka_sink_output") \
    .option("checkpointLocation", f"hdfs://[HDFS_IP]:9000/spark-checkpoint") \
    .outputMode("append") \
    .start()
```

<br></br>
* Kafka로부터 실시간 로그 수신
* success 여부를 기반으로 성공률 통계 집계
* Redis 저장 병렬 수행

```python
# -------------------- Redis 저장 함수 --------------------
def write_to_redis(batch_df, epoch_id):
    scenario_stats = batch_df.groupBy("scenario_id").agg(
        spark_sum("success").alias("success_count"),
        count("*").alias("total_count"))
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    r = redis.StrictRedis(host="[REDIS_IP]", port=6379, db=0)

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
    .option("checkpointLocation", f"hdfs://{HDFS_IP}:9000/checkpoint_success_ratio_pt") \
    .start()

)
```
<br></br>
* Redis 저장 예시

```json
{
  "success_ratio": 0.85,
  "total_count": 100,
  "success_count": 85,
  "updated_at": "2025-06-18 12:50:10"
}
```

<br></br>
## 실시간 시각화 시스템

* 🔧 관리자 페이지: Redis에 저장된 scenario 별 success 통계를 5초 단위로 UI에서 ECharts로 시각화하여 관리자가 설정한 시나리오에 대한 데이터가 얼마나 수집되고 있는지와 정상적으로 수집되고 있는지 확인

  <img src="https://github.com/xo482/real-time-log-data-pipeline/blob/dev/images/graph.png" width="600">

* 📊 Grafana: Prometheus를 활용해 Kafka, Spark, Redis의 성능 및 지연 시간 등 시스템 전반을 실시간으로 모니터링하고 카프카 Lag 감시 및 TPS 측정

  <img src="https://github.com/xo482/real-time-log-data-pipeline/blob/dev/images/monitoring.png" width="600">

<br></br>
##기술 스택

| 범주             | 사용 기술 및 도구                                             | 설명                                     |
| -------------- | ------------------------------------------------------ | -------------------------------------- |
| **데이터 수집**     | **NGINX(access.log)**, **Matomo Tag Manager**, | 웹 사용자 행동 데이터를 추적하여 로그로 저장              |
| **로그 수집/전송**   | **Fluentd**                                            | NGINX 로그를 수집하여 Kafka로 전송 (tail 기반)     |
| **메시지 브로커**    | **Apache Kafka** (3-broker, KRaft mode)                | 로그 메시지를 비동기 스트리밍 방식으로 전달               |
| **실시간 처리 (1)** | **Spring Kafka (Java)**                                | Kafka 메시지를 소비하여 시나리오 기반 필터링 및 포맷 변환 수행 |
| **실시간 처리 (2)** | **Apache Spark Structured Streaming (PySpark)**        | Kafka 메시지를 병렬 처리하여 실시간 통계 집계 및 저장      |
| **데이터 저장소**    | **Redis**                                              | 시나리오 및 사용자 정보 캐싱, 실시간 성공률 저장           |
|                | **HDFS**                                               | 원본 로그 데이터 영구 저장 (Parquet 포맷)           |
|                | **MariaDB**                                            | 사용자(Member), 시나리오(Scenario) 등 기준 정보 저장 |
| **시각화**        | **ECharts**                                            | Redis 기반 실시간 성공률 시각화 (웹 기반)            |
|                | **Grafana + Prometheus**                               | Kafka/Spark/시스템 성능 모니터링용 대시보드 구축       |
| **인프라**        | **AWS EC2**, **Docker Compose**                        | 클라우드 기반 분산 처리 환경 구축    |

