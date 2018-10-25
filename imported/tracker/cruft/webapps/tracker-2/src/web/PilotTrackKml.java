package web;

import net.simforge.commons.kml.KmlAltitudeMode;
import net.simforge.commons.kml.KmlOut;
import net.simforge.commons.persistence.Persistence;
import forge.commons.db.DB;
import forge.commons.Misc;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.SQLException;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.DateMidnight;
import org.joda.time.MutableDateTime;
import entities.ReportPilotPosition;
import entities.Report;
import entities.Pilot;
import tracker.Stats;
import core.UpdateStamp;
import core.PilotPosition;

/*

        <td>[Report an issue]
            <a href="pilot-track.kml?pilot=<%=pilot.getPilotNumber()%>&movementId=<%=movement.getId()%>" title="See KML track for the movement">Track</a>
            <% if (eachDate != null) { %>
            <a href="pilot-track.kml?pilot=<%=pilot.getPilotNumber()%>&date=<%=Web.urlDf.print(currDate)%>" title="See KML track for this date">Track for date</a>
            <% } %></td>


 */
public class PilotTrackKml extends HttpServlet {
    private static DateTimeFormatter hm = DateTimeFormat.forPattern("HH:mm");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Stats.track_noExc("PilotTrackKml", request);

        long started = System.currentTimeMillis();

        String pilotIdStr = request.getParameter("pilotId");
        String dateStr = request.getParameter("date");

        int pilotId = Integer.parseInt(pilotIdStr);
        DateMidnight from = DateTimeFormat.forPattern("yyyyMMdd").parseDateTime(dateStr).toDateMidnight();
        MutableDateTime mdt = from.toMutableDateTime();
        mdt.addDays(1);
        DateMidnight to = mdt.toDateTime().toDateMidnight();

        Connection connx;

        Report fromReport;
        Report toReport;
        List<Report> reports;
        List<ReportPilotPosition> positions;

        try {
            connx = DB.getConnection();

            reports = Persistence.loadWhere(
                    connx,
                    Report.class,
                    "report >= '" + UpdateStamp.toUpdate(from.toDateTime()) + "' and report <= '" + UpdateStamp.toUpdate(to.toDateTime()) + "' order by id asc");
            fromReport = reports.get(0);
            toReport = reports.get(reports.size()-1);

            if (fromReport != null && toReport != null) {
                positions = Persistence.loadWhere(
                        connx,
                        ReportPilotPosition.class,
                        "pilot_id = " + pilotId + " and report_id between " + fromReport.getId() + " and " + toReport.getId() + " order by report_id");
            } else {
                positions = null;
            }
            connx.close();
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        long loaded = System.currentTimeMillis();

        response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml");

        response.getOutputStream().print(makeKml2(pilotId, from, reports, positions));

        System.out.println("PilotTrackKml: " + (System.currentTimeMillis() - started) + "  (init, ms: " + (loaded - started) + ")");
    }

    private String makeKml2(int pilotId, DateMidnight date, List<Report> reports, List<ReportPilotPosition> positions) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        KmlOut out = new KmlOut(baos);
        out.openDocument();
        out.writeName("Pilot " + pilotId + ", date " + DateTimeFormat.forPattern("yyyy/MM/dd").print(date));

        Map<Integer, ReportPilotPosition> rep2pos = new HashMap<Integer, ReportPilotPosition>();
        for (ReportPilotPosition position : positions) {
            rep2pos.put(position.getReportId(), position);
        }

        Report currReportFrom = null;
        Report currReportTo = null;
        Pilot.State currState = null;

        for (int i = 0; i < reports.size(); i++) {
            Report eachReport = reports.get(i);
            ReportPilotPosition eachPosition = rep2pos.get(eachReport.getId());
            Pilot.State eachState = Pilot.State.get(eachPosition);

            if (currState == null && i == 0) {
                currReportFrom = eachReport;
                currReportTo = eachReport;
                currState = eachState;
            } else if (eachState == currState) {
                currReportTo = eachReport;
            } else {
                addSegment(out, currState, currReportFrom, currReportTo, positions);
                currReportFrom = eachReport;
                currReportTo = eachReport;
                currState = eachState;
            }
        }

