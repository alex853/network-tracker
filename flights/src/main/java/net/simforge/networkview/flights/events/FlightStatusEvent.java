package net.simforge.networkview.flights.events;

import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.flights.FlightStatus;

public class FlightStatusEvent extends BaseEvent {

    private FlightStatus status;

    public FlightStatusEvent(int pilotNumber, ReportInfo reportInfo, FlightStatus status) {
        super(pilotNumber, reportInfo, "flight/status/" + status.toString());
        this.status = status;
    }

    public FlightStatus getStatus() {
        return status;
    }
}
