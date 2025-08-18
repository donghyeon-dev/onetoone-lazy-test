package caidentia.onetoonelazy.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity(name = "USER_INFO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String name;

    @Column(nullable = false, length=18)
    private String password;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    // 양방향
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user"
            , optional = false) // optional = false로 설정하여 항상 존재해야 함을 명시. User만 조회시 UserProfile은 프록시로 초기화하며 추가쿼리 발생하지 않음. 실제 접근시 쿼리실행
    private UserProfile userProfile;

    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        userProfile.modifyUser(this);
    }
}
