package caidentia.onetoonelazy;

import caidentia.onetoonelazy.domain.User;
import caidentia.onetoonelazy.domain.UserProfile;
import caidentia.onetoonelazy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/lazy-test")
    public void lazyLoadingTest(){
        // User 생성 및 저장
        User user = User.builder()
                .name("lazyTest")
                .email("lazy@example.com")
                .password("password123")
                .build();

        UserProfile profile = UserProfile.builder()
                .bio("단방향 Lazy Loading 테스트")
                .phoneNumber("010-1111-2222")
                .address("서울시 강남구")
                .build();

        user.modifyUserProfile(profile);
        var savedUser = userRepository.saveAndFlush(user);
        
        // 영속성 컨텍스트 초기화 - 중요!
        entityManager.clear();

        System.out.println("=== 단방향 1:1 Lazy Loading 테스트 ===");
        System.out.println("1. Before userRepository.findById");
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        System.out.println("1. After userRepository.findById");

        System.out.println("2. Before foundUser.getUserProfile()");
        UserProfile userProfile = foundUser.getUserProfile();
        System.out.println("2. After foundUser.getUserProfile()");
        System.out.println("   UserProfile is initialized: " + org.hibernate.Hibernate.isInitialized(userProfile));

        System.out.println("3. Before userProfile.getBio()");
        String bio = userProfile.getBio();
        System.out.println("3. After userProfile.getBio()");
        System.out.println("   UserProfile is initialized: " + org.hibernate.Hibernate.isInitialized(userProfile));
        System.out.println("   Bio: " + bio);
        
        userRepository.delete(foundUser);
    }
}
