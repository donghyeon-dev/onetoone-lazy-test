package caidentia.onetoonelazy;

import caidentia.onetoonelazy.domain.UserBidirectionalWithMapsId;
import caidentia.onetoonelazy.domain.UserProfileBidirectionalWithMapsId;
import caidentia.onetoonelazy.repository.UserBidirectionalWithMapsIdRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
//@ActiveProfiles("test")
class BytecodeEnhancementTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserBidirectionalWithMapsIdRepository userBidirectionalWithMapsIdRepository;

    private UserBidirectionalWithMapsId savedUserBidirectionalWithMapsId;

    @BeforeEach
    void setUp() {
        UserProfileBidirectionalWithMapsId profile = UserProfileBidirectionalWithMapsId.builder()
                .bio("테스트 바이오")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        UserProfileBidirectionalWithMapsId savedProfile = entityManager.persistAndFlush(profile);
        
        UserBidirectionalWithMapsId userBidirectionalWithMapsId = UserBidirectionalWithMapsId.builder()
                .name("testUser")
                .email("test@example.com")
                .password("password123")
                .userProfile(savedProfile)
                .build();

        savedUserBidirectionalWithMapsId = entityManager.persistAndFlush(userBidirectionalWithMapsId);
        entityManager.clear();
    }

    @Test
    @DisplayName("양방향 @OneToOne lazy loading 테스트")
    @Transactional
    void testLazyLoading() {
        // UserBidirectionalWithMapsId 조회 (JPA Repository 사용)
        System.out.println("1########## Before userBidirectionalWithMapsIdRepository.findById");
        UserBidirectionalWithMapsId foundUserBidirectionalWithMapsId = userBidirectionalWithMapsIdRepository.findById(savedUserBidirectionalWithMapsId.getId()).orElseThrow();
        
        // UserProfileBidirectionalWithMapsId 접근 전 - lazy 상태 확인
        System.out.println("2########## Before foundUserBidirectionalWithMapsId.getUserProfileBidirectionalLazyLoadingWithMapsId");
        UserProfileBidirectionalWithMapsId userProfileBidirectionalWithMapsId = foundUserBidirectionalWithMapsId.getUserProfile();
        System.out.println("########## After foundUserBidirectionalWithMapsId.getUserProfileBidirectionalLazyLoadingWithMapsId()");
        assertThat(Hibernate.isInitialized(userProfileBidirectionalWithMapsId)).isFalse();
        
        // 실제 데이터 접근 시 초기화됨
        System.out.println("4########## Before userProfileBidirectionalWithMapsId.getBio();");
        String bio = userProfileBidirectionalWithMapsId.getBio();
        assertThat(Hibernate.isInitialized(userProfileBidirectionalWithMapsId)).isTrue();
        assertThat(bio).isEqualTo("테스트 바이오");
    }

}
