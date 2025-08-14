# Branch 별 전략

- master: 1:1 unidirectional mapping
- one-to-one-bidirectional: 1:1 bidirectional mapping with Byte Enhancement

## master(1:1 unidirectional mapping)

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

    // 단방향 예시로 명확한 lazy loading 시점 테스트
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
```

``` java
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

    // 단방향 관계로 변경 - User 참조 제거
}
```

### Table

```sql
create table user_info
(
    id              bigint      not null,
    user_profile_id bigint unique,
    name            varchar(12) not null unique,
    password        varchar(18) not null,
    email           varchar(30) not null unique,
    primary key (id)
);
create table user_profile
(
    id           bigint not null,
    phone_number varchar(20),
    address      varchar(100),
    bio          varchar(100),
    primary key (id)
);
alter table if exists user_info
    add constraint FKrp1jpwbye24ypkop95ygtr5bs
        foreign key (user_profile_id)
            references user_profile

```

### logs

```
=== 단방향 1:1 Lazy Loading 테스트 ===
1. Before userRepository.findById
Hibernate: 
    select
        u1_0.id,
        u1_0.email,
        u1_0.name,
        u1_0.password,
        u1_0.user_profile_id 
    from
        user_info u1_0 
    where
        u1_0.id=?
1. After userRepository.findById
2. Before foundUser.getUserProfile()
2. After foundUser.getUserProfile()
   UserProfile is initialized: false
3. Before userProfile.getBio()
Hibernate: 
    select
        up1_0.id,
        up1_0.address,
        up1_0.bio,
        up1_0.phone_number 
    from
        user_profile up1_0 
    where
        up1_0.id=?
3. After userProfile.getBio()
   UserProfile is initialized: true
   Bio: 단방향 Lazy Loading 테스트
```

## master(1:1 unidirectional mapping with @MapsId)
@MapsId를 사용함으로써 부모의 PK와 자식의 PK가 동일한 값으로 사용된다.
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

    // 단방향 예시로 명확한 lazy loading 시점 테스트
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    private UserProfile userProfile;

    public void modifyUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }
}
```


```java
public class UserProfile {

    @Id
    private Long id;

    @Column(length = 100)
    private String bio;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String address;

    // 단방향 관계로 변경 - User 참조 제거
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
    phone_number varchar(20),
    address      varchar(100),
    bio          varchar(100),
    primary key (id)
);

alter table if exists user_info
    add constraint FKn5qwr4sh64d0pv07y5ad1eigq
        foreign key (id)
            references user_profile
 ```
