package caidentia.onetoonelazy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "USER_PROFILE")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    private Long id;

    @Column(length = 100)
    private String bio;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String address;

    @OneToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private User user;

    public void modifyUser(User user) {
        this.user = user;
    }
}
