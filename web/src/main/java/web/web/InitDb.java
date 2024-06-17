package web.web;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import web.web.admin.domain.*;
import web.web.shoppingmall.domain.Gender;
import web.web.shoppingmall.domain.Member;
import web.web.shoppingmall.domain.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 2명 memberA, memberB
 */
@Component
@RequiredArgsConstructor
public class InitDb {
    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
        initService.dbInit3();
        initService.InitScenario1();
        initService.InitScenario2();
        initService.dbInit5();
        initService.dbInit6();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {
            Member member = new Member("memberA","member1@gmail.com", "1", "40", Gender.MALE);
            em.persist(member);
        }
        public void dbInit2() {
            Member member = new Member("memberB","member2@gmail.com", "2", "17", Gender.FEMALE);
            em.persist(member);
        }

        public void dbInit3() {
            Member member = new Member("memberC","member3@gmail.com", "3", "22", Gender.FEMALE);
            em.persist(member);
        }

        public void InitScenario1() {
            LogFormat logFormat = new LogFormat();
            logFormat.setDate(1);
            logFormat.setHTTP_USER_AGENT(1);
            em.persist(logFormat);

            Filter filter1 = new Filter("HTTP_USER_AGENT", "contains", "Chrome");

            List<Filter> filters = new ArrayList<>();
            filters.add(filter1);

            String title = "크롬으로 들어와 구매 버튼을 누른 사람의 비율은?";
            String manager = "홍길동";
            Status status = Status.RUN;
            LogicalOperator logicalOperator = LogicalOperator.AND;

            Scenario scenario = Scenario.createScenario(title, manager, status, logicalOperator, logFormat, filters);
            em.persist(scenario);
        }
        public void InitScenario2() {
            LogFormat logFormat = new LogFormat();
            logFormat.setE_n(1);
            logFormat.setH(1);
            logFormat.setMemberId(1);
            logFormat.setDate(1);
            em.persist(logFormat);

            Filter filter1 = new Filter("age", ">", "20");
            Filter filter2 = new Filter("gender", "==", "MALE");
            Filter filter3 = new Filter("h", ">=", "9");


            List<Filter> filters = new ArrayList<>();
            filters.add(filter1);
            filters.add(filter2);
            filters.add(filter3);

            String title = " 13시 이후 구매 버튼을 누르는 사람 중에 성인 남성 비율을 알아보기 위한 시나리오";
            String manager = "홍길동";
            Status status = Status.RUN;
            LogicalOperator logicalOperator = LogicalOperator.AND;

            Scenario scenario = Scenario.createScenario(title, manager, status, logicalOperator, logFormat, filters);
            em.persist(scenario);
        }


        public void dbInit5() {
            // 현재 날짜와 시간 가져오기
            LocalDateTime now = LocalDateTime.now();
            // 어제의 날짜와 시간 계산
            LocalDateTime yesterday = now.minusDays(1);
            Order order = new Order();
            order.setOrderDate(yesterday);
            em.persist(order);

            Order order2 = new Order();
            order2.setOrderDate(now);
            em.persist(order2);
        }

        public void dbInit6() {
            NumberOfVisitors numberOfVisitors = new NumberOfVisitors();
            numberOfVisitors.setCount(1000L);
            em.persist(numberOfVisitors);
        }
    }
}