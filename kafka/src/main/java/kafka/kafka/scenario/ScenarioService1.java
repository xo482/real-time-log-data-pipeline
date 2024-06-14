package kafka.kafka.scenario;

import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.domain.Status;
import kafka.kafka.admin.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScenarioService1 {

    private static final String TOPIC_NAME = "scenario_topic_1";
    private static final String NEXT_TOPIC = "format_topic_1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;

    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
    public void listen(String message) {
        // 메시지 출력

        Long scenario_id = 1L;
        Scenario scenario = scenarioRepository.findById(scenario_id).orElse(null);
        if (scenario == null) {
            System.out.println("|  scenario_1  |              |");
            return;
        }
        if (scenario.getStatus() == Status.PAUSE){
            System.out.println("|  scenario_1  |    paused    |");
            return;
        }

        System.out.println("|  scenario_1  |     run      |");
        kafkaTemplate.send(NEXT_TOPIC, message);
    }
}


