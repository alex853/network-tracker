package flights.model.criteria;

import flights.model.Flight;
import net.simforge.tracker.tools.ParsingLogics;
import net.simforge.tracker.world.Position;
import net.simforge.tracker.world.ReferenceData;

public class LostFlightEnduranceCriterion implements Criterion {
    private Flight flight;

    public LostFlightEnduranceCriterion(Flight movement) {
        this.flight = movement;
    }

    @Override
    public void process(Position position) {
        // no/op
    }

    @Override
    public boolean meets(Position position) {
        // compare aircraft endurance and actual flight time
        // estimated flight time = flight time (time between takeoff and last seen position)
        //                       + offline time (time between last seen position and current time)
        // if this estimated flight time is above that max endurance increased by reserve buffer (1 hour)
        // than flight is moved to Terminated status

        double flightTimeHours = flight.getPilotContext().getMainContext().getTimeBetween(flight.getOrigin().getReportId(), flight.getLastSeen().getReportId());
        double offlineHours = flight.getPilotContext().getMainContext().getTimeBetween(flight.getLastSeen().getReportId(), position.getReportId());
        double totalTimeHours = flightTimeHours + offlineHours;

        // todo flight.getFlightplan() as a source
        // todo what if no fpAircraft known ?
        String fpAircraft = "A320/G";
        String aircraftType = ParsingLogics.parseAircraftType(fpAircraft);
        double maxEnduranceHours = ReferenceData.getMaxEndurance(aircraftType);

        double reserveBufferHours = 1.0;

        @SuppressWarnings({"UnnecessaryLocalVariable"})
        boolean enduranceIsOk = totalTimeHours < maxEnduranceHours + reserveBufferHours;

        return enduranceIsOk;
    }

    @Override
    public String toString() {
        return "{No Status}";
    }
}
