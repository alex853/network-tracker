package net.simforge.networkview.flights2.events;

import net.simforge.networkview.flights2.flight.Flight;
import net.simforge.networkview.flights2.PilotContext;

public class FlightStatusEvent extends BaseEvent {

    private Flight flight;

    public FlightStatusEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getLastProcessedReport(), "flight/status/" + flight.getStatus().toString());
        this.flight = flight;
    }

    public Flight getFlight() {
        return flight;
    }

    static {
        TrackingEventHandler.registry.put(FlightStatusEvent.class,
                (TrackingEventHandler<FlightStatusEvent>) (delegate, event) -> { /* no op */ });
    }

}
