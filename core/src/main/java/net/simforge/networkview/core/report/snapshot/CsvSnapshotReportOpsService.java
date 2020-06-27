package net.simforge.networkview.core.report.snapshot;

import net.simforge.commons.io.Csv;
import net.simforge.networkview.core.report.ReportInfo;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportOpsService;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;

import java.util.*;
import java.util.stream.Collectors;

public class CsvSnapshotReportOpsService implements ReportOpsService {

    private static final String REPORT_ID = "Report.id";
    private static final String REPORT_VERSION = "Report.version";
    private static final String REPORT_REPORT = "Report.report";
    private static final String REPORT_CLIENTS = "Report.clients";
    private static final String REPORT_PILOTS = "Report.pilots";
    private static final String REPORT_HAS_LOGS = "Report.hasLogs";
    private static final String REPORT_PARSED = "Report.parsed";

    private static final String POSITION_ID = "Position.id";
    private static final String POSITION_VERSION = "Position.version";
    private static final String POSITION_PILOT_NUMBER = "Position.pilotNumber";
    private static final String POSITION_CALLSIGN = "Position.callsign";
    private static final String POSITION_ALTITUDE = "Position.altitude";
    private static final String POSITION_GROUNDSPEED = "Position.groundspeed";
    private static final String POSITION_HEADING = "Position.heading";
    private static final String POSITION_QNH_MB = "Position.qnhMb";
    private static final String POSITION_FP_AIRCRAFT = "Position.fpAircraft";
    private static final String POSITION_FP_DEP = "Position.fpDep";
    private static final String POSITION_FP_DEST = "Position.fpDest";
    private static final String POSITION_PARSED_REG_NO = "Position.parsedRegNo";
    private static final String POSITION_LATITUDE = "Position.latitude";
    private static final String POSITION_LONGITUDE = "Position.longitude";

    private List<Report> reports = new ArrayList<>();
    private Map<Long, ReportPilotPosition> positions = new HashMap<>();

    public CsvSnapshotReportOpsService(Csv csv) {
        for (int row = 0; row < csv.rowCount(); row++) {
            Report report = new Report();

            report.setId(Long.parseLong(csv.value(row, REPORT_ID)));
            report.setVersion(Integer.parseInt(csv.value(row, REPORT_VERSION)));
            report.setReport(csv.value(row, REPORT_REPORT));
            report.setClients(Integer.parseInt(csv.value(row, REPORT_CLIENTS)));
            report.setPilots(Integer.parseInt(csv.value(row, REPORT_PILOTS)));
            report.setHasLogs("Y".equals(csv.value(row, REPORT_HAS_LOGS)));
            report.setParsed("Y".equals(csv.value(row, REPORT_PARSED)));

            reports.add(report);

            boolean hasPosition = csv.rowWidth(row) > csv.columnIndex(POSITION_ID);
            if (hasPosition) {
                ReportPilotPosition position = new ReportPilotPosition();

                position.setId(Long.parseLong(csv.value(row, POSITION_ID)));
                position.setVersion(Integer.parseInt(csv.value(row, POSITION_VERSION)));
                position.setPilotNumber(Integer.parseInt(csv.value(row, POSITION_PILOT_NUMBER)));
                position.setReport(report);
                position.setCallsign(csv.value(row, POSITION_CALLSIGN));
                position.setAltitude(Integer.parseInt(csv.value(row, POSITION_ALTITUDE)));
                position.setGroundspeed(Integer.parseInt(csv.value(row, POSITION_GROUNDSPEED)));
                position.setHeading(Integer.parseInt(csv.value(row, POSITION_HEADING)));
                position.setQnhMb(Integer.parseInt(csv.value(row, POSITION_QNH_MB)));
                position.setFpAircraft(csv.value(row, POSITION_FP_AIRCRAFT));
                position.setFpOrigin(csv.value(row, POSITION_FP_DEP));
                position.setFpDestination(csv.value(row, POSITION_FP_DEST));
                position.setParsedRegNo(csv.value(row, POSITION_PARSED_REG_NO));
                position.setLatitude(Double.parseDouble(csv.value(row, POSITION_LATITUDE)));
                position.setLongitude(Double.parseDouble(csv.value(row, POSITION_LONGITUDE)));

                positions.put(position.getReport().getId(), position);
            }
        }
    }

    //==========================================================================================================================
    @Override
    public Report loadFirstReport() {
        return !reports.isEmpty() ? reports.get(0) : null;
    }

    @Override
    public Report loadNextReport(String report) {
        for (int i = 0; i < reports.size(); i++) {
            Report eachReport = reports.get(i);
            if (eachReport.getReport().equals(report)) {
                if (i < reports.size() - 1) {
                    return reports.get(i + 1);
                }
            }
        }
        return null;
    }

