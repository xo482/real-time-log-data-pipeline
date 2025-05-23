package web.web.admin.domain;

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

    @ElementCollection
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
}
