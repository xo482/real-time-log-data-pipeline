package kafka.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kafka.kafka.admin.domain.Filter;
import kafka.kafka.admin.domain.LogFormat;
import kafka.kafka.admin.domain.LogicalOperator;
import kafka.kafka.admin.domain.Scenario;
import kafka.kafka.admin.repository.ScenarioRepository;
import kafka.kafka.exception.FilterEvaluationException;
import kafka.kafka.exception.FormatProcessingException;
import kafka.kafka.exception.InvalidLogDataException;
import kafka.kafka.exception.ScenarioNotRunningException;
import kafka.kafka.shoppingmall.domain.Gender;
import kafka.kafka.shoppingmall.domain.Member;
import kafka.kafka.shoppingmall.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioProcessingService {

    private static final String TOPIC_NAME = "scenario-processing-topic";
    private static final String CONSUMER_GROUP_NAME = "scenario-processing-group";
    private static final String NEXT_TOPIC_PREFIX = "spark-ingest-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final RedisService redisService;
    private final ScenarioRepository scenarioRepository;
    private final MemberRepository memberRepository;

    @KafkaListener(topics = TOPIC_NAME, groupId = CONSUMER_GROUP_NAME, concurrency = "9")
    public void listen(String message) {
        try {
            // JSON 객체로 파싱
            JsonNode jsonNode = objectMapper.readTree(message);

            // 내가 원하는 데이터가 들어온 것이 아니라면 메서드 종료시킴
            if (!jsonNode.has("path")) throw new InvalidLogDataException("Missing 'path' field in log data");


            // path 필드에서 쿼리 문자열과 날짜 꺼냄
            String queryData = jsonNode.get("path").asText();
            String date = jsonNode.get("date").asText();


            // 쿼리 문자열에서 필요한 정보만 추출
            String[] queryDataArr = queryData.split("\\?");
            String scenarioIdString = queryDataArr[0];
            String queryString = queryDataArr[1];
            // 쿼리 문자열을 JSON 객체로 변환
            JsonNode queryJson = convertQueryStringToJson(queryString);
            // 시나리오 ID 추출
            Long scenarioId = Long.parseLong(scenarioIdString.substring(1));
            // 불핋요한 필드 삭제 (uadata)
            if (queryJson.has("uadata"))
                ((ObjectNode) queryJson).remove("uadata");
            // date 값을 paramsJson에 추가
            ((ObjectNode) queryJson).put("date", date);

            // 시나리오 정보 cache or DB 에서 가져옴
            Scenario scenario = getScenarioFromCacheOrDb(scenarioId);
            // 존재 및 실행 중 여부 확인
            if (scenario == null || !scenario.isRunning()) {
                throw new ScenarioNotRunningException("Scenario " + scenarioId + " is not found or not running");
            }

            ////////////////  여기서부터 로그 포멧 변환 ////////////////

            LogFormat format = scenario.getLogFormat();
            parsingJsonByFormat(format, jsonNode);

            ////////////////////////////////////////// 필터링

            List<Filter> filters = scenario.getFilters();
            LogicalOperator logicalOperator = scenario.getLogicalOperator();
            Member member = getMemberFromCacheOrDb(jsonNode);

            // 5. 필터링 로직
            boolean flag = evaluateFilters(logicalOperator, filters, member, jsonNode);

            // spark 전송을 위해 spark_ingest_topic으로 전송
            ((ObjectNode) jsonNode).put("scenario_id", scenarioId);
            if (flag) ((ObjectNode) jsonNode).put("success", 1);
            else ((ObjectNode) jsonNode).put("success", 0);
            String newMessage = objectMapper.writeValueAsString(jsonNode);
            kafkaTemplate.send(NEXT_TOPIC_PREFIX, newMessage);

        } catch (InvalidLogDataException e) {
            log.warn("[INVALID LOG] {}", e.getMessage());
        } catch (ScenarioNotRunningException e) {
            log.warn("[SCENARIO STOPPED] {}", e.getMessage());
        } catch (FormatProcessingException e) {
            log.error("[FORMAT ERROR] {}", e.getMessage());
        } catch (FilterEvaluationException e) {
            log.error("[FILTER ERROR] {}", e.getMessage());
        } catch (Exception e) {
            log.error("[UNEXPECTED ERROR] {}", e.getMessage());
        }
    }

    private boolean evaluateFilters(LogicalOperator logicalOperator, List<Filter> filters, Member member, JsonNode jsonNode) throws FilterEvaluationException {
        try {
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
            return flag;
        }
        catch (Exception e) {
            throw new FilterEvaluationException("Filter evaluation failed", e);
        }
    }

    private Member getMemberFromCacheOrDb(JsonNode jsonNode) throws JsonProcessingException {
        Long memberId = null;
        Member member = null;
        if (jsonNode.has("memberId")) memberId = jsonNode.get("memberId").asLong();
        if (memberId != null) {
            String memberKey = "member:" + memberId + ":info";
            String memberString = redisService.getValue(memberKey);
            if (memberString != null) member = objectMapper.readValue(memberString, Member.class);
            else {
                member = memberRepository.findById(memberId).orElse(null);
                redisService.setValueWithTTL(memberKey, objectMapper.writeValueAsString(member), 600);
            }
        }
        return member;
    }

    private void parsingJsonByFormat(LogFormat format, JsonNode jsonNode) throws FormatProcessingException {
        if (!(jsonNode instanceof ObjectNode)) return;
        ObjectNode objNode = (ObjectNode) jsonNode;

        Field[] fields = LogFormat.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                String key = field.getName();
                if ("id".equals(key)) continue;
                if ((int) field.get(format) == 0) {
                    objNode.remove(key);
                }
            } catch (Exception e) {
                throw new FormatProcessingException("Failed to access field in LogFormat via reflection", e);
            }
        }
    }

    private Scenario getScenarioFromCacheOrDb(Long scenarioId) throws Exception {
        String key = "scenario:" + scenarioId + ":data";
        String scenarioJson = redisService.getValue(key);

        if (scenarioJson != null) {
            return objectMapper.readValue(scenarioJson, Scenario.class);
        } else {
            Scenario scenario = scenarioRepository.findById(scenarioId).orElse(null);
            if (scenario != null) {
                redisService.setValueWithTTL(key, objectMapper.writeValueAsString(scenario), 600);
            }
            return scenario;
        }
    }

    // 쿼리 문자열을 JSON 형식으로 변환하는 메서드
    private JsonNode convertQueryStringToJson(String queryString) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode queryJson = objectMapper.createObjectNode();

        // 쿼리 문자열을 &로 구분하여 각 파라미터를 분리하고, 이를 JSON 객체로 변환
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                queryJson.put(key, value);
            }
        }

        return queryJson;
    }


    private boolean compare(String left, String operator, String right, Member member, JsonNode jsonNode) {
        if (left.equals("age")) return compareAge(operator, right, member);
        if (left.equals("gender")) return compareGender(operator, right, member);
        if (left.equals("h") || left.equals("m") || left.equals("s")) return compareInt(left, operator, right, jsonNode);
        else return compareStr(left, operator, right, jsonNode);
    }

    private boolean compareStr(String left, String operator, String right, JsonNode jsonNode) {
        if (jsonNode.get(left) == null) return false;
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
}