package com.ksrtc.bustimetable.repository;

import com.ksrtc.bustimetable.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}