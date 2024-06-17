package kafka.kafka.scenario;


import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.domain.Status;
import kafka.kafka.admin.repository.ScenarioRepository;
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
    private final ScenarioRepository scenarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topicPattern = "scenario_topic_.*", groupId = "my_group")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        // 메시지 출력

        Long scenarioId = extractScenarioIdFromTopic(topic);
        Scenario scenario = scenarioRepository.findById(scenarioId).orElse(null);
        if (scenario == null) {
            System.out.println("|  scenario_" + scenarioId + "  |              |");
            return;
        }
        if (scenario.getStatus() == Status.PAUSE){
            System.out.println("|  scenario_" + scenarioId + "  |    paused    |");
            return;
        }

        System.out.println("|  scenario_" + scenarioId + "  |     run      |");
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
