package com.foxstyle.api.repository;

import com.foxstyle.api.entity.DailyBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyBackupRepository extends JpaRepository<DailyBackup, Long> {
    Optional<DailyBackup> findByBackupDate(LocalDate backupDate);
    List<DailyBackup> findAllByOrderByBackupDateDesc();
}
