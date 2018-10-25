package net.simforge.tracker.webapp.dto;

import java.util.List;

public class NetworkStatusDto {
    private String network;
    private String currentReport;

    // CODE     TIME THRESHOLD
    // OK       Up to 5 mins from now
    // GAP      Up to 15 mins from now
    // OUTDATED Higher
    private String currentStatusCode;
    private String currentStatusMessage;
    private String currentStatusDetails;

    private List<PilotPositionDto> pilotPositions;


    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getCurrentReport() {
        return currentReport;
    }

    public void setCurrentReport(String currentReport) {
        this.currentReport = currentReport;
    }

    public String getCurrentStatusCode() {
        return currentStatusCode;
    }

    public void setCurrentStatusCode(String currentStatusCode) {
        this.currentStatusCode = currentStatusCode;
    }

    public String getCurrentStatusMessage() {
        return currentStatusMessage;
    }

    public void setCurrentStatusMessage(String currentStatusMessage) {
        this.currentStatusMessage = currentStatusMessage;
    }

    public String getCurrentStatusDetails() {
        return currentStatusDetails;
    }

    public void setCurrentStatusDetails(String currentStatusDetails) {
        this.currentStatusDetails = currentStatusDetails;
    }

    public List<PilotPositionDto> getPilotPositions() {
        return pilotPositions;
    }

    public void setPilotPositions(List<PilotPositionDto> pilotPositions) {
        this.pilotPositions = pilotPositions;
    }
}
