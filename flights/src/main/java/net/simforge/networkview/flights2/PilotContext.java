package net.simforge.networkview.flights2;

import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;

import java.util.List;

public class PilotContext {
    private String lastProcessedReport;
    private List<Flight> flights; // ordered by first seen date/time, last 3 days flights
    private Flight currentFlight; // if any, can be null
    private List<Object> changes; // changes to persist
    private int pilotNumber;

    public int getPilotNumber() {
        return pilotNumber;
    }

    public boolean isActive() {
        throw new UnsupportedOperationException("PilotContext.isActive");
    }

    /**
     * It processes pilot position and returns new pilot context.
     * The new context will have updated flights and some changes that have to be persisted.
     */
    public PilotContext processPosition(Report report, ReportPilotPosition reportPilotPosition) {
        throw new UnsupportedOperationException("PilotContext.processPosition");
    }

    public List<Object> getChanges() {
        throw new UnsupportedOperationException("PilotContext.getChanges");
    }

}
