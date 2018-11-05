package net.simforge.networkview.flights3.events;

import net.simforge.networkview.flights3.PilotContext;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(PilotContext pilotContext) {
        super(pilotContext.getPilotNumber(), pilotContext.getCurrPosition().getReport(), "flight/flightplan");
    }

}
