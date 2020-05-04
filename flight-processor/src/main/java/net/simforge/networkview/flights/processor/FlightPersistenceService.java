package net.simforge.networkview.flights.processor;

import net.simforge.networkview.datafeeder.ReportInfo;
import net.simforge.networkview.flights.processor.dto.FlightDto;
import net.simforge.networkview.flights.processor.dto.PilotContextDto;

public interface FlightPersistenceService {
    ReportInfo loadLastProcessedReport();

    void saveLastProcessedReport(ReportInfo report);

    PilotContextDto loadPilotContextInfo(int pilotNumber);

    void upsertFlight(FlightDto flight);

    void upsertPilotContextInfo(PilotContextDto pilotContext);
}
