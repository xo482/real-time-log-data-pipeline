package kafka.kafka.scenario;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.kafka.admin.domain.Status;
import kafka.kafka.admin.repository.ScenarioRepository;
import kafka.kafka.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisService redisService;
    private final ScenarioRepository scenarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topicPattern = "scenario_topic_.*", groupId = "scenario_group", concurrency = "5")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws JsonProcessingException {
        Long scenarioId = extractScenarioIdFromTopic(topic);
        String key = "scenario:" + scenarioId + ":status";
        String statusString = redisService.getValue(key);

        Status status;
        if (statusString != null) status = objectMapper.readValue(statusString, Status.class);
        else {
            status = scenarioRepository.findById(scenarioId).orElse(null).getStatus();
            redisService.setValueWithTTL(key, objectMapper.writeValueAsString(status), 600);
        }


        if (status == Status.PAUSE) return;
        kafkaTemplate.send("format_topic_" + scenarioId, message);
    }

    private Long extractScenarioIdFromTopic(String topic) {
        Pattern pattern = Pattern.compile("scenario_topic_(\\d+)");
        Matcher matcher = pattern.matcher(topic);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}