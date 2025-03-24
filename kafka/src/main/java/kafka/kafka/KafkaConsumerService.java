package kafka.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kafka.kafka.admin.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final String TOPIC_NAME = "start_topic";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;


    private ObjectMapper objectMapper = new ObjectMapper();


    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group", concurrency = "3")
    public void listen(String message) {
        // 메시지 출력
//        System.out.println("Hub Received message: " + message);

        try {
            // JSON 객체로 파싱
            JsonNode jsonNode = objectMapper.readTree(message);
            // message 키와 time 키에 대한 필드 추출
            if (jsonNode.has("message") && jsonNode.has("date")) {
                String logMessage = jsonNode.get("message").asText();
                String date = jsonNode.get("date").asText();

                // path 필드에서 pageView 값을 추출
                String pathValue = "";
                if (logMessage.contains("path=")) {
                    pathValue = logMessage.split("path=")[1].split(" ")[0].split("/")[1];
                }

                if(pathValue.equals("event")) {
                    // "params=" 이후의 문자열을 추출
                    String[] parts = logMessage.split("params=");
                    if (parts.length > 1) {
                        String paramsString = parts[1].trim();

                        // params 문자열을 JSON 객체로 파싱
                        JsonNode paramsJson = objectMapper.readTree(paramsString);

                        // uadata 필드가 삭제
                        if (paramsJson.has("uadata")) {
                            ((ObjectNode) paramsJson).remove("uadata");
                        }

                        // time 값을 paramsJson에 추가
                        ((ObjectNode) paramsJson).put("date", date);


//                        List<Long> ids = scenarioRepository.findAllIds();
//                        for (Long id : ids)  {
//                            kafkaTemplate.send("scenario_topic_" + id, paramsJson.toString());
//                        }
                        kafkaTemplate.send("scenario_topic_1", paramsJson.toString());
                    } else {
                        System.out.println("The message does not contain 'params='.");
                    }
                }

            } else {
                System.out.println("내가 원하는 로그가 온 것이 아니다.");
            }
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }
}