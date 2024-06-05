package kafka.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

@Service
public class KafkaProducerService {

    private static final String TOPIC_NAME = "start_topic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);

        kafkaTemplate.send(TOPIC_NAME, jsonObject.toString());
    }
}

