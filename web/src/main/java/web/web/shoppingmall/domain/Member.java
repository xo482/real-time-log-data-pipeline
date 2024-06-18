package web.web.shoppingmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private String email;
    private String password;
    private String age;
    private String address;


    @Enumerated(EnumType.STRING)
    private Gender gender;

    @CreatedDate
    @DateTimeFormat(pattern = "yyyy-MM-dd/HH:mm:ss")
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "member") //readOnly 가 된다.
    private List<Order> orders = new ArrayList<>();


    public Member(String username, String email, String password, String age, Gender gender) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
    }

    protected Member() {
    }
}
