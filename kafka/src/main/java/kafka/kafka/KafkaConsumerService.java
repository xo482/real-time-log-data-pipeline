package kafka.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kafka.kafka.admin.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private static final String TOPIC_NAME = "start_topic";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();


    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group", concurrency = "3")
    public void listen(String message) {

        try {
            // JSON 객체로 파싱
            JsonNode jsonNode = objectMapper.readTree(message);

            // 내가 원하는 데이터가 들어온 것이 아니라면 메서드 종료시킴
            if (!jsonNode.has("path")) {
                log.warn("Invalid message received: {}", message);
                return;
            }

            // path 필드에서 쿼리 문자열과 날짜 꺼냄
            String queryString = jsonNode.get("path").asText();
            String date = jsonNode.get("date").asText();

            // 쿼리 문자열에서 필요한 정보만 추출
            queryString = queryString.split("\\?")[1];

            // 쿼리 문자열을 JSON 객체로 변환
            JsonNode queryJson = convertQueryStringToJson(queryString);

            // 불핋요한 필드 삭제 (uadata)
            if (queryJson.has("uadata"))
                ((ObjectNode) queryJson).remove("uadata");

            // date 값을 paramsJson에 추가
            ((ObjectNode) queryJson).put("date", date);

            // scenario_topic_1으로 데이터 전송
            kafkaTemplate.send("scenario_topic_1", queryJson.toString());

        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }

    // 쿼리 문자열을 JSON 형식으로 변환하는 메서드
    private JsonNode convertQueryStringToJson(String queryString) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode queryJson = objectMapper.createObjectNode();

        // 쿼리 문자열을 &로 구분하여 각 파라미터를 분리하고, 이를 JSON 객체로 변환
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                queryJson.put(key, value);
            }
        }

        return queryJson;
    }
}