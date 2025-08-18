package caidentia.onetoonelazy.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity(name = "USER_INFO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBidirectionalWithMapsId {

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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false, mappedBy = "user")
    private UserProfileBidirectionalWithMapsId userProfile;

    public void modifyUserProfile(UserProfileBidirectionalWithMapsId userProfile) {
        this.userProfile = userProfile;
        userProfile.modifyUser(this);
    }
}
