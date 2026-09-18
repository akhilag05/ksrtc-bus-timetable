package com.ksrtc.bustimetable.importer;

public class PdfBusRow {

    private Integer rowNumber;
    private String from;
    private String to;
    private String serviceClass;
    private String viaPlaces;
    private String departureTime;

    public PdfBusRow() {
    }

    public PdfBusRow(
            Integer rowNumber,
            String from,
            String to,
            String serviceClass,
            String viaPlaces,
            String departureTime) {

        this.rowNumber = rowNumber;
        this.from = from;
        this.to = to;
        this.serviceClass = serviceClass;
        this.viaPlaces = viaPlaces;
        this.departureTime = departureTime;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getViaPlaces() {
        return viaPlaces;
    }

    public void setViaPlaces(String viaPlaces) {
        this.viaPlaces = viaPlaces;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    @Override
    public String toString() {
        return "PdfBusRow{" +
                "rowNumber=" + rowNumber +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", serviceClass='" + serviceClass + '\'' +
                ", viaPlaces='" + viaPlaces + '\'' +
                ", departureTime='" + departureTime + '\'' +
                '}';
    }
}