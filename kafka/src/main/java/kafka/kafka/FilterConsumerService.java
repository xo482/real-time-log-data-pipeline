package kafka.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.kafka.admin.domain.Filter;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.repository.ScenarioRepository;
import kafka.kafka.domain.Member;
import kafka.kafka.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterConsumerService {

    private static final String TOPIC_NAME = "filter_topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ScenarioRepository scenarioRepository;
    private final MemberRepository memberRepository;

    private ObjectMapper objectMapper = new ObjectMapper();


    @KafkaListener(topics = TOPIC_NAME, groupId = "my_group")
    @Transactional
    public void listen(String message) {
        System.out.println("========================================== filter ==========================================");
        System.out.println("Received message: " + message);

        Scenario scenario = scenarioRepository.findById(1L).orElse(null);

        // 필터링 정보 가져오기
        List<Filter> filters = scenario.getFilters();
        if (filters == null) {
            System.err.println("filters not found");
            return;
        }

        try {
            // 받은 메시지를 JSON으로 파싱
            JsonNode jsonNode = objectMapper.readTree(message);

            // 고객 가져오기
            Member member = memberRepository.findById(jsonNode.get("memberId").asLong()).get();

            boolean flag = false; // or 연산자라고 한다면 플래그 값이 변경되는 걸 반대로 진행해야함
            for (Filter filter : filters) {
                if (filter.getLeft().equals("age")) {
                    int age = Integer.parseInt(member.getAge());
                    System.out.print("age = " + age);

                    if (filter.getOperator().equals(">")) {
                        if (age <= Integer.parseInt(filter.getRight())) {
                            flag = true;
                        }
                    }
                    if (filter.getOperator().equals("<")) {
                        if (age >= Integer.parseInt(filter.getRight())) {
                            flag = true;
                        }
                    }
                    if (filter.getOperator().equals("==")) {
                        if (age != Integer.parseInt(filter.getRight())) {
                            flag = true;
                        }
                    }
                }

                if (filter.getLeft().equals("gender")) {
                    String gender = member.getGender().toString();
                    System.out.println(", gender = " + gender);

                    if (filter.getOperator().equals("==")) {
                        if (!gender.equals(filter.getRight())) {
                            flag = true;
                        }
                    }
                }
            }

            if (!flag) {
                System.out.println("적합하므로 성공 테이블에 저장");
            } else {
                System.out.println("적합하지 않으므로 실패 테이블에 저장");
            }

        } catch (Exception e) {
            System.err.println("Failed to filtering message: " + e.getMessage());
        }
    }
}
