package net.simforge.networkview.flights_v2;

import net.simforge.networkview.core.report.ReportUtils;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportOpsService;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.core.report.persistence.ReportSessionManager;
import net.simforge.networkview.flights.Flightplan;
import net.simforge.networkview.flights.Position;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws IOException {
        // load pilot track for some range
        // determine online/offline/takeoff/landing/touch&go events
        // determine ranges
        //    on ground
        //    flying
        // split ranges by abrupt movements (jumps) (ie jump event)
        // callsign change on ground requires to split range (flightplan change too if some defined plan is changed into another)

        // on ground section BEFORE takeoff - taxi-out section
        // is being determined as short (10-20 mins) on ground section SINCE first movement
        // on ground section AFTER landing - taxi-in section
        // is being determined as short (10-20 mins) on ground section TILL end of movement

        // on ground section between LANDING and then TAKEOFF without any sign of callsign or flightplan change
        // or taxi-out/taxi-in sections should be splitted by 2 halves

        // flight is sequence of sections - taxi out section, flying section, taxi in section and set of events

//        int pilotNumber = 1138460;
//        int pilotNumber = 1001634;
//        int pilotNumber = 913904;
        int pilotNumber = 1283157;

        ReportSessionManager reportSessionManager = new ReportSessionManager();
//        ReportOpsService service = new BaseReportOpsService(reportSessionManager, Network.VATSIM);
//        ReportOpsService service = new BaseReportOpsService(reportSessionManager, Network.VATSIM, 2020);
//        ReportOpsService service = new ReportOpsServiceCsvDatasourceWrapper("./src/test/resources/snapshots/pilot-913904_2018-11-23.csv");
        ReportOpsService service = new ReportOpsServiceCsvDatasourceWrapper("./src/test/resources/snapshots/pilot-1283157_from-2015182747_amount-250.csv");
        List<Report> reports = service.loadAllReports();
        List<ReportPilotPosition> reportPilotPositions = service.loadPilotPositions(pilotNumber);
        Map<String, ReportPilotPosition> reportPilotPositionByReport = reportPilotPositions.stream().collect(Collectors.toMap(p -> p.getReport().getReport(), Function.identity()));
        reportSessionManager.dispose();

        Track track = new Track();

        for (Report report : reports) {
            ReportPilotPosition reportPilotPosition = reportPilotPositionByReport.get(report.getReport());
            Position position = reportPilotPosition != null ? Position.create(reportPilotPosition) : Position.createOfflinePosition(report);
            track.add(position);
        }

        track.recognizeTakeoffsLandings();
        track.recognizeTouchAndGoes();


        track.printEvents();
    }

    private static class Track {
        private List<Position> trackData = new ArrayList<>();
        private Map<String, Set<EventType>> eventsByTime = new TreeMap<>();

        public void add(Position position) {
            trackData.add(position);
        }

        public void recognizeTakeoffsLandings() {
            for (int i = 1; i < trackData.size(); i++) {
                Position prevPosition = trackData.get(i - 1);
                Position position = trackData.get(i);

//            boolean wentOnline = !prevPosition.isPositionKnown() && position.isPositionKnown();
//            boolean wentOffline = prevPosition.isPositionKnown() && !position.isPositionKnown();
                boolean bothPositionsKnown = prevPosition.isPositionKnown() && position.isPositionKnown();

                boolean takeoff = bothPositionsKnown && prevPosition.isOnGround() && !position.isOnGround();
                boolean landing = bothPositionsKnown && !prevPosition.isOnGround() && position.isOnGround();

                if (takeoff) {
                    Set<EventType> events = this.eventsByTime.computeIfAbsent(
                            prevPosition.getReportInfo().getReport(),
                            set -> new TreeSet<>());
                    events.add(EventType.Takeoff);
                }

                if (landing) {
                    Set<EventType> eventList = this.eventsByTime.computeIfAbsent(
                            position.getReportInfo().getReport(),
                            set -> new TreeSet<>());
                    eventList.add(EventType.Landing);
                }
            }
        }

        public void recognizeTouchAndGoes() {
            for (Map.Entry<String, Set<EventType>> entry : eventsByTime.entrySet()) {
                Set<EventType> events = entry.getValue();
                if (events.contains(EventType.Takeoff) && events.contains(EventType.Landing)) {
                    events.remove(EventType.Takeoff);
                    events.remove(EventType.Landing);
                    events.add(EventType.TouchAndGo);
                }
            }
        }

        public void printEvents() {
            for (Map.Entry<String, Set<EventType>> entry : eventsByTime.entrySet()) {
                Position position = trackDataByReport(entry.getKey());
                System.out.println(ReportUtils.log(entry.getKey())
                        + " -> " + entry.getValue()
                        + " | " + position.getStatus()
                        + " " + Flightplan.fromPosition(position));
            }
        }

        private Position trackDataByReport(String report) {
            for (Position position : trackData) {
                if (position.getReportInfo().getReport().equals(report)) {
                    return position;
                }
            }
            return null;
        }
    }

    private enum EventType {
        Takeoff,
        Landing,
        TouchAndGo
    }

    private static class Flight {
        private int pilotNumber;
        private String status;
        private String callsign;
        private String aircraftType;
        private String aircraftRegNo;
        private String origin;
        private String destination;
        private String plannedOrigin;
        private String plannedDestination;
        private Double distanceFlown;
        private Double flownTime;
        private Double networkTime;
        private List<EventInfo> events;
    }

    private static class EventInfo {
        private EventType eventType;
        private String report;
    }
}
