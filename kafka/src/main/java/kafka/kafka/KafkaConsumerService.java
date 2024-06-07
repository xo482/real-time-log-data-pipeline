package kafka.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.domain.Status;
import kafka.kafka.admin.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final String TOPIC_NAME = "start_topic";
    private static final String NEXT_TOPIC = "format_topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ScenarioRepository scenarioRepository;

    private ObjectMapper objectMapper = new ObjectMapper();


    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
    public void listen(String message) {
        // 메시지 출력
        System.out.println("========================================== start ==========================================");
        System.out.println("Received message: " + message);


        Scenario scenario = scenarioRepository.findById(1L).orElse(null);
        if (scenario == null) {
            System.err.println("scenario not found");
            return;
        }
        if (scenario.getStatus() == Status.PAUSE){
            System.err.println("Scenario paused");
            return;
        }


        try {
            // JSON 객체로 파싱
            JsonNode jsonNode = objectMapper.readTree(message);
            // message 키와 time 키에 대한 필드 추출
            if (jsonNode.has("message") && jsonNode.has("time")) {
                String logMessage = jsonNode.get("message").asText();
                String time = jsonNode.get("time").asText();

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
                    ((ObjectNode) paramsJson).put("time", time);

                    System.out.println("After parsing: " + paramsJson);

                    // 다음 토픽으로 전송
                    kafkaTemplate.send(NEXT_TOPIC, paramsJson.toString());
                } else {
                    System.out.println("The message does not contain 'params='.");
                }

            } else {
                System.out.println("The message does not contain the 'message' or 'time' field.");
            }
        } catch (Exception e) {
            System.err.println("Failed to parse message: " + e.getMessage());
        }
    }
}



//@Service
//public class KafkaConsumerService {
//
//    private static final String TOPIC_NAME = "start_topic";
//    private static final String NEXT_TOPIC = "format_topic";
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    private ObjectMapper objectMapper = new ObjectMapper();
//
//    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
//    public void listen(String message) {
//        // 메시지 출력
//        System.out.println("========================================== start ==========================================");
//        System.out.println("Received message: " + message);
//
//        try {
//            // JSON 객체로 파싱
//            JsonNode jsonNode = objectMapper.readTree(message);
//            // message 키에 대한 필드 추출
//            if (jsonNode.has("message")) {
//                String logMessage = jsonNode.get("message").asText();
//                String time = jsonNode.get("time").asText();
//
//                // "params=" 이후의 문자열을 추출
//                String[] parts = logMessage.split("params=");
//                if (parts.length > 1) {
//                    String paramsString = parts[1].trim();
//
//                    // params 문자열을 JSON 객체로 파싱
//                    JsonNode paramsJson = objectMapper.readTree(paramsString);
//
//                    // 내부 JSON 문자열을 파싱
//                    if (paramsJson.has("uadata")) {
//                        JsonNode uadataJson = paramsJson.get("uadata");
//                        ((ObjectNode) paramsJson).set("uadata", uadataJson);
//                    }
//
//                    ((ObjectNode) paramsJson).put("time", time);
//                    System.out.println("After parsing: " + paramsJson);
//
//                    // 다음 토픽으로 전송
//                    kafkaTemplate.send(NEXT_TOPIC, paramsJson.toString());
//                } else {
//                    System.out.println("The message does not contain 'params='.");
//                }
//
//            } else {
//                System.out.println("The message does not contain the 'message' field.");
//            }
//        } catch (Exception e) {
//            System.err.println("Failed to parse message: " + e.getMessage());
//        }
//    }
//}
