package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.PilotContext;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getCurrPosition().getReport(), "flight/flightplan");
    }

    static {
        TrackingEventHandler.registry.put(FlightplanEvent.class,
                (TrackingEventHandler<FlightplanEvent>) (delegate, event) -> { /* no op */ });
        // todo if aircraft is on ground in status Arrival and Flightplan has changed - we need to finish flight and start new one
    }

}
