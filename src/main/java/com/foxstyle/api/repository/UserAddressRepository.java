package com.foxstyle.api.repository;

import com.foxstyle.api.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {

    List<UserAddress> findByUserUserIdOrderByIsDefaultDesc(Integer userId);

    Optional<UserAddress> findByAddressIdAndUserUserId(Integer addressId, Integer userId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.userId = :userId")
    void clearDefaultAddress(@Param("userId") Integer userId);
}
