package com.ksrtc.bustimetable.importer;

public class ImportSummary {

    private int pdfRows;
    private int routesCreated;
    private int routesReused;
    private int servicesCreated;
    private int servicesSkipped;

    public ImportSummary() {
    }

    public int getPdfRows() {
        return pdfRows;
    }

    public void setPdfRows(int pdfRows) {
        this.pdfRows = pdfRows;
    }

    public int getRoutesCreated() {
        return routesCreated;
    }

    public void setRoutesCreated(int routesCreated) {
        this.routesCreated = routesCreated;
    }

    public int getRoutesReused() {
        return routesReused;
    }

    public void setRoutesReused(int routesReused) {
        this.routesReused = routesReused;
    }

    public int getServicesCreated() {
        return servicesCreated;
    }

    public void setServicesCreated(int servicesCreated) {
        this.servicesCreated = servicesCreated;
    }

    public int getServicesSkipped() {
        return servicesSkipped;
    }

    public void setServicesSkipped(int servicesSkipped) {
        this.servicesSkipped = servicesSkipped;
    }
}