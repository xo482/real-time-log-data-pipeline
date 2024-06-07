package web.web.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter @Setter
public class Filter {

    @Column(name = "filter_left")
    private String left;
    @Column(name = "filter_operator")
    private String operator;
    @Column(name = "filter_right")
    private String right;

    // 기본 생성자 및 필요한 생성자
    public Filter() {
    }

    public Filter(String left, String operator, String right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}
