package net.simforge.networkview.flights.method.eventbased;

import net.simforge.commons.misc.Str;
import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportOps;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.core.report.persistence.ReportSessionManager;
import org.hibernate.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class BruteforceProcessingTask {
    public static void main(String[] args) throws IOException {
        // find first report
        // load report positions
        // make list of pilots
        // iterate through pilots
        // for each pilot:
        //     load all available positions
        //     process it
        //     save results to storage

        // after restart
        // do the same
        // when saving results to storage
        // do upsertion of existing data

        // upsertion works in that way
        // event - exists with same time/type - ok, skip
        //         else - save it

        ReportSessionManager reportSessionManager = new ReportSessionManager();

        DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

        File root = new File("./results/" + System.currentTimeMillis());
        root.mkdirs();

        Report firstReport;
        try (Session session = reportSessionManager.getSession(Network.VATSIM)) {
            firstReport = ReportOps.loadFirstReport(session);
        }

        Set<Integer> processedPilotNumbers = new TreeSet<>();

        Report report = firstReport;
        while (report != null) {

            List<ReportPilotPosition> pilotPositions;
            try (Session session = reportSessionManager.getSession(Network.VATSIM)) {
                pilotPositions = ReportOps.loadPilotPositions(session, report);
            }

            Set<Integer> pilotNumbersInReport = pilotPositions.stream().map(ReportPilotPosition::getPilotNumber).collect(Collectors.toSet());
            Set<Integer> pilotsToProcess = pilotNumbersInReport.stream().filter(p -> !processedPilotNumbers.contains(p)).collect(Collectors.toSet());

            System.out.println("Report " + report.getDt() + " - pilots to process " + pilotsToProcess.size() + ", total pilots " + pilotNumbersInReport.size());

            long lastStatusTs = System.currentTimeMillis();
            int counter = 0;

            for (Integer pilotNumber : pilotsToProcess) {
                PilotContext context = new PilotContext(pilotNumber);

                try (Session session = reportSessionManager.getSession(Network.VATSIM)) {
                    List<ReportPilotPosition> allPositions = loadPilotPositions(session, pilotNumber);

                    for (ReportPilotPosition position : allPositions) {
                        context = context.processPosition(position.getReport(), position);
                    }
                }

                List<Flight> flights = new ArrayList<>(context.getRecentFlights());
                if (context.getCurrFlight() != null) {
                    flights.add(context.getCurrFlight());
                }

                File pilotReportFile = new File(root, pilotNumber + ".txt");
                FileWriter writer = new FileWriter(pilotReportFile, true);
//                    System.out.println("Pilot " + pilotNumber);
                for (Flight flight : flights) {
                    String status = Str.al(Str.limit(flight.getStatus().name(), 9), 9);
                    String dof = flight.getFirstSeen().getReportInfo().getDt().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    String depTime = flight.getTakeoff() != null
                            ? flight.getTakeoff().getReportInfo().getDt().format(HHMM)
                            : flight.getFirstSeen().getReportInfo().getDt().format(HHMM);
                    String arrTime = flight.getLanding() != null
                            ? flight.getLanding().getReportInfo().getDt().format(HHMM)
                            : flight.getLastSeen().getReportInfo().getDt().format(HHMM);
                    String depIcao = Str.ar(Str.limit(flight.getTakeoff() != null ? flight.getTakeoff().getStatus() : flight.getFirstSeen().getStatus(), 6), 6);
                    String arrIcao = Str.al(Str.limit(flight.getLanding() != null ? flight.getLanding().getStatus() : flight.getLastSeen().getStatus(), 6), 6);
                    String fp = flight.getFlightplan() != null ? flight.getFlightplan().toString() : "NO F/P";


                    String line = status + " | " + dof + " | " + depTime + " | " + depIcao + "->" + arrIcao + " | " + arrTime + " | " + fp;
                    writer.append(line + "\r\n");
//                        System.out.println(line);
                }
                writer.close();

                processedPilotNumbers.add(pilotNumber);

                counter++;
                long now = System.currentTimeMillis();
                if (now - lastStatusTs > 10000) {
                    System.out.println("        " + counter + " done");
                    lastStatusTs = now;
                }
            }


            try (Session session = reportSessionManager.getSession(Network.VATSIM)) {
                report = ReportOps.loadNextReport(session, report.getReport());
            }
        }

        reportSessionManager.dispose();
    }

    private static List<ReportPilotPosition> loadPilotPositions(Session session, Integer pilotNumber) {
        return session
                .createQuery("from ReportPilotPosition where pilotNumber = :pilotNumber order by report.id")
                .setInteger("pilotNumber", pilotNumber)
                .list();
    }
}