    @Override
    public Report loadReport(long reportId) {
        for (Report report : reports) {
            if (report.getId() == reportId) {
                return report;
            }
        }
        return null;
    }

    @Override
    public List<Report> loadAllReports() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Report> loadReports(ReportInfo sinceReport, ReportInfo tillReport) {
        return reports.stream().filter(r -> sinceReport.getId() <= r.getId() && r.getId() <= tillReport.getId()).collect(Collectors.toList());
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(ReportInfo reportInfo) {
        ReportPilotPosition pilotPosition = positions.get(reportInfo.getId());
        return pilotPosition != null ? Collections.singletonList(pilotPosition) : Collections.emptyList();
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositions(int pilotNumber) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReportPilotPosition loadPilotPosition(int pilotNumber, ReportInfo reportInfo) {
        List<ReportPilotPosition> reportPilotPositions = loadPilotPositions(reportInfo);
        return reportPilotPositions.stream().filter(p -> p.getPilotNumber() == pilotNumber).findFirst().orElse(null);
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsSinceTill(int pilotNumber, ReportInfo sinceReport, ReportInfo tillReport) {
        return positions.entrySet().stream().filter(e -> sinceReport.getId() <= e.getKey() && e.getKey() <= tillReport.getId()).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public List<ReportPilotPosition> loadPilotPositionsTill(int pilotNumber, String tillReport) {
        throw new UnsupportedOperationException();
    }

    public static void addColumns(Csv csv) {
        csv.addColumn(REPORT_ID);
        csv.addColumn(REPORT_VERSION);
        csv.addColumn(REPORT_REPORT);
        csv.addColumn(REPORT_CLIENTS);
        csv.addColumn(REPORT_PILOTS);
        csv.addColumn(REPORT_HAS_LOGS);
        csv.addColumn(REPORT_PARSED);

        csv.addColumn(POSITION_ID);
        csv.addColumn(POSITION_VERSION);
        csv.addColumn(POSITION_PILOT_NUMBER);
        csv.addColumn(POSITION_CALLSIGN);
        csv.addColumn(POSITION_ALTITUDE);
        csv.addColumn(POSITION_GROUNDSPEED);
        csv.addColumn(POSITION_HEADING);
        csv.addColumn(POSITION_QNH_MB);
        csv.addColumn(POSITION_FP_AIRCRAFT);
        csv.addColumn(POSITION_FP_DEP);
        csv.addColumn(POSITION_FP_DEST);
        csv.addColumn(POSITION_PARSED_REG_NO);
        csv.addColumn(POSITION_LATITUDE);
        csv.addColumn(POSITION_LONGITUDE);
    }

    public static void addRow(Csv csv, Report report, ReportPilotPosition position) {
        int row = csv.addRow();

        csv.set(row, REPORT_ID, String.valueOf(report.getId()));
        csv.set(row, REPORT_VERSION, String.valueOf(report.getVersion()));
        csv.set(row, REPORT_REPORT, report.getReport());
        csv.set(row, REPORT_CLIENTS, String.valueOf(report.getClients()));
        csv.set(row, REPORT_PILOTS, String.valueOf(report.getPilots()));
        csv.set(row, REPORT_HAS_LOGS, report.getHasLogs() ? "Y" : "N");
        csv.set(row, REPORT_PARSED, report.getParsed() ? "Y" : "N");

        if (position == null) {
            return;
        }

        csv.set(row, POSITION_ID, String.valueOf(position.getId()));
        csv.set(row, POSITION_VERSION, String.valueOf(position.getVersion()));
        csv.set(row, POSITION_PILOT_NUMBER, String.valueOf(position.getPilotNumber()));
        csv.set(row, POSITION_CALLSIGN, position.getCallsign());
        csv.set(row, POSITION_ALTITUDE, String.valueOf(position.getAltitude()));
        csv.set(row, POSITION_GROUNDSPEED, String.valueOf(position.getGroundspeed()));
        csv.set(row, POSITION_HEADING, String.valueOf(position.getHeading()));
        csv.set(row, POSITION_QNH_MB, String.valueOf(position.getQnhMb()));
        csv.set(row, POSITION_FP_AIRCRAFT, position.getFpAircraft());
        csv.set(row, POSITION_FP_DEP, position.getFpOrigin());
        csv.set(row, POSITION_FP_DEST, position.getFpDestination());
        csv.set(row, POSITION_PARSED_REG_NO, position.getParsedRegNo());
        csv.set(row, POSITION_LATITUDE, String.valueOf(position.getLatitude()));
        csv.set(row, POSITION_LONGITUDE, String.valueOf(position.getLongitude()));
    }

}
