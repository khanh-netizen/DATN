package com.foxstyle.api.repository;

import com.foxstyle.api.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Integer> {
    Optional<Setting> findBySettingKey(String settingKey);
    boolean existsBySettingKeyIgnoreCase(String settingKey);
}
