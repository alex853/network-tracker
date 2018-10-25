package net.simforge.networkview.flights.model.events;

import net.simforge.networkview.flights.model.Flight;
import net.simforge.networkview.flights.model.PilotContext;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getPosition().getReportId(), "flight/flightplan");
    }
}
