# Branch 별 전략
- master: 1:1 unidirectional mapping
- one-to-one-bidirectional: 1:1 bidirectional mapping with Byte Enhancement
- one-to-one-bidirectional-mapsid: 1:1 bidirectional mapping with @MapsId





## one-to-one-bidirectional-mapsid
### Domain
```java
public class UserBidirectionalLazyLoadingWithMapsId {

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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false, mappedBy = "userBidirectionalWithMapsId")
    private UserProfile userProfile;

    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        userProfile.modifyUser(this);
    }
}
```

```java
public class UserProfileBidirectionalLazyLoadingWithMapsId {

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
    user_id      bigint not null,
    phone_number varchar(20),
    address      varchar(100),
    bio          varchar(100),
    primary key (user_id)
);

alter table if exists user_profile
    add constraint FKqn9hd9joc1cqolpc4io5wyv8n
        foreign key (user_id)
            references user_info
```
### 부모-> 자식 logs
```
=== 양방향 1:1 Lazy Loading 테스트 ===
1. Before userRepository.findById
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
2025-08-18T10:43:26.306+09:00 TRACE 74560 --- [onetoone-lazy] [nio-8899-exec-2] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
1. After userRepository.findById
2. Before foundUserBidirectionalWithMapsId.getUserProfile()
2. After foundUserBidirectionalWithMapsId.getUserProfile()
   UserProfile is initialized: false
3. Before userProfileBidirectionalWithMapsId.getBio()
[Hibernate] 
    select
        up1_0.user_id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number 
    from
        user_profile up1_0 
    where
        up1_0.user_id=?
2025-08-18T10:43:26.333+09:00 TRACE 74560 --- [onetoone-lazy] [nio-8899-exec-2] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
3. After userProfileBidirectionalWithMapsId.getBio()
   UserProfile is initialized: true
   Bio: 양방향 Lazy Loading 테스트

```

### 자식->부모 logs
```
=== 양방향 1:1 Lazy Loading 테스트 ===
1. Before userProfileRepository.findById
[Hibernate] 
    select
        up1_0.user_id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number 
    from
        user_profile up1_0 
    where
        up1_0.user_id=?
2025-08-18T10:43:49.686+09:00 TRACE 74560 --- [onetoone-lazy] [nio-8899-exec-3] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
1. After userProfileRepository.findById
2. Before foundUserProfileBidirectionalLazyLoadingWithMapsId.getUser()
   User is initialized: false
2. After foundUserProfileBidirectionalLazyLoadingWithMapsId.getUser()
3. Before userBidirectionalWithMapsId.getName()
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
2025-08-18T10:43:49.689+09:00 TRACE 74560 --- [onetoone-lazy] [nio-8899-exec-3] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
3. After userBidirectionalWithMapsId.getName()
   User is initialized: true
   userName: lazyTest

```
