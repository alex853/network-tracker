package net.simforge.networkview.flights.method.rangebased;

import net.simforge.networkview.core.report.ReportInfo;

import java.util.HashMap;
import java.util.Map;

public class ProcessorPOCStatusServiceStub implements ProcessorPOCStatusService {
    private ReportInfo lastProcessedReport;
    private Map<Integer, PilotContext> pilotContexts = new HashMap<>();

    @Override
    public PilotContext loadPilotContext(int pilotNumber) {
        PilotContext pilotContext = pilotContexts.get(pilotNumber);
        if (pilotContext != null) {
            return pilotContext.makeCopy();
        } else {
            return null;
        }
    }

    @Override
    public PilotContext createPilotContext(int pilotNumber) {
        PilotContext pilotContext = loadPilotContext(pilotNumber);
        if (pilotContext != null) {
            throw new IllegalArgumentException("Pilot context for pilot " + pilotNumber + " exists");
        }
        pilotContext = new PilotContext(pilotNumber);
        pilotContexts.put(pilotNumber, pilotContext.makeCopy());
        return pilotContext;
    }

    @Override
    public void savePilotContext(PilotContext pilotContext) {
        pilotContexts.put(pilotContext.getPilotNumber(), pilotContext.makeCopy());
    }

    @Override
    public ReportInfo loadLastProcessedReport() {
        return lastProcessedReport;
    }

    @Override
    public void saveLastProcessedReport(ReportInfo report) {
        this.lastProcessedReport = report;
    }
}
