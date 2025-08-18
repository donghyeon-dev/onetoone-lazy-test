# JPA 단방향 1:1 관계 Lazy Loading 가이드

## 개요

JPA에서 1:1 관계에서 진정한 Lazy Loading을 구현하는 방법은 여러 가지가 있습니다. 이 가이드에서는 각 방법의 장단점과 실제 동작을 비교해보겠습니다.

## ⚠️ 중요 발견: optional 속성의 영향

최신 테스트 결과, **양방향 관계에서도 `optional` 속성과 관계없이 lazy loading이 작동**하는 것을 확인했습니다. 이는 기존의 일반적인 이해와 다른 결과입니다.

## 실제 테스트 결과 비교

### 1. 양방향 관계에서의 Lazy Loading 동작

#### ✅ 양방향 관계 - optional=false

```java
// User 엔티티 (연관관계 주인)
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;
}

// UserProfile 엔티티 (mappedBy 사용)
@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    // optional=false로 설정 시 Lazy Loading 작동
    @OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY, optional = false)
    private User user;
}
```

**테스트 결과**: `getUserProfile()` 호출 시 쿼리 실행 안됨, `getBio()` 호출 시에만 쿼리 실행

#### ✅ 양방향 관계 - optional=true (기본값)

```java
// UserProfile 엔티티에서 optional=true 사용
@OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY, optional = true)
private User user;
```

**테스트 결과**: `optional=false`와 동일하게 Lazy Loading 정상 작동

### 스키마 차이점

- **optional=false**: `user_profile_id bigint NOT NULL unique`
- **optional=true**: `user_profile_id bigint NOT NULL unique` (실제로는 동일)

### 2. 단방향 관계에서의 Lazy Loading 동작

#### ✅ 단방향 관계 (권장 방법)

```java
// User 엔티티 (연관관계 주인)
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
    
    @Column(nullable = false, length = 18)
    private String password;
    
    @Column(nullable = false, unique = true, length = 30)
    private String email;
    
    // 단방향 관계로 진정한 Lazy Loading 구현
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;
    
    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}

// UserProfile 엔티티 (독립적)
@Entity(name = "USER_PROFILE")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(length = 100)
    private String bio;
    
    @Column(length = 20)
    private String phoneNumber;
    
    @Column(length = 100)
    private String address;
    
    // 양방향 관계 제거 - User 참조 없음
}
```

## 데이터베이스 스키마

위 구조는 다음과 같은 스키마를 생성합니다:

```sql
-- user_info 테이블
CREATE TABLE user_info (
    id BIGINT NOT NULL,
    user_profile_id BIGINT UNIQUE,  -- 외래키
    name VARCHAR(12) NOT NULL UNIQUE,
    password VARCHAR(18) NOT NULL,
    email VARCHAR(30) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

-- user_profile 테이블
CREATE TABLE user_profile (
    id BIGINT NOT NULL,
    phone_number VARCHAR(20),
    address VARCHAR(100),
    bio VARCHAR(100),
    PRIMARY KEY (id)
);

-- 외래키 제약조건
ALTER TABLE user_info 
ADD CONSTRAINT FK_user_profile 
FOREIGN KEY (user_profile_id) REFERENCES user_profile;
```

## 사용 방법

### 1. 기본 CRUD 작업

```java
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    // 사용자 생성
    public User createUser(String name, String email, String password) {
        UserProfile profile = UserProfile.builder()
            .bio("기본 프로필")
            .phoneNumber("010-0000-0000")
            .address("서울시")
            .build();
            
        User user = User.builder()
            .name(name)
            .email(email)
            .password(password)
            .userProfile(profile)
            .build();
            
        return userRepository.save(user);
    }
    
    // Lazy Loading 테스트
    public void testLazyLoading(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // 이 시점에서는 UserProfile 쿼리 실행 안됨
        UserProfile profile = user.getUserProfile();  
        
        // 실제 데이터 접근 시에만 쿼리 실행
        String bio = profile.getBio();  // 여기서 SELECT 쿼리 실행!
    }
}
```

