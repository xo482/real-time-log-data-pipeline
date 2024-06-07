package web.web;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import web.web.admin.domain.*;
import web.web.domain.Gender;
import web.web.domain.Member;

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
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {
            Member member = new Member("김성현","ksh990408@skuniv.ac.kr", "1234!!", "21", Gender.MALE);
            em.persist(member);
        }
        public void dbInit2() {
            Member member = new Member("가나다라","qwer4545@gmail.com", "zxczxc789~", "21", Gender.FEMALE);
            em.persist(member);
        }

        public void dbInit3() {
            LogFormat logFormat = new LogFormat();
            logFormat.setE_n(1);
            logFormat.setTime(1);
            logFormat.setMemberId(1);
            em.persist(logFormat);

            Filter filter1 = new Filter("나이", "<", "20");
            Filter filter2 = new Filter("성별", "==", "MALE");

            List<Filter> filters = new ArrayList<>();
            filters.add(filter1);
            filters.add(filter2);

            String title = "구매 버튼을 누르는 사람 중에 성인 남성 비율을 알아보기 위한 시나리오";
            String manager = "홍길동";
            Status status = Status.RUN;
            LogicalOperator logicalOperator = LogicalOperator.AND;

            Scenario scenario = Scenario.createScenario(title, manager, status, logicalOperator, logFormat, filters);
            em.persist(scenario);
        }
    }
}