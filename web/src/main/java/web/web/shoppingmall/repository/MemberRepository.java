package web.web.shoppingmall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.shoppingmall.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
