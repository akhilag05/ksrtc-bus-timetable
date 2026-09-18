package com.ksrtc.bustimetable.service;

import java.util.List;

import com.ksrtc.bustimetable.dto.BusSearchRequest;
import com.ksrtc.bustimetable.dto.BusSearchResponse;

public interface RouteSearchService {

	List<BusSearchResponse> searchBuses(BusSearchRequest request);
}