        addSegment(out, currState, currReportFrom, currReportTo, positions);

        out.closeDocument();
        out.closeKml();
        baos.close();

        return baos.toString();
    }

    private void addSegment(KmlOut out, Pilot.State state, Report from, Report to, List<ReportPilotPosition> positions) throws IOException {
        String interval = hm.print(from.getReportDt()) + " - " + hm.print(to.getReportDt());

        out.openFolder();
        out.writeName(interval + " " + state.name());

        if (state != Pilot.State.Offline) {
            addTrack(out, from, to, positions);
        }

        out.closeFolder();
    }

    private void addTrack(KmlOut out, Report from, Report to, List<ReportPilotPosition> positions) throws IOException {
        for (ReportPilotPosition position : positions) {
            if (position.getReportId() < from.getId()) {
                continue;
            }
            if (to != null && position.getReportId() > to.getId()) {
                break;
            }
            drawAircraft(out, position);
        }
    }

    private String makeKml(int pilotId, DateMidnight date, List<Report> reports, List<ReportPilotPosition> positions) throws IOException {
        Map<Integer, ReportPilotPosition> rep2pos = new HashMap<Integer, ReportPilotPosition>();
        for (ReportPilotPosition position : positions) {
            rep2pos.put(position.getReportId(), position);
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        KmlOut out = new KmlOut(baos);
        out.openDocument();
        out.writeName("Pilot " + pilotId + ", date " + DateTimeFormat.forPattern("yyyy/MM/dd").print(date));

        DateTimeFormatter dtf = Misc.yMdHms;

        for (int i = 0; i < reports.size();) {
            Report reportFrom = reports.get(i);
            ReportPilotPosition positionFrom = rep2pos.get(reportFrom.getId());

            Report reportTo = reportFrom;
            int j = i + 1;
            for (; j < reports.size(); j++) {
                Report eachReport = reports.get(j);
                ReportPilotPosition eachPosition = rep2pos.get(eachReport.getId());

                if (positionFrom == null) {
                    if (eachPosition == null) {
                        reportTo = eachReport;
                    } else {
                        break;
                    }
                } else {
                    if (eachPosition != null) {
                        reportTo = eachReport;
                    } else {
                        break;
                    }
                }
            }

            out.openFolder();
            out.writeName((positionFrom != null ? "ONLINE" : "offline") + " " + dtf.print(reportFrom.getReportDt()) + " - " + dtf.print(reportTo.getReportDt()));

            for (int k = i; k < j; k++) {
                Report report = reports.get(k);
                ReportPilotPosition position = rep2pos.get(report.getId());

                out.openFolder();
                out.writeName(dtf.print(report.getReportDt()) + " " + (position != null ? "ONLINE" : "offline"));

                if (position != null) {
                    drawAircraft(out, position);
                }

                out.closeFolder();
            }

            out.closeFolder();

            i = j;
        }

        out.closeDocument();
        out.closeKml();
        baos.close();

        return baos.toString();
    }

    private static void drawAircraft(KmlOut out, ReportPilotPosition position) throws IOException {
        PilotPosition aPosition = new PilotPosition(position);

        out.openPlacemark();
        out.writeName(aPosition.getActualFL());
        out.writeDescription(
                "<![CDATA[" +
                "<br>" +
                "Rep alt: " + position.getAltitude() + "<br>" +
                "Rep QNH: " + position.getQnhMb() + "<br>" +
                "Alt Mode: " + aPosition.getAltimeterMode() + "<br>" +
                "Actual alt: " + aPosition.getActualAltitude() + "<br>" +
                "Actual FL: " + aPosition.getActualFL() + "<br>" +
                "]]>");

        out.openPoint();
        out.writeExtrude(true);
        out.writeAltitudeMode(KmlAltitudeMode.absolute);
        out.openCoordinates();
        out.writeCoordinates(position.getLongitude(), position.getLatitude(), altitudeToKml(aPosition.getActualAltitude()));
        out.closeCoordinates();
        out.closePoint();

        out.closePlacemark();
    }

    private static final double ALT_COEFF = 3;

    private static int altitudeToKml(int altitude) {
        return (int) (ALT_COEFF * altitude / 3.28);
    }
}
