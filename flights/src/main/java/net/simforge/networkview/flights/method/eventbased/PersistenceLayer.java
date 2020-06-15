package net.simforge.networkview.flights.method.eventbased;

import net.simforge.networkview.core.report.persistence.Report;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface PersistenceLayer {
    List<PilotContext> loadActivePilotContexts(LocalDateTime lastProcessedReportDt) throws IOException; // todo remove exception

    PilotContext createContext(int pilotNumber, Report seenReport);

    PilotContext loadContext(int pilotNumber) throws IOException; // todo remove exception

    PilotContext saveChanges(PilotContext pilotContext);
}
