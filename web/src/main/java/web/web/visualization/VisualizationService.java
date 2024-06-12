package web.web.visualization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.web.admin.domain.NumberOfVisitors;
import web.web.admin.repository.FailureLogRepository;
import web.web.admin.repository.NumberOfVisitorRepository;
import web.web.admin.repository.SuccessLogRepository;
import web.web.shoppingmall.domain.Order;
import web.web.shoppingmall.repository.MemberRepository;
import web.web.shoppingmall.repository.OrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VisualizationService {
    private final SuccessLogRepository successLogRepository;
    private final FailureLogRepository failureLogRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final NumberOfVisitorRepository numberOfVisitorRepository;

    public Long[] logCount() {
        Long[] list = new Long[2];
        list[0] = successLogRepository.count();
        list[1] = failureLogRepository.count();
        return list;
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
}
