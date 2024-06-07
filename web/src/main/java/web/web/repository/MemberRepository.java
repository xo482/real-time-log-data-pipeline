package web.web.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
