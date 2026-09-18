package com.ksrtc.bustimetable.dto;

public class RouteStopResponse {

    private String stopName;
    private Integer stopOrder;
    private Integer estimatedMinutesFromBoarding;

    public RouteStopResponse() {
    }

    public RouteStopResponse(String stopName, Integer stopOrder,
                             Integer estimatedMinutesFromBoarding) {
        this.stopName = stopName;
        this.stopOrder = stopOrder;
        this.estimatedMinutesFromBoarding = estimatedMinutesFromBoarding;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public Integer getEstimatedMinutesFromBoarding() {
        return estimatedMinutesFromBoarding;
    }

    public void setEstimatedMinutesFromBoarding(Integer estimatedMinutesFromBoarding) {
        this.estimatedMinutesFromBoarding = estimatedMinutesFromBoarding;
    }
}