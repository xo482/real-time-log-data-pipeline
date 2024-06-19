package kafka.kafka.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.kafka.admin.domain.Filter;
import kafka.kafka.admin.domain.LogicalOperator;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.domain.log.FailureLog;
import kafka.kafka.admin.domain.log.SuccessLog;
import kafka.kafka.admin.repository.FailureLogRepository;
import kafka.kafka.admin.repository.ScenarioRepository;
import kafka.kafka.admin.repository.SuccessLogRepository;
import kafka.kafka.shoppingmall.domain.Gender;
import kafka.kafka.shoppingmall.domain.Member;
import kafka.kafka.shoppingmall.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FilterService {


    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;
    private final MemberRepository memberRepository;
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Member member;
    private JsonNode jsonNode;

    @KafkaListener(topicPattern = "filter_topic_.*", groupId = "my_group")
    @Transactional
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        Long scenarioId = extractScenarioIdFromTopic(topic);
        Scenario scenario = scenarioRepository.findById(scenarioId).orElse(null);

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
            Long memberId = 0L;
            if (jsonNode.has("memberId")) {
                memberId = jsonNode.get("memberId").asLong();
            }
            if (memberId != 0L) {
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
                System.out.println("scenario_filter_"+scenarioId+" : 적합하므로 성공 테이블에 저장");
                successLogRepository.save(new SuccessLog(scenarioId, jsonNode.toString()));
            } else {
                System.out.println("scenario_filter_"+scenarioId+" : 적합하지 않으므로 실패 테이블에 저장");
                failureLogRepository.save(new FailureLog(scenarioId, jsonNode.toString()));
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

        // null이면 비회원으로 남긴 로그임
        if (member == null) {
            return false;
        }

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

        // null이면 비회원으로 남긴 로그임
        if (member == null) {
            return false;
        }

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

    private Long extractScenarioIdFromTopic(String topic) {
        Pattern pattern = Pattern.compile("filter_topic_(\\d+)");
        Matcher matcher = pattern.matcher(topic);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }
}