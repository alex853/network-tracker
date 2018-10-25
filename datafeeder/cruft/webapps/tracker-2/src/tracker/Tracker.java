package tracker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.*;

import forge.commons.Settings;
import forge.commons.Geo;
import entities.ReportPilotPosition;
import entities.ReportFpRemarks;
import net.simforge.commons.kml.KmlAltitudeMode;
import net.simforge.commons.kml.KmlOut;
import web.Links;
import web.Web;
import web.WebCache;
import org.joda.time.DateTime;

public class Tracker extends HttpServlet {
    private static final String ROOT = Settings.get("webapps.tracker-2.root");
    private static final double ALT_COEFF = 3;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Stats.track_noExc("Tracker", request);

        long started = System.currentTimeMillis();
        long inited;

        response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml");

        TrackerData data;
        try {
            data = TrackerData.getOrLoad(getServletContext());
            inited = System.currentTimeMillis();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("db error", e);
        }

        response.getOutputStream().print(makeKml(data));

        System.out.println("Tracker processing, ms: " + (System.currentTimeMillis() - started) + "  (init, ms: " + (inited - started) + ")");
    }

    private static String makeKml(TrackerData data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        KmlOut out = new KmlOut(baos);
        out.openDocument();
        out.writeName(String.valueOf(Math.random()));

        for(int i = 0; i < 36; i++) {
            int heading = i * 10;

            out.write(
                    "<Style id=\"aircraft-flying-" + i + "\">" +
                    "<IconStyle>" +
                    "<heading>" + heading + "</heading>" +
                    "<Icon>" +
                    "<href>" + ROOT + "img/aircraft-flying.png</href>" +
                    "</Icon>" +
                    "</IconStyle>" +
                    "<LabelStyle><scale>0.8</scale></LabelStyle>" +
                    "</Style>");
            out.write(
                    "<Style id=\"aircraft-on-ground-" + i + "\">" +
                    "<IconStyle>" +
                    "<heading>" + heading + "</heading>" +
                    "<Icon>" +
                    "<href>" + ROOT + "img/aircraft-on-ground.png</href>" +
                    "</Icon>" +
                    "<scale>0.75</scale>" +
                    "</IconStyle>" +
                    "<LabelStyle><scale>0.6</scale></LabelStyle>" +
                    "</Style>");
            out.write(
                    "<Style id=\"aircraft-offline-" + i + "\">" +
                    "<IconStyle>" +
                    "<heading>" + heading + "</heading>" +
                    "<Icon>" +
                    "<href>" + ROOT + "img/aircraft-offline.png</href>" +
                    "</Icon>" +
                    "</IconStyle>" +
                    "<LabelStyle><scale>0.7</scale></LabelStyle>" +
                    "</Style>");
        }
        out.write(
                "<Style id=\"position\">" +
                "<IconStyle>" +
                "<Icon>" +
                "<href>" + ROOT + "img/position.png</href>" +
                "</Icon>" +
                "</IconStyle>" +
                "<LabelStyle><scale>0.6</scale></LabelStyle>" +
                "</Style>");

//        writeTrackStyle(out, "track", "7fff0000", "40ff0000");
//        writeTrackStyle(out, "missing", "7f0000ff", "400000ff");
//        writeTrackStyle(out, "taxi", "7f00ff00", "4000ff00");
//        writeTrackStyle(out, "jump", "7f00ffff", "4000ffff");
        writeTrackStyle(out, "track",   "7fe3ff75", "40e3ff75");
        writeTrackStyle(out, "missing", "7f0024db", "400024db");
        writeTrackStyle(out, "taxi",    "7f0048ff", "400048ff");
        writeTrackStyle(out, "jump",    "7fab62cd", "40ab62cd");

        writeTrackStyle(out, "offline", "60d0d0d0", "30d0d0d0");

//            for (Integer pilotId : data.sortedPilots) {
        for (Integer pilotId : data.getPilots().keySet()) {
            PilotData pilotData = data.getPilots().get(pilotId);

            out.openFolder();

            PilotData.Position lastPosition = null;
            if (pilotData.isNowOnline()) {
                lastPosition = pilotData.getPositions().get(0);
            } else {
                List<PilotData.Position> positions = pilotData.getPositions();
                for (PilotData.Position eachPosition : positions) {
                    if (eachPosition != null) {
                        lastPosition = eachPosition;
                        break;
                    }
                }
            }

            out.writeName(lastPosition.getPosition().getCallsign());

/*            out.openFolder();
            out.writeName("Positions");

            List<PilotData.Position> positions = pilotData.getPositions();
            for (PilotData.Position position : positions) {
                drawPosition(out, pilotData, position);
            }

            out.closeFolder();*/

            if (pilotData.isNowOnline()) {
                if (lastPosition.isOnGround())
                    drawAircraft(out, lastPosition, "#aircraft-on-ground");
                else
                    drawAircraft(out, lastPosition, "#aircraft-flying");
            } else {
                drawAircraft(out, lastPosition, "#aircraft-offline");
            }

            drawSegments(out, pilotData);

            out.closeFolder();
        }

        out.closeDocument();
        out.closeKml();
        baos.close();

        return baos.toString();
    }

    private static void drawSegments(KmlOut out, PilotData pilotData) throws IOException {
        List<PilotData.Segment> segments = pilotData.getSegments();

        out.openFolder();
        out.writeName("Track");

        String lastStyleUrl = null;
        for (PilotData.Segment segment : segments) {
            String styleUrl = "";
            String name = "";
            PilotData.SegmentType type = segment.getType();
            boolean online = pilotData.isNowOnline();
            if (type == PilotData.SegmentType.Flying) {
                styleUrl = "#track";
                name = "Flying";
            } else if (type == PilotData.SegmentType.MissingReports) {
                styleUrl = "#missing";
                name = "Missing Reports";
            } else if (type == PilotData.SegmentType.Taxi) {
                styleUrl = "#taxi";
                name = "Taxi";
            } else if (type == PilotData.SegmentType.JumpOnGround) {
                styleUrl = "#jump";
                name = "Jump On Ground";
            }
            if (!online)
                styleUrl = "#offline";

            ReportPilotPosition prevPosition = segment.getP1().getPosition();
            ReportPilotPosition currPosition = segment.getP2().getPosition();

            if (lastStyleUrl == null || !lastStyleUrl.equals(styleUrl)) {
                if (lastStyleUrl != null) {
                    out.closeCoordinates();
                    out.closeLineString();
                    out.closePlacemark();
                }

                out.openPlacemark();
                out.write("<styleUrl>" + styleUrl + "</styleUrl>");
                out.writeName(name);
                out.openLineString();
                out.writeExtrude(true);
                out.writeAltitudeMode(KmlAltitudeMode.absolute);
                out.openCoordinates();
                out.writeCoordinates(prevPosition.getLongitude(), prevPosition.getLatitude(), altitudeToKml(segment.getP1().getActualAltitude(), segment.getP1().isOnGround()));
            }

            out.writeCoordinates(currPosition.getLongitude(), currPosition.getLatitude(), altitudeToKml(segment.getP2().getActualAltitude(), segment.getP2().isOnGround()));
            lastStyleUrl = styleUrl;

/*            out.openPlacemark();
            out.write("<styleUrl>" + styleUrl + "</styleUrl>");
            out.writeName(name);
            out.openLineString();
            out.writeExtrude(true);
            out.writeAltitudeMode(KmlAltitudeMode.absolute);
            out.openCoordinates();
            out.writeCoordinates(prevPosition.getLongitude(), prevPosition.getLatitude(), altitudeToKml(segment.getP1().getActualAltitude()));
            out.writeCoordinates(currPosition.getLongitude(), currPosition.getLatitude(), altitudeToKml(segment.getP2().getActualAltitude()));
            out.closeCoordinates();
            out.closeLineString();
            out.closePlacemark();*/
        }

        if (lastStyleUrl != null) {
            out.closeCoordinates();
            out.closeLineString();
            out.closePlacemark();
        }

        out.closeFolder();
    }

    private static void drawAircraft(KmlOut out, PilotData.Position position, String styleUrl) throws IOException {
        int heading = position.getPosition().getHeading();
        int h = (int) Math.round(heading / 10.0);
        if (h == 36) {
            h = 0;
        }

        out.openPlacemark();
        out.write("<styleUrl>" + styleUrl + "-" + h + "</styleUrl>");
        out.writeName(position.getPosition().getCallsign() + "   " + position.getActualFL());
        ReportFpRemarks reportFpRemarks = WebCache.getFpRemarks(position.getPosition().getFpRemarksId());
        String fpRemarks = reportFpRemarks != null ? reportFpRemarks.getFpRemarks() : "";
        out.writeDescription(
                "<![CDATA[" +
                "<br>" +
                position.getPosition().getCallsign() + "<br>" +
                position.getActualFL() + "<br>" +
                "<br>" +
                "<b>Pilot:</b> <a href=\"" + ROOT + "pilot.jsp?pilot=" + position.getPosition().getPilotNumber() + "\">" + position.getPosition().getPilotNumber() + "</a><br>" +
                "<b>Aircraft</b>: " + ("".equals(position.getAircraft()) ? "n/a" : position.getAircraft()) + "<br>" +
                "<b>Reg</b>: " + (position.getPosition().getParsedRegNo() != null ? position.getPosition().getParsedRegNo() : "n/a") + "<br>" +
                "<br>" +
                "<b>Route</b>: from " + Links.airport(position.getPosition().getFpDep()) + " to " + Links.airport(position.getPosition().getFpDest()) + "<br>" +
                "<br>" +
                "<b>F/P remarks</b>: " + fpRemarks + "<br>" +
                "<br>" +
                "<a href=\"" + ROOT + "pilot-daily-log.jsp?pilot=" + position.getPosition().getPilotNumber() + "&day=" + Web.urlDf.print(new DateTime()) + "\">position log</a>" +
                "<br>" +
                "]]>");
        out.openPoint();
        out.writeExtrude(true);
        out.writeAltitudeMode(KmlAltitudeMode.absolute);
        out.openCoordinates();
        out.writeCoordinates(position.getPosition().getLongitude(), position.getPosition().getLatitude(), altitudeToKml(position.getActualAltitude(), position.isOnGround()));
        out.closeCoordinates();
        out.closePoint();

        double lon = position.getPosition().getLongitude();
        double lat = position.getPosition().getLatitude();
        int hdg = position.getPosition().getHeading();

        Geo.Coords coords = Geo.pointByHdgNM(lat, lon, hdg - 180, 1.0);

        out.write(
                "<Camera>\n" +
                "  <longitude>" + coords.lon + "</longitude>" +
                "  <latitude>" + coords.lat + "</latitude>" +
                "  <altitude>" + (altitudeToKml(position.getActualAltitude(), position.isOnGround()) + 800) + "</altitude>" +
                "  <heading>" + hdg + "</heading>" +
                "  <tilt>70</tilt>" +
                "  <roll>0</roll>" +
                "  <altitudeMode>absolute</altitudeMode>" +
                "</Camera> ");
        out.closePlacemark();
    }

