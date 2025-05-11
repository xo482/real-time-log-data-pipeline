package web.web.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.web.admin.domain.NumberOfVisitors;
import web.web.admin.domain.Status;
import web.web.admin.dto.ScenarioRatioDto;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.NumberOfVisitorRepository;
import web.web.admin.repository.ScenarioRepository;
import web.web.admin.repository.SuccessLogRepository;
import web.web.shoppingmall.domain.Order;
import web.web.shoppingmall.repository.MemberRepository;
import web.web.shoppingmall.repository.OrderRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class VisualizationService {
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final NumberOfVisitorRepository numberOfVisitorRepository;
    private final ScenarioRepository scenarioRepository;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<Long, String> lastUpdatedAtByScenario = new ConcurrentHashMap<>(); // 이전에 처리된 Spark updatedAt 저장 (갱신 여부 판단용)



    public Long[] logCount() {
        Long[] list = new Long[2];

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoSecondsAgo = now.minusSeconds(12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        String startTime = twoSecondsAgo.format(formatter);
        String endTime = now.format(formatter);

        list[0] = successLogRepository.countLogsInTimeRange(startTime, endTime);
        list[1] = failureLogRepository.countLogsInTimeRange(startTime, endTime);

        return list;
    }

    public Map<Long, Long> liveCount() {
        Map<Long, Long> map = new HashMap<>();
        List<Long> scenarioIds = scenarioRepository.findAllIds();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoSecondsAgo = now.minusSeconds(4);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        String startTime = twoSecondsAgo.format(formatter);
        String endTime = now.format(formatter);

        for (Long scenarioId : scenarioIds) {
            if (scenarioRepository.findById(scenarioId).get().getStatus() == Status.RUN) {
                Long successLogCount = successLogRepository.countLogsInTimeRangeByScenarioId(scenarioId, startTime, endTime);
                map.put(scenarioId, successLogCount);
            }
        }

        return map;
    }

    public Long memberCount() {
        return memberRepository.count();
    }

    public Long dayOrderCount() {
        LocalDate today = LocalDate.now(); // 오늘 날짜

        // 오늘 주문한 주문의 수
        Long count = 0L;

        // 모든 주문을 순회하면서 오늘 주문한 주문의 수를 증가시킴
        for (Order order : orderRepository.findAll()) {
            LocalDateTime orderDateTime = order.getOrderDate(); // 주문 날짜와 시간 가져오기
            LocalDate orderDate = orderDateTime.toLocalDate(); // LocalDateTime을 LocalDate로 변환

            // 주문 날짜가 오늘 날짜와 동일한지 확인
            if (orderDate.equals(today)) {
                count++; // 오늘 주문한 주문 수 증가
            }
        }

        return count;
    }

    public Long pageView() {
        NumberOfVisitors numberOfVisitors = numberOfVisitorRepository.findById(1L).get();
        return numberOfVisitors.getCount();
    }

    /**
     * 5초마다 실행되는 스케줄러
     * - Redis에서 각 시나리오의 최신 상태를 조회
     * - 갱신되지 않은 경우 0값 삽입
     * - 새로운 시나리오 등장 시 9개의 0값 삽입 후 정상 흐름으로 진입
     */
    @Scheduled(fixedRate = 5000)
    public void syncScenarioHistory() {
        Set<String> keys = redisTemplate.keys("scenario:*:success_ratio");
        if (keys == null) return;

        for (String key : keys) {
            log.debug(key);
            Long scenarioId = Long.parseLong(key.split(":")[1]);
            String historyKey = key + ":history"; // ?

            // 현재 시간 기준으로 5초 단위 보정된 시간 (collectedAt 용)
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            ZonedDateTime nowCorrected = alignTo5Sec(now);

            // Spark가 Redis에 넣은 최신 상태 (String -> JSON)
            String rawJson = redisTemplate.opsForValue().get(key);
            if (rawJson == null) continue;

            ScenarioRatioDto parsed = parse(rawJson);
            parsed.setScenarioId(scenarioId);

            String currentUpdatedAt = parsed.getUpdatedAt(); // Spark 기준 시각
            String lastSeenUpdatedAt = lastUpdatedAtByScenario.get(scenarioId); // 이전에 본 Spark 시각

            // ✅ 새로운 시나리오 감지 시: 9건 0으로 초기화
            Long historySize = redisTemplate.opsForList().size(historyKey);
            if (historySize == null || historySize == 0) {
                // 최근 9개 시점을 기준으로 성공률 0.0 유입량 0 삽입
                initializeZeroHistory(scenarioId, 9, now);
            }

            // ✅ 유입이 없는 경우 (updatedAt이 이전과 같음)
            if (currentUpdatedAt != null && currentUpdatedAt.equals(lastSeenUpdatedAt)) {
                ScenarioRatioDto zeroDto = createZeroDto(scenarioId, nowCorrected);
                redisTemplate.opsForList().rightPush(historyKey, toJson(zeroDto));
                redisTemplate.opsForList().trim(historyKey, -10, -1);
            }
            // ✅ 새로운 유입 발생 시: 실제 데이터 저장
            else {
                parsed.setCollectedAt(nowCorrected); // Spring 기준 시간으로 보정
                redisTemplate.opsForList().rightPush(historyKey, toJson(parsed));
                redisTemplate.opsForList().trim(historyKey, -10, -1);
                lastUpdatedAtByScenario.put(scenarioId, currentUpdatedAt); // 상태 갱신
            }
        }
    }

    /**
     * 서버 시작 시 실행됨
     * - Redis에 존재하는 모든 시나리오에 대해 10개의 0값 히스토리를 미리 삽입
     * - 그래프가 공백 없이 시작되도록 보장
     */
    @PostConstruct
    public void initializeAllScenarioHistories() {
        Set<String> keys = redisTemplate.keys("scenario:*:success_ratio");
        if (keys == null) return;

        for (String key : keys) {
            Long scenarioId = Long.parseLong(key.split(":")[1]);
            redisTemplate.delete("scenario:" + scenarioId + ":success_ratio:history");

            // 현재 시점 기준 10개의 0값 데이터 삽입
            initializeZeroHistory(scenarioId, 10, ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
        }
    }

    /**
     * 특정 시나리오에 대해 count개 만큼 0 DTO 삽입 (과거 시점 기준으로)
     * @param scenarioId 대상 시나리오 ID
     * @param count 삽입할 0 DTO 수 (일반적으로 9 또는 10)
     * @param baseTime 기준 시각 (현재 시각)
     */
    private void initializeZeroHistory(Long scenarioId, int count, ZonedDateTime baseTime) {
        String historyKey = "scenario:" + scenarioId + ":success_ratio:history";
        for (int i = count - 1; i >= 0; i--) {
            // baseTime으로부터 과거 i*5초씩 떨어진 시각 생성
            ZonedDateTime ts = alignTo5Sec(baseTime.minusSeconds(i * 5L));
            ScenarioRatioDto dto = createZeroDto(scenarioId, ts);
            redisTemplate.opsForList().rightPush(historyKey, toJson(dto));
        }
    }

    /**
     * 전체 시나리오의 최근 10개 히스토리를 조회
     * - 관리자 화면이나 그래프 API에서 호출
     */
    public Map<Long, List<ScenarioRatioDto>> getAllScenarioHistories() {
        Set<String> keys = redisTemplate.keys("scenario:*:success_ratio:history");
        if (keys == null || keys.isEmpty()) return Map.of();

        Map<Long, List<ScenarioRatioDto>> result = new HashMap<>();

        for (String key : keys) {
            try {
                Long scenarioId = Long.parseLong(key.split(":")[1]);
                List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
                if (jsonList == null) continue;

                List<ScenarioRatioDto> history = jsonList.stream().map(json -> {
                    try {
                        ScenarioRatioDto dto = objectMapper.readValue(json, ScenarioRatioDto.class);
                        dto.setScenarioId(scenarioId);
                        return dto;
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).toList();

                result.put(scenarioId, history);
            } catch (Exception e) {
                // 잘못된 키 무시 (e.g. 형식 불일치)
            }
        }

        return result;
    }

    /**
     * 유입이 없던 시각에 대응되는 0 DTO 생성
     * @param scenarioId 시나리오 ID
     * @param correctedTime 보정된 시각 (collectedAt으로 사용)
     */
    private ScenarioRatioDto createZeroDto(Long scenarioId, ZonedDateTime correctedTime) {
        ScenarioRatioDto dto = new ScenarioRatioDto();
        dto.setScenarioId(scenarioId);
        dto.setSuccessRatio(0.0);
        dto.setTotalCount(0);
        dto.setSuccessCount(0);
        dto.setCollectedAt(correctedTime); // Spring 보정 시간
        dto.setUpdatedAt(null); // 유입 없으므로 Spark 기준 시각은 없음
        return dto;
    }

    /**
     * Redis에 저장된 JSON 문자열 → DTO 역직렬화
     */
    private ScenarioRatioDto parse(String json) {
        try {
            return objectMapper.readValue(json, ScenarioRatioDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    /**
     * DTO → JSON 문자열 직렬화
     */
    private String toJson(ScenarioRatioDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * 주어진 ZonedDateTime을 가장 가까운 5초 단위로 보정
     * 예: 12:03:07 → 12:03:05 / 12:03:11 → 12:03:10
     */
    private ZonedDateTime alignTo5Sec(ZonedDateTime time) {
        int second = (time.getSecond() / 5) * 5;
        return time.withSecond(second).withNano(0);
    }

}
