package com.ksrtc.bustimetable.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ksrtc.bustimetable.service.GeoapifyRoutingService;

@RestController
@RequestMapping("/api/geoapify")
public class GeoapifyTestController {

    private final GeoapifyRoutingService geoapifyRoutingService;

    public GeoapifyTestController(
            GeoapifyRoutingService geoapifyRoutingService) {
        this.geoapifyRoutingService = geoapifyRoutingService;
    }

    @GetMapping("/route")
    public String getRoute(
            @RequestParam double fromLat,
            @RequestParam double fromLon,
            @RequestParam double toLat,
            @RequestParam double toLon) {

        return geoapifyRoutingService.getRoute(
                fromLat,
                fromLon,
                toLat,
                toLon);
    }
}