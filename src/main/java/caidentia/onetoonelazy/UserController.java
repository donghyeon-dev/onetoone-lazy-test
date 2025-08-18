package caidentia.onetoonelazy;

import caidentia.onetoonelazy.domain.User;
import caidentia.onetoonelazy.domain.UserProfile;
import caidentia.onetoonelazy.repository.UserProfileRepository;
import caidentia.onetoonelazy.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private final UserProfileRepository userProfileRepository;

    private final EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/save")
    public void saveUser() {
        // User 생성 및 저장
        User user = User.builder()
                .name("lazyTest")
                .email("lazy@example.com")
                .password("password123")
                .build();

        UserProfile profile = UserProfile.builder()
                .bio("양방향 Lazy Loading 테스트")
                .phoneNumber("010-1111-2222")
                .address("서울시 강남구")
                .build();

        user.modifyUserProfile(profile);
        var savedUser = userRepository.saveAndFlush(user);

        // User 생성 및 저장
        User user2 = User.builder()
                .name("lazyTest2")
                .email("lazy2@example.com")
                .password("password123")
                .build();

        UserProfile profile2 = UserProfile.builder()
                .bio("양방향 Lazy Loading 테스트2")
                .phoneNumber("010-1111-2223")
                .address("서울시 강남구")
                .build();

        user2.modifyUserProfile(profile2);
        var savedUser2 = userRepository.saveAndFlush(user2);
    }

    @GetMapping("/lazy-test-user")
    public void lazyLoadingTest() {


        System.out.println("=== 양방향 1:1 Lazy Loading 테스트 ===");
        System.out.println("1. Before userRepository.findById");
        User foundUser = userRepository.findById(1L).orElseThrow();
        System.out.println("1. After userRepository.findById");

        System.out.println("2. Before foundUser.getUserProfile()");
        System.out.println("Expecting initialized would be true. Byte Enhancement 's property interceptor triggered by approching getter method.");
        UserProfile userProfile = foundUser.getUserProfile();
        System.out.println("2. After foundUser.getUserProfile()");
        System.out.println("   UserProfile is initialized: " + entityManagerFactory.getPersistenceUnitUtil().isLoaded(userProfile, "userProfile"));

        System.out.println("3. Before userProfile.getBio()");
        String bio = userProfile.getBio();
        System.out.println("3. After userProfile.getBio()");
        System.out.println("   UserProfile is initialized: " + entityManagerFactory.getPersistenceUnitUtil().isLoaded(userProfile, "userProfile"));
        System.out.println("   Bio: " + bio);

    }

    @GetMapping("/lazy-test-user-profile")
    public void profileLazyLoadingTest() {
        System.out.println("=== 양방향 1:1 Lazy Loading 테스트 ===");
        System.out.println("1. Before userProfileRepository.findById");
        UserProfile foundUserProfile = userProfileRepository.findById(1L).orElseThrow();
        System.out.println("1. After userProfileRepository.findById");

        System.out.println("2. Before foundUserProfile.getUser()");
        User user = foundUserProfile.getUser();
        System.out.println("   User is initialized: " + entityManagerFactory.getPersistenceUnitUtil().isLoaded(user, "user"));
        System.out.println("2. After foundUserProfile.getUser()");


        System.out.println("3. Before user.getName()");
        String userName = user.getName();
        System.out.println("3. After user.getName()");
        System.out.println("   User is initialized: " + entityManagerFactory.getPersistenceUnitUtil().isLoaded(user, "user"));
        System.out.println("   userName: " + userName);

    }
}
