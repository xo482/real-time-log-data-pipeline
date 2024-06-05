package kafka.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FilterConsumerService {

    private static final String TOPIC_NAME = "filter_topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
    public void listen(String message) {
        System.out.println("========================================== filter ==========================================");
        System.out.println("Received message: " + message);

    }
}
