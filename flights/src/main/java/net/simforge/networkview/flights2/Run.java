package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class Run {
    public static void main(String[] args) throws IOException {
        PersistenceLayer persistenceLayer = new PersistenceLayer1();
        ReportDatasource reportDatasource = new CsvDatasource(Csv.empty());

        Map<Integer, PilotContext> pilotContextMap = new HashMap<>();
        // todo load pilot contexts updated in last N hours

        String lastReport = null;
        while (true) {
            Report report = reportDatasource.loadNextReport(lastReport);
            if (report == null) {
                break;
            }

            List<ReportPilotPosition> reportPilotPositions = reportDatasource.loadPilotPositions(report.getId());

            for (ReportPilotPosition reportPilotPosition : reportPilotPositions) {
                Integer pilotNumber = reportPilotPosition.getPilotNumber();
                PilotContext pilotContext = pilotContextMap.get(pilotNumber);
                if (pilotContext == null) {
                    pilotContext = persistenceLayer.loadContext(pilotNumber);
                }
                if (pilotContext == null) {
                    pilotContext = persistenceLayer.createContext(pilotNumber);
                }

                PilotContext newPilotContext = pilotContext.processPosition(null, reportPilotPosition);
                persistenceLayer.saveChanges(newPilotContext);
            }
        }
    }
}
