package com.ksrtc.bustimetable.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.ksrtc.bustimetable.dto.BusSearchRequest;
import com.ksrtc.bustimetable.dto.BusSearchResponse;
import com.ksrtc.bustimetable.service.RouteSearchService;

@RestController
@RequestMapping("/api/buses")
public class BusSearchController {

    private final RouteSearchService routeSearchService;

    public BusSearchController(RouteSearchService routeSearchService) {
        this.routeSearchService = routeSearchService;
    }

    @PostMapping("/search")
    public List<BusSearchResponse> searchBuses(
            @RequestBody BusSearchRequest request) {

        return routeSearchService.searchBuses(request);
    }
}
