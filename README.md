
## one-to-one-bidirectional-mapsid
@MapsId를 사용하여 양방향 LazyLoading을 할때 핵심은 FK를 가지지 않은 부모 엔티티에서 `optional=false`를 선언하여 추가쿼리가 발생하지 않도록 하는것임.
### Domain
```java
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

    // 양방향
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user"
            , optional = false) // optional = false로 설정하여 항상 존재해야 함을 명시. User만 조회시 UserProfile은 프록시로 초기화하며 추가쿼리 발생하지 않음. 실제 접근시 쿼리실행
    private UserProfile userProfile;

    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        userProfile.modifyUser(this);
    }
}
```

```java
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private User user;

    public void modifyUser(User user) {
        this.user = user;
    }
}

```

### Table
```sql
create table user_info
(
    id       bigint      not null,
    name     varchar(12) not null unique,
    password varchar(18) not null,
    email    varchar(30) not null unique,
    primary key (id)
);

create table user_profile
(
    id      bigint not null,
    phone_number varchar(20),
    address      varchar(100),
    bio          varchar(100),
    primary key (id)
);

alter table if exists user_profile
    add constraint FKqn9hd9joc1cqolpc4io5wyv8n
        foreign key (id)
            references user_info
```
### 부모-> 자식 logs
```
=== 양방향 1:1 Lazy Loading 테스트 ===
1. Before userMapsIdRepository.findById
[Hibernate] 
    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password 
    from
        user_info u1_0 
    where
        u1_0.id=?
2025-08-18T15:18:16.184+09:00 TRACE 80904 --- [onetoone-lazy] [nio-8899-exec-2] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
1. After userMapsIdRepository.findById
2. Before foudnUser.getUserProfile()
2. After foudnUser.getUserProfile()
   UserProfile is initialized: false
3. Before userProfile.getBio()
[Hibernate] 
    select
        up1_0.id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number 
    from
        user_profile up1_0 
    where
        up1_0.id=?
2025-08-18T15:18:16.207+09:00 TRACE 80904 --- [onetoone-lazy] [nio-8899-exec-2] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
3. After userProfile.getBio()
   UserProfile is initialized: true
   Bio: 양방향 Lazy Loading 테스트

```

### 자식->부모 logs
```
=== 양방향 1:1 Lazy Loading 테스트 ===
1. Before userProfileMapsIdRepository.findById
[Hibernate] 
    select
        up1_0.id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number 
    from
        user_profile up1_0 
    where
        up1_0.id=?
2025-08-18T15:18:27.895+09:00 TRACE 80904 --- [onetoone-lazy] [nio-8899-exec-3] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
1. After userProfileMapsIdRepository.findById
2. Before foudnUserProfile.getUser()
   User is initialized: false
2. After foudnUserProfile.getUser()
3. Before user.getName()
[Hibernate] 
    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password 
    from
        user_info u1_0 
    where
        u1_0.id=?
2025-08-18T15:18:27.899+09:00 TRACE 80904 --- [onetoone-lazy] [nio-8899-exec-3] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
3. After user.getName()
   User is initialized: true
   userName: lazyTest
```
