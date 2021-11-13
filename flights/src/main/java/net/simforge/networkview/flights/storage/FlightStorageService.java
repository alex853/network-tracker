package net.simforge.networkview.flights.storage;

import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.flights.flight.Flight1;

import java.util.Collection;

public interface FlightStorageService {
    Collection<Flight1> loadFlights(int pilotNumber, ReportInfo fromReport, ReportInfo toReport);

    void saveFlight(Flight1 flight);

    void deleteFlight(Flight1 flight);
}
