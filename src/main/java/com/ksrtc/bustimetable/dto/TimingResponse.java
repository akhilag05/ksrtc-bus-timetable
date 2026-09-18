package com.ksrtc.bustimetable.dto;

public class TimingResponse {

    private String stopName;
    private Integer estimatedMinutesFromBoarding;
    private String estimatedArrivalTime;

    public TimingResponse() {
    }

    public TimingResponse(String stopName,
                           Integer estimatedMinutesFromBoarding,
                           String estimatedArrivalTime) {
        this.stopName = stopName;
        this.estimatedMinutesFromBoarding = estimatedMinutesFromBoarding;
        this.estimatedArrivalTime = estimatedArrivalTime;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public Integer getEstimatedMinutesFromBoarding() {
        return estimatedMinutesFromBoarding;
    }

    public void setEstimatedMinutesFromBoarding(Integer estimatedMinutesFromBoarding) {
        this.estimatedMinutesFromBoarding = estimatedMinutesFromBoarding;
    }

    public String getEstimatedArrivalTime() {
        return estimatedArrivalTime;
    }

    public void setEstimatedArrivalTime(String estimatedArrivalTime) {
        this.estimatedArrivalTime = estimatedArrivalTime;
    }
}