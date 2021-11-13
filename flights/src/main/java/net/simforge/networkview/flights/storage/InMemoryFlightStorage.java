package net.simforge.networkview.flights.storage;

import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.flights.flight.Flight1;
import net.simforge.networkview.flights.Flight1Util;
import net.simforge.networkview.core.report.ReportRange;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryFlightStorage implements FlightStorageService {
    private Map<Integer, List<Flight1>> flights = new HashMap<>();

    @Override
    public Collection<Flight1> loadFlights(int pilotNumber, ReportInfo fromReport, ReportInfo toReport) {
        List<Flight1> flights = this.flights.get(pilotNumber);
        if (flights == null) {
            return new ArrayList<>();
        }

        return Flight1Util.findOverlappedFlights(ReportRange.between(fromReport, toReport), flights)
                .stream().map(this::copy).collect(Collectors.toList());
    }

    @Override
    public void saveFlight(Flight1 flight) {
        Flight1 anotherFlight = find(flight);
        if (anotherFlight != null) {
            throw new IllegalArgumentException("There is already the same flight saved");
        }

        List<Flight1> flights = this.flights.computeIfAbsent(flight.getPilotNumber(), l -> new ArrayList<>());
        flights.add(copy(flight));
    }

    @Override
    public void deleteFlight(Flight1 flight) {
        Flight1 anotherFlight = find(flight);
        if (anotherFlight == null) {
            return;
        }

        this.flights.get(flight.getPilotNumber()).remove(anotherFlight);
    }

    private Flight1 find(Flight1 flight) {
        List<Flight1> flights = this.flights.get(flight.getPilotNumber());
        if (flights == null) {
            return null;
        }

        for (Flight1 anotherFlight : flights) {
            if (anotherFlight.getTakeoff().getReportInfo().getReport().equals(flight.getTakeoff().getReportInfo().getReport())) {
                return anotherFlight;
            }
        }

        return null;
    }

    private Flight1 copy(Flight1 flight) {
        Flight1 copy = new Flight1();
        copy.setPilotNumber(flight.getPilotNumber());
        copy.setTakeoff(flight.getTakeoff());
        copy.setLanding(flight.getLanding());
        return copy;
    }
}