/*    private static void drawPosition(KmlOut out, PilotData pilotData, PilotData.Position position) throws IOException {
        out.openPlacemark();
        out.write("<styleUrl>#position</styleUrl>");
        out.writeName(String.valueOf(position.getActualFL()));
        out.writeDescription(ROOT + "pilot-daily-log.jsp?pilot=" + pilotData.getNumber());
        out.openPoint();
        out.writeExtrude(true);
        out.writeAltitudeMode(KmlAltitudeMode.absolute);
        out.openCoordinates();
        out.writeCoordinates(position.getPosition().getLongitude(), position.getPosition().getLatitude(), altitudeToKml(position.getActualAltitude()));
        out.closeCoordinates();
        out.closePoint();
        out.closePlacemark();
    }*/

    private static int altitudeToKml(int altitude, boolean onGround) {
        if (!onGround) {
            return (int) (ALT_COEFF * altitude / 3.28);
        } else {
            return (int) (altitude / 3.28) + 5;
        }
    }

    private static void writeTrackStyle(KmlOut out, String styleName, String lineColor, String polyColor) throws IOException {
        out.write(
                "<Style id=\"" + styleName + "\">" +
                "<LineStyle><color>" + lineColor + "</color></LineStyle>" +
                "<PolyStyle><color>" + polyColor + "</color></PolyStyle>" +
                "</Style>");
    }
}
