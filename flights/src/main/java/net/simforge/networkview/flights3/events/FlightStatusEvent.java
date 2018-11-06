package net.simforge.networkview.flights3.events;

import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights3.Flight;

public class FlightStatusEvent extends BaseEvent {

    public FlightStatusEvent(int pilotNumber, String report, FlightStatus status) {
        super(pilotNumber, report, "flight/status/" + status.toString());
    }

    public Flight getFlight() {
        throw new UnsupportedOperationException("FlightStatusEvent.getFlight");
    }

}
