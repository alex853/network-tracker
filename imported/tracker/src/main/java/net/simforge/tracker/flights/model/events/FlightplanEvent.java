package net.simforge.tracker.flights.model.events;

import net.simforge.tracker.flights.model.Flight;
import net.simforge.tracker.flights.model.PilotContext;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getPosition().getReportId(), "flight/flightplan");
    }
}
