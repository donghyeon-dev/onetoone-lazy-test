package caidentia.onetoonelazy;

import caidentia.onetoonelazy.domain.User;
import caidentia.onetoonelazy.domain.UserProfile;
import caidentia.onetoonelazy.repository.UserRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
//@ActiveProfiles("test")
class BytecodeEnhancementTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        UserProfile profile = UserProfile.builder()
                .bio("테스트 바이오")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        UserProfile savedProfile = entityManager.persistAndFlush(profile);
        
        User user = User.builder()
                .name("testUser")
                .email("test@example.com")
                .password("password123")
                .userProfile(savedProfile)
                .build();

        savedUser = entityManager.persistAndFlush(user);
        entityManager.clear();
    }

    @Test
    @DisplayName("양방향 @OneToOne lazy loading 테스트")
    @Transactional
    void testLazyLoading() {
        // User 조회 (JPA Repository 사용)
        System.out.println("1########## Before userRepository.findById");
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        
        // UserProfile 접근 전 - lazy 상태 확인
        System.out.println("2########## Before foundUser.getUserProfile");
        UserProfile userProfile = foundUser.getUserProfile();
        System.out.println("########## After foundUser.getUserProfile()");
        assertThat(Hibernate.isInitialized(userProfile)).isFalse();
        
        // 실제 데이터 접근 시 초기화됨
        System.out.println("4########## Before userProfile.getBio();");
        String bio = userProfile.getBio();
        assertThat(Hibernate.isInitialized(userProfile)).isTrue();
        assertThat(bio).isEqualTo("테스트 바이오");
    }

}
