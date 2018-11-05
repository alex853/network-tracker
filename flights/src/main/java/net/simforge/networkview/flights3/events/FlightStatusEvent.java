package net.simforge.networkview.flights3.events;

import net.simforge.networkview.flights3.Flight;
import net.simforge.networkview.flights3.PilotContext;

public class FlightStatusEvent extends BaseEvent {

    private Flight flight;

    public FlightStatusEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getCurrPosition().getReport(), "flight/status/" + flight.getStatus().toString());
        this.flight = flight;
    }

    public Flight getFlight() {
        return flight;
    }

}
