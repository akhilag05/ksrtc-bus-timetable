package com.ksrtc.bustimetable.dto;

import java.util.List;

public class BusSearchResponse {

    private Long busServiceId;

    private String officialOrigin;

    private String officialDestination;

    private String destination;

    private String boardingPoint;

    private String departureTime;

    private String serviceClass;

    private List<RouteStopResponse> stops;

    private List<TimingResponse> timings;

    public BusSearchResponse() {
    }

    public Long getBusServiceId() {
        return busServiceId;
    }

    public void setBusServiceId(Long busServiceId) {
        this.busServiceId = busServiceId;
    }

    public String getOfficialOrigin() {
        return officialOrigin;
    }

    public void setOfficialOrigin(String officialOrigin) {
        this.officialOrigin = officialOrigin;
    }

    public String getOfficialDestination() {
        return officialDestination;
    }

    public void setOfficialDestination(String officialDestination) {
        this.officialDestination = officialDestination;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getBoardingPoint() {
        return boardingPoint;
    }

    public void setBoardingPoint(String boardingPoint) {
        this.boardingPoint = boardingPoint;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public List<RouteStopResponse> getStops() {
        return stops;
    }

    public void setStops(List<RouteStopResponse> stops) {
        this.stops = stops;
    }

    public List<TimingResponse> getTimings() {
        return timings;
    }

    public void setTimings(List<TimingResponse> timings) {
        this.timings = timings;
    }
}