package net.simforge.networkview.flights3.events;

import net.simforge.networkview.datafeeder.ReportInfo;
import net.simforge.networkview.flights2.flight.FlightStatus;

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
