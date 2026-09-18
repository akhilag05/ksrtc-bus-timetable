package com.ksrtc.bustimetable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ksrtc.bustimetable.entity.BusService;
import com.ksrtc.bustimetable.entity.Route;

public interface BusServiceRepository extends JpaRepository<BusService, Long> {

    List<BusService> findByRoute(Route route);

    List<BusService> findByRouteAndServiceClass(
            Route route,
            String serviceClass);
}