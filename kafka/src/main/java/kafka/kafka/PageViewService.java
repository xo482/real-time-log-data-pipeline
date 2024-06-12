package kafka.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.kafka.admin.domain.NumberOfVisitors;
import kafka.kafka.admin.repository.NumberOfVisitorRepository;
import kafka.kafka.admin.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PageViewService {

    private static final String TOPIC_NAME = "pageView_topic";
    private final NumberOfVisitorRepository numberOfVisitorRepository;

    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
    @Transactional
    public void listen(String message) {
        // 메시지 출력
        System.out.println("========================================== visitor ==========================================");
        System.out.println("Received message: " + message);

        NumberOfVisitors numberOfVisitors = numberOfVisitorRepository.findById(1L).get();
        numberOfVisitors.setCount(numberOfVisitors.getCount()+1);

        System.out.println(numberOfVisitors.getCount());

    }
}
