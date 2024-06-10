package kafka.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import kafka.kafka.admin.domain.Filter;
import kafka.kafka.admin.domain.LogicalOperator;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.domain.log.FailureLog;
import kafka.kafka.admin.domain.log.SuccessLog;
import kafka.kafka.admin.repository.FailureLogRepository;
import kafka.kafka.admin.repository.ScenarioRepository;
import kafka.kafka.admin.repository.SuccessLogRepository;
import kafka.kafka.domain.Gender;
import kafka.kafka.domain.Member;
import kafka.kafka.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterConsumerService {

    private static final String TOPIC_NAME = "filter_topic";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;
    private final MemberRepository memberRepository;
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Member member;
    private JsonNode jsonNode;
    private ScriptEngine engine;

    @PostConstruct
    public void init() {
        // ScriptEngine 초기화
        ScriptEngineManager manager = new ScriptEngineManager();
        this.engine = manager.getEngineByName("nashorn");
        if (this.engine == null) {
            throw new IllegalStateException("Nashorn script engine not found. Make sure to add it as a dependency.");
        }
    }


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
            jsonNode = objectMapper.readTree(message);

            // 고객 가져오기
            Long memberId = jsonNode.get("memberId").asLong();
            if (memberId != 0) {
                member = memberRepository.findById(memberId).get();
            }

            // AND 연산
            boolean flag;
            if (scenario.getLogicalOperator() == LogicalOperator.AND) {
                flag = true;
                for (Filter filter : filters) {
                    if (!compare(filter.getLeft(), filter.getOperator(), filter.getRight())) {
                        flag = false;
                        break;
                    }
                }
            }
            // OR 연산
            else {
                flag = false;
                for (Filter filter : filters) {
                    if (compare(filter.getLeft(), filter.getOperator(), filter.getRight())) {
                        flag = true;
                        break;
                    }
                }
            }


            //== 저장 ==//
            if (flag) {
                System.out.println("적합하므로 성공 테이블에 저장");
                successLogRepository.save(new SuccessLog(jsonNode.toString()));
            } else {
                System.out.println("적합하지 않으므로 실패 테이블에 저장");
                failureLogRepository.save(new FailureLog(jsonNode.toString()));
            }

        } catch (Exception e) {
            System.err.println("Failed to filtering message: " + e.getMessage());
        }
    }

    private boolean compare(String left, String operator, String right) {
        if (left.equals("age")) return compareAge(operator, right);
        if (left.equals("gender")) return compareGender(operator, right);
        if (left.equals("h") || left.equals("m") || left.equals("s"))
            return compareInt(left, operator, right);
        else return compareStr(left, operator, right);
    }

    private boolean compareStr(String left, String operator, String right) {
        String str = jsonNode.get(left).asText();
        switch (operator) {
            case "==":
                return str.equals(right);
            case "!=":
                return !str.equals(right);
            case "contains":
                return str.contains(right);
            case "!contains":
                return !str.contains(right);
            default:
                throw new IllegalArgumentException("Invalid operator for string comparison: " + operator);
        }
    }
    private boolean compareInt(String left, String operator, String right) {
        int hour = jsonNode.get(left).asInt();
        int comparison = Integer.parseInt(right);
        switch (operator) {
            case ">":
                return hour > comparison;
            case "<":
                return hour < comparison;
            case "==":
                return hour == comparison;
            case "!=":
                return hour != comparison;
            case ">=":
                return hour >= comparison;
            case "<=":
                return hour <= comparison;
            default:
                throw new IllegalArgumentException("Invalid operator for integer comparison: " + operator);
        }
    }
    private boolean compareGender(String operator, String right) {
        Gender gender = member.getGender();
        String genderStr = gender.toString();
        switch (operator) {
            case "==":
                return genderStr.equals(right);
            case "!=":
                return !genderStr.equals(right);
            default:
                throw new IllegalArgumentException("Invalid operator for gender comparison: " + operator);
        }
    }
    private boolean compareAge(String operator, String right) {
        int age = Integer.parseInt(member.getAge());
        int comparisonAge = Integer.parseInt(right);
        switch (operator) {
            case ">":
                return age > comparisonAge;
            case "<":
                return age < comparisonAge;
            case "==":
                return age == comparisonAge;
            case "!=":
                return age != comparisonAge;
            case ">=":
                return age >= comparisonAge;
            case "<=":
                return age <= comparisonAge;
            default:
                throw new IllegalArgumentException("Invalid operator for age comparison: " + operator);
        }
    }
}