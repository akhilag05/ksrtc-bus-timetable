package com.ksrtc.bustimetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ksrtc.bustimetable.entity.Stop;

public interface StopRepository extends JpaRepository<Stop, Long> {

    List<Stop> findByNormalizedName(String normalizedName);

    Optional<Stop> findFirstByNormalizedName(String normalizedName);
}

