package flights.model.events;

import flights.model.Flight;
import flights.model.PilotContext;

public class FlightStatusEvent extends BaseEvent {

    private Flight flight;

    public FlightStatusEvent(PilotContext pilotContext, Flight flight) {
        super(pilotContext.getPilotNumber(), pilotContext.getPosition().getReportId(), "flight/status/" + flight.getStatus().toString());
        this.flight = flight;
    }

    public Flight getFlight() {
        return flight;
    }
}
