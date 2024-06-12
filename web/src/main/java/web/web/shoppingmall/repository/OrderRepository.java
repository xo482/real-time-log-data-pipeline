package web.web.shoppingmall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import web.web.shoppingmall.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
