package flights.model.events;

import flights.model.Flight;
import flights.model.PilotContext;

public class FlightplanEvent extends BaseEvent {

    public FlightplanEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getPosition().getReportId(), "flight/flightplan");
    }
}
