package kafka.kafka.shoppingmall.repository;

import kafka.kafka.shoppingmall.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {
}
