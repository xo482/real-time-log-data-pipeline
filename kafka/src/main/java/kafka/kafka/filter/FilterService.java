package kafka.kafka.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import kafka.kafka.service.RedisService;
import kafka.kafka.shoppingmall.domain.Gender;
import kafka.kafka.shoppingmall.domain.Member;
import kafka.kafka.shoppingmall.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.internals.AbstractPartitionAssignor;
import org.springframework.data.domain.Page;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterService {

    private static final String NEXT_TOPIC_PREFIX = "spark_ingest_topic_";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScenarioRepository scenarioRepository;
    private final MemberRepository memberRepository;
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisService redisService;

    private final ThreadLocal<List<SuccessLog>> successBuffer = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<FailureLog>> failureBuffer = ThreadLocal.withInitial(ArrayList::new);
    private static final int BATCH_SIZE = 200;
    private static final long FLUSH_INTERVAL_MS = 5000; // 5초마다 flush
    private final ThreadLocal<Long> lastFlushTime = ThreadLocal.withInitial(System::currentTimeMillis);


    @KafkaListener(topicPattern = "filter_topic_.*", groupId = "filter_group", concurrency = "3")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws JsonProcessingException {

        // 1. JSON 파싱
        JsonNode jsonNode = objectMapper.readTree(message);

        // 2. Redis에서 필터 및 연산자 멤버 가져오기
        Long scenarioId = extractScenarioIdFromTopic(topic);

        Long memberId = null;
        if (jsonNode.has("memberId")) memberId = jsonNode.get("memberId").asLong();

        String key = "scenario:" + scenarioId + ":filer";
        String opKey = "scenario:" + scenarioId + ":operator";
        String memberKey = "member:" + memberId + ":info";

        String filterString, operatorString, memberString=null;
        if (memberId != null) {
            Map<String, String> redisValues = redisService.getValue(Arrays.asList(key, opKey));
            filterString = redisValues.get(key);
            operatorString = redisValues.get(opKey);
        }
        else {
            Map<String, String> redisValues = redisService.getValue(Arrays.asList(key, opKey, memberKey));
            filterString = redisValues.get(key);
            operatorString = redisValues.get(opKey);
            memberString = redisValues.get(memberKey);
        }


        List<Filter> filters;
        LogicalOperator logicalOperator;
        if (filterString != null || operatorString != null) {
            filters = objectMapper.readValue(filterString, new TypeReference<List<Filter>>() {});
            logicalOperator = objectMapper.readValue(operatorString, LogicalOperator.class);
        } else {

            Scenario scenario = scenarioRepository.findById(scenarioId).orElse(null);
            filters = scenario.getFilters();
            logicalOperator = scenario.getLogicalOperator();

            redisService.setValueWithTTL(key, objectMapper.writeValueAsString(filters), 600);
            redisService.setValueWithTTL(opKey, objectMapper.writeValueAsString(logicalOperator), 600);
        }


        Member member = null;
        if (memberString != null) member = objectMapper.readValue(memberString, Member.class);
        else {
            if (memberId != null) {
                member = memberRepository.findById(memberId).orElse(null);
                redisService.setValueWithTTL(memberKey, objectMapper.writeValueAsString(member), 600);
            }
        }

        // 5. 필터링 로직
        boolean flag;
        if (logicalOperator == LogicalOperator.AND) {
            flag = true;
            for (Filter filter : filters) {
                if (!compare(filter.getLeft(), filter.getOperator(), filter.getRight(), member, jsonNode)) {
                    flag = false;
                    break;
                }
            }
        } else {
            flag = false;
            for (Filter filter : filters) {
                if (compare(filter.getLeft(), filter.getOperator(), filter.getRight(), member, jsonNode)) {
                    flag = true;
                    break;
                }
            }
        }



        ((ObjectNode) jsonNode).put("id", scenarioId);
        if (flag) ((ObjectNode) jsonNode).put("success", 1);
        else  ((ObjectNode) jsonNode).put("success", 0);
        String newMessage = objectMapper.writeValueAsString(jsonNode);
        System.out.println(newMessage);
        kafkaTemplate.send(NEXT_TOPIC_PREFIX + scenarioId, newMessage);


//        //== 저장 ==//
//        if (flag) {
//            List<SuccessLog> buffer = successBuffer.get();
//            buffer.add(new SuccessLog(scenarioId, jsonNode.toString()));
//            flushSuccessIfNeeded(buffer);
//        } else {
//            List<FailureLog> buffer = failureBuffer.get();
//            buffer.add(new FailureLog(scenarioId, jsonNode.toString()));
//            flushFailureIfNeeded(buffer);
//        }

    }


//    private void flushSuccessIfNeeded(List<SuccessLog> buffer) {
//        long now = System.currentTimeMillis();
//        if (buffer.size() >= BATCH_SIZE || now - lastFlushTime.get() >= FLUSH_INTERVAL_MS) {
//
//            if (!buffer.isEmpty()) {
//                successLogRepository.saveAll(new ArrayList<>(buffer));
//                buffer.clear();
//            }
//
//            lastFlushTime.set(now);
//        }
//    }
//
//    private void flushFailureIfNeeded(List<FailureLog> buffer) {
//        long now = System.currentTimeMillis();
//        if (buffer.size() >= BATCH_SIZE || now - lastFlushTime.get() >= FLUSH_INTERVAL_MS) {
//
//            if (!buffer.isEmpty()) {
//                failureLogRepository.saveAll(new ArrayList<>(buffer));
//                buffer.clear();
//            }
//
//            lastFlushTime.set(now);
//        }
//    }


    private boolean compare(String left, String operator, String right, Member member, JsonNode jsonNode) {
        if (left.equals("age")) return compareAge(operator, right, member);
        if (left.equals("gender")) return compareGender(operator, right, member);
        if (left.equals("h") || left.equals("m") || left.equals("s")) return compareInt(left, operator, right, jsonNode);
        else return compareStr(left, operator, right, jsonNode);
    }

    private boolean compareStr(String left, String operator, String right, JsonNode jsonNode) {
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
    private boolean compareInt(String left, String operator, String right, JsonNode jsonNode) {
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
    private boolean compareGender(String operator, String right, Member member) {

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
    private boolean compareAge(String operator, String right, Member member) {

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

