package web.web.admin.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import web.web.admin.domain.NumberOfVisitors;

public interface NumberOfVisitorRepository extends JpaRepository<NumberOfVisitors,Long> {
}