### 2. 영속성 컨텍스트 초기화 (테스트 시 필요)

```java
@RestController
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @GetMapping("/lazy-test")
    public void lazyLoadingTest() {
        // 데이터 생성 및 저장
        User user = createAndSaveUser();
        
        // 영속성 컨텍스트 초기화 - 중요!
        entityManager.clear();
        
        // 이제 실제 DB에서 조회됨
        User foundUser = userRepository.findById(user.getId()).orElseThrow();
        
        // Lazy Loading 테스트
        UserProfile profile = foundUser.getUserProfile();  // 쿼리 실행 안됨
        String bio = profile.getBio();  // 여기서 쿼리 실행!
    }
}
```

## Lazy Loading 동작 확인

### 로그 출력 예시

```
-- findById() 실행 시
Hibernate: select u1_0.id,u1_0.email,u1_0.name,u1_0.password,u1_0.user_profile_id 
           from user_info u1_0 where u1_0.id=?

-- getUserProfile() 호출 시: 쿼리 실행 안됨 (프록시 반환)

-- getBio() 호출 시: 실제 Lazy Loading 발생
Hibernate: select up1_0.id,up1_0.address,up1_0.bio,up1_0.phone_number 
           from user_profile up1_0 where up1_0.id=?
```

## 주의사항

### 1. 영속성 컨텍스트 이슈

```java
// ❌ 같은 트랜잭션에서는 1차 캐시로 인해 쿼리 실행 안됨
User savedUser = userRepository.save(user);
User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();  // 캐시에서 조회

// ✅ 영속성 컨텍스트 초기화 후 조회
entityManager.clear();
User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();  // DB에서 조회
```

### 2. 양방향 참조가 필요한 경우

단방향 관계에서는 `UserProfile`에서 `User`에 접근할 수 없습니다. 필요한 경우:

```java
// Repository를 통한 역방향 조회
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserProfile(UserProfile userProfile);
    Optional<User> findByUserProfileId(Long userProfileId);
}
```

### 3. 테스트 환경에서의 차이점

- **단위 테스트**: Lazy Loading이 정상 작동
- **실제 애플리케이션**: 양방향 관계에서는 Eager Loading처럼 동작

## 성능 최적화 팁

### 1. 필요한 경우에만 조회

```java
// UserProfile이 필요 없는 경우
public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream()
        .map(user -> UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .build())  // UserProfile 접근 안함
        .collect(toList());
}
```

### 2. 페치 조인 사용 (필요시)

```java
@Query("SELECT u FROM User u JOIN FETCH u.userProfile WHERE u.id = :id")
Optional<User> findByIdWithProfile(@Param("id") Long id);
```

## 최종 결론 및 권장사항

### 🎯 핵심 발견사항

1. **양방향 관계도 Lazy Loading 가능**: `optional` 속성과 관계없이 양방향 관계에서도 lazy loading이 작동합니다
2. **단방향 관계가 여전히 권장**: 더 깔끔하고 예측 가능한 구조
3. **영속성 컨텍스트 이슈는 공통**: 모든 방법에서 테스트 시 `entityManager.clear()` 필요

### 📊 방법별 비교

| 방법 | Lazy Loading | 코드 복잡도 | 역방향 참조 | 권장도 |
|------|-------------|------------|------------|--------|
| **단방향 관계** | ✅ 완벽 작동 | 낮음 | Repository 필요 | ⭐⭐⭐⭐⭐ |
| **양방향 + optional=false** | ✅ 작동 | 중간 | 가능 | ⭐⭐⭐⭐ |
| **양방향 + optional=true** | ✅ 작동 | 중간 | 가능 | ⭐⭐⭐ |

### 🚀 권장사항

1. **새 프로젝트**: 단방향 관계 사용
2. **기존 프로젝트**: 양방향 관계도 문제없이 작동
3. **테스트 환경**: 반드시 영속성 컨텍스트 초기화 고려
4. **성능 최적화**: 필요에 따라 페치 조인 활용