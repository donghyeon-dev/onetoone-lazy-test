# Branch 별 매핑전략
1:1 매핑에서 LazyLoading을 사용하기위한 전략 목록 

## master (1:1 unidirectional mapping)
- LazyLoading을 사용하는 1:1 단방향 매핑 
## one-to-one-bidirectional (Byte Enhancement)
- Hibernate Byte Enhancement와 함께 LazyLoading을 사용하는 1:1 양방향 매핑 
- 순수하게 Lazy Loading처럼 보이기 어려운 부분이 있음
  - epxect: 자식 엔티티를 접근할 때(`user.getUserProfile()`)가 아닌, 자식 엔티티의 필드에 접근할때(`user.getUserProfile().getName()`) 조회쿼리 실행
  - actual: 자식 엔티티를 접근할때 조회쿼리 실행(`user.getUserProfile()`)
## one-to-one-bidirectional-mapsid
- @MapsId를 사용하여 부모의 PK가 자식의 PK이자 FK가 되는 1:1 양방향 매핑

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
