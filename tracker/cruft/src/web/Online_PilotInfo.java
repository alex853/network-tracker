package web;

import entities.ReportPilotPosition;

import java.util.Map;
import java.util.HashMap;

public class Online_PilotInfo {
    private int pilotId;
    private String callsign;
    private Map<Integer, ReportPilotPosition> positions = new HashMap<Integer, ReportPilotPosition>();
    private int maxReportId;

    public Online_PilotInfo(int pilotId, String callsign) {
        this.pilotId = pilotId;
        this.callsign = callsign;
    }

    public void putReportPosition(ReportPilotPosition position) {
        int eachReportId = position.getReportId();
        if (eachReportId > maxReportId) {
            maxReportId = eachReportId;
        }
        positions.put(eachReportId, position);
    }

    public int getPilotId() {
        return pilotId;
    }

    public String getCallsign() {
        return callsign;
    }

    public ReportPilotPosition getPosition(int reportId) {
        return positions.get(reportId);
    }

    public ReportPilotPosition getLastPosition() {
        return positions.get(maxReportId);
    }
}
