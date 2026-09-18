package com.ksrtc.bustimetable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ksrtc.bustimetable.entity.Route;
import com.ksrtc.bustimetable.entity.RouteStop;
import com.ksrtc.bustimetable.entity.Stop;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    List<RouteStop> findByStop(Stop stop);

    List<RouteStop> findByRouteOrderByStopOrderAsc(Route route);
}