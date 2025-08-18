package caidentia.onetoonelazy.domain;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private UserProfile userProfile;

    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        userProfile.modifyUser(this);
    }
}
