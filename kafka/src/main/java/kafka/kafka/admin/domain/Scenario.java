package kafka.kafka.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Scenario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scenario_id")
    private Long id;

    private String title;
    private String manager;

    @ManyToOne
    @JoinColumn(name = "log_format_id")
    private LogFormat logFormat;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scenario_filter", joinColumns = @JoinColumn(name = "scenario_id"))
    private List<Filter> filters = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private LogicalOperator logicalOperator;

    public static Scenario createScenario(String title, String manager, Status status, LogicalOperator logicalOperator, LogFormat logFormat, List<Filter> filters) {
        Scenario scenario = new Scenario();
        scenario.title = title;
        scenario.manager = manager;
        scenario.status = status;
        scenario.logicalOperator = logicalOperator;
        scenario.logFormat = logFormat;
        scenario.filters = filters;

        return scenario;
    }

    // 필터를 추가하는 메서드
    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    // 필터를 제거하는 메서드
    public void removeFilter(Filter filter) {
        this.filters.remove(filter);
    }

    // 시나리오의 현재 실행 상태를 반환하는 메서드
    // json에서 running이라는 필드를 찾지 않도록 애노테이션 추가
    @JsonIgnore
    public boolean isRunning() {
        return this.status == Status.RUN;
    }
}

