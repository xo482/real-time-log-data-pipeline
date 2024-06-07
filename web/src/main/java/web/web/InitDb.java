package web.web;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import web.web.domain.Gender;
import web.web.domain.Member;

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
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {
            Member member = new Member("memberA","ksh990408@skuniv.ac.kr", "1234!!", "21", Gender.MALE);
            em.persist(member);
        }
        public void dbInit2() {
            Member member = new Member("memberB","qwer4545@gmail.com", "zxczxc789~", "21", Gender.FEMALE);
            em.persist(member);
        }
    }
}