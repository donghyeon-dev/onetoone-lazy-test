package caidentia.onetoonelazy.repository;

import caidentia.onetoonelazy.domain.UserProfileBidirectionalWithMapsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileBidirectionalWithMapsIdRepository extends JpaRepository<UserProfileBidirectionalWithMapsId, Long> {
}
