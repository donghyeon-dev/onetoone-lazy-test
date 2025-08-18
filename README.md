## one-to-one-bidirectional(Byte Enhancement)
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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
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
    @JoinColumn(name = "user_id")
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
    id           bigint not null,
    user_id      bigint unique,
    phone_number varchar(20),
    address      varchar(100),
    bio          varchar(100),
    primary key (id)
);

alter table if exists user_profile
    add constraint FKqn9hd9joc1cqolpc4io5wyv8n
        foreign key (user_id)
            references user_info
```
### 부모-> 자식 logs
Byte Enhancement에 의해 자식 엔티티를 접근할 때(`user.getUserProfile()`) proeprty inceteptor에 의해 조회쿼리 발생 
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
2025-08-18T14:41:59.702+09:00 TRACE 66160 --- [onetoone-lazy] [nio-8899-exec-4] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
1. After userRepository.findById
2. Before foundUser.getUserProfile()
Expecting initialized would be true. Byte Enhancement 's property interceptor triggered by approching getter method.
[Hibernate] 
    select
        up1_0.id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number,
        up1_0.user_id 
    from
        user_info u1_0 
    left join
        user_profile up1_0 
            on u1_0.id=up1_0.user_id 
    where
        u1_0.id=?
2025-08-18T14:41:59.705+09:00 TRACE 66160 --- [onetoone-lazy] [nio-8899-exec-4] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
2. After foundUser.getUserProfile()
   UserProfile is initialized: true
3. Before userProfile.getBio()
3. After userProfile.getBio()
   UserProfile is initialized: true
   Bio: 양방향 Lazy Loading 테스트
```

### 자식->부모 logs
```
=== 양방향 1:1 Lazy Loading 테스트 ===
1. Before userProfileRepository.findById
[Hibernate] 
    select
        up1_0.id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number,
        up1_0.user_id 
    from
        user_profile up1_0 
    where
        up1_0.id=?
2025-08-18T14:42:53.790+09:00 TRACE 66160 --- [onetoone-lazy] [nio-8899-exec-6] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
1. After userProfileRepository.findById
2. Before foundUserProfile.getUser()
   User is initialized: false
2. After foundUserProfile.getUser()
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
2025-08-18T14:42:53.795+09:00 TRACE 66160 --- [onetoone-lazy] [nio-8899-exec-6] org.hibernate.orm.jdbc.bind              : binding parameter (1:BIGINT) <- [1]
3. After user.getName()
   User is initialized: true
   userName: lazyTest
```
