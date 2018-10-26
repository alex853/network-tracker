package net.simforge.networkview.flights2;

import net.simforge.commons.io.Csv;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.datasource.ReportDatasource;

import java.io.IOException;
import java.util.*;

@Deprecated
public class Run2 {
    public static void main(String[] args) throws IOException {
        ReportDatasource reportDatasource = new CsvDatasource(Csv.empty());
        PersistenceLayer persistenceLayer = null;

        // todo load active and non-error pilot contexts

        Set<PilotContext> queue = new TreeSet<>();



        // for each report, every few mins
        Report report = null;

        List<ReportPilotPosition> reportPilotPositions = reportDatasource.loadPilotPositions(0/*report*/);
        Set<Integer> pilotNumberInReport = new TreeSet<>();
        for (ReportPilotPosition reportPilotPosition : reportPilotPositions) {

        }

        for (PilotContext pilotContext : queue) {
            pilotNumberInReport.remove(pilotContext.getPilotNumber());
        }

        for (Integer pilotNumber : pilotNumberInReport) {
            PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
            if (pilotContext == null) {
                pilotContext = persistenceLayer.createContext(pilotNumber);
            }

            queue.add(pilotContext);
        }






        Iterator<PilotContext> it = queue.iterator();

        Set<PilotContext> nextQueue = new TreeSet<>();

        while (it.hasNext()) {
            PilotContext pilotContext = it.next();

            PilotContext newPilotContext;
            //try {
            long reportTimestamp = 0;
            report = reportDatasource.loadReport(reportTimestamp);
                ReportPilotPosition reportPilotPosition = null;//reportDatasource.loadPilotPosition(report, pilotContext.getPilotNumber());
                PilotContext dirtyPilotContext = pilotContext.processPosition(null, reportPilotPosition);
                newPilotContext = persistenceLayer.saveChanges(dirtyPilotContext);
            //} catch(??? e) {
                newPilotContext = persistenceLayer.saveChanges(pilotContext/*.errorOccured()*/);
            //}

            if (true/*newPilotContext.isRecent() && !newPilotContext.tooManyErrors()*/) {
                nextQueue.add(newPilotContext);
            }
        }
    }
}
