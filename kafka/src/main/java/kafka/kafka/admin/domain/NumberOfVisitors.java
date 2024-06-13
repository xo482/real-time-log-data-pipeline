package kafka.kafka.admin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class NumberOfVisitors {

    @Id
    @GeneratedValue
    private Long id;

    private Long count;
}
