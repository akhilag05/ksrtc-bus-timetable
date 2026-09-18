package com.ksrtc.bustimetable.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ksrtc.bustimetable.entity.RouteDistance;
import com.ksrtc.bustimetable.entity.Stop;

public interface RouteDistanceRepository
        extends JpaRepository<RouteDistance, Long> {

    Optional<RouteDistance> findByFromStopAndToStop(
            Stop fromStop,
            Stop toStop);
}