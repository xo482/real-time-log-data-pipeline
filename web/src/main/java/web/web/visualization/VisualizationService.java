package web.web.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    public List<ScenarioRatioDto> getScenarioRatios() {
        Set<String> keys = redisTemplate.keys("scenario:*:success_ratio");
        if (keys == null) return List.of();

        return keys.stream()
                .map(key -> {
            try {

                String value = redisTemplate.opsForValue().get(key);
                if (value == null) return null;

                ScenarioRatioDto dto = objectMapper.readValue(value, ScenarioRatioDto.class);
                String[] parts = key.split(":");
                dto.setScenarioId(Long.parseLong(parts[1]));

                // updateAt이 5초 이상 차이나면 최근에 갱신된 데이터가 아니므로(유입된 데이터가 없었음) ratio 0으로 처리
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime updateAt = LocalDateTime.parse(dto.getUpdatedAt(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                if (Duration.between(updateAt, now).toSeconds() > 5) {
                    dto.setSuccessRatio(0.0);
                    dto.setTotalCount(0);
                }

                return dto;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        })
                .filter(Objects::nonNull)
                .toList();
    }
}
