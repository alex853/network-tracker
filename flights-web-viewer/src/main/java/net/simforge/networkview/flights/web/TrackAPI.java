package net.simforge.networkview.flights.web;

import net.simforge.commons.legacy.BM;
import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.ReportUtils;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.networkview.flights.datasource.ReportDatasource;
import net.simforge.networkview.flights2.Position;
import net.simforge.networkview.flights2.persistence.DBReportDatasource;
import net.simforge.networkview.flights3.persistence.DBFlight;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static net.simforge.networkview.flights2.flight.FlightStatus.Lost;

@Path("track")
public class TrackAPI {
    private static final Logger logger = LoggerFactory.getLogger(FlightsAPI.class.getName());

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ServletContext servletContext;
    private WebAppContext webAppContext;

//    private AuthHelper auth;

    @Context
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webAppContext = WebAppContext.get(servletContext);
    }

    @Context
    public void setRequest(HttpServletRequest request) {
//        this.auth = new AuthHelper(request);
    }

    @GET
    @Path("pilot-track")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPilotTrack(@QueryParam("pilotNumber") String pilotNumber) {
        BM.start("TrackAPI.getPilotTrack");

        // load last report
        // load previous 50 reports
        // load pilot available positions for loaded reports
        // load flights for the period
        // make the result

        try (Session session = webAppContext.openFlightsSession()) {

            Report lastReport = loadLastReport();
            List<Report> reports = loadPreviousReports(lastReport, 50);

            ReportDatasource datasource = new DBReportDatasource(Network.VATSIM, webAppContext.getReportsSessionManager());

            long fromReportId = reports.get(0).getId();
            long toReportId = reports.get(reports.size() - 1).getId();
            List<ReportPilotPosition> reportPilotPositions = datasource.loadPilotPositions(Integer.parseInt(pilotNumber), fromReportId, toReportId);
            Map<Long, ReportPilotPosition> reportPilotPositionMap = reportPilotPositions.stream().collect(toMap(p -> p.getReport().getId(), Function.identity()));

            List<Map<String, Object>> positionDtos = new ArrayList<>();

            //noinspection unchecked,JpaQlInspection
            List<DBFlight> flights = session
                    .createQuery("from Flight where pilotNumber = :pilotNumber " +
                            "and (firstSeenReportId between :fromReportId and :toReportId " +
                            "or lastSeenReportId between :fromReportId and :toReportId " +
                            "or (firstSeenReportId <= :fromReportId and lastSeenReportId >= :toReportId))")
                    .setInteger("pilotNumber", Integer.parseInt(pilotNumber))
                    .setLong("fromReportId", fromReportId)
                    .setLong("toReportId", toReportId)
                    .list();

            for (Report report : reports) {
                ReportPilotPosition reportPilotPosition = reportPilotPositionMap.get(report.getId());
                DBFlight flight = findFlight(flights, report);

                Map<String, Object> map = new HashMap<>();
                map.put("report", JavaTime.Hms.format(ReportUtils.fromTimestampJava(report.getReport())));
                if (report.getId().equals(lastReport.getId())) {
                    map.put("reportLast", true);
                }

                if (reportPilotPosition != null) {
                    Position position = Position.create(reportPilotPosition);
                    map.put("posInfo", position.getStatus());
                    map.put("posLat", position.getCoords().getLat());
                    map.put("posLng", position.getCoords().getLon());
                    map.put("posOnGround", position.isOnGround());
                }

                if (flight != null) {
                    String from = flight.getTakeoffIcao();
                    if (from == null) {
                        if (flight.getPlannedDeparture() != null) {
                            from = "(" + flight.getPlannedDeparture() + ")";
                        }
                    } else {
                        if (from.equals(flight.getPlannedDeparture())) {
                            from = "[" + from + "]";
                        }
                    }
                    if (from == null) {
                        from = "InAir";
                    }

                    String to = flight.getLandingIcao();
                    if (to == null) {
                        if (flight.getPlannedDestination() != null) {
                            to = "(" + flight.getPlannedDestination() + ")";
                        }
                    } else {
                        if (to.equals(flight.getPlannedDestination())) {
                            to = "[" + to + "]";
                        }
                    }
                    if (to == null) {
                        to = flight.getStatus() == Lost.getCode() ? "{LOST}" : "{Unkn}";
                    }

                    map.put("flightInfo", from + "-" + to);
                }

                positionDtos.add(0, map); // making it in reverse order
            }

            return Response.ok(RestUtils.success(positionDtos)).build();
        } catch (Exception e) {
            logger.error("Could not load active flights", e);

            String msg = String.format("Could not load active flights: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        } finally {
            BM.stop();
        }
    }

    private DBFlight findFlight(List<DBFlight> flights, Report report) {
        long reportId = report.getId();
        for (DBFlight flight : flights) {
            if (flight.getFirstSeenReportId() <= reportId && reportId <= flight.getLastSeenReportId()) {
                return flight;
            }
        }
        return null;
    }

    private Report loadLastReport() {
        BM.start("TrackAPI.loadLastReport");
        try (Session session = webAppContext.getReportsSessionManager().getSession(Network.VATSIM)) {

            //noinspection JpaQlInspection
            return (Report) session
                    .createQuery("from Report where parsed = true order by report desc")
                    .setMaxResults(1)
                    .uniqueResult();

        } finally {
            BM.stop();
        }
    }

    private List<Report> loadPreviousReports(Report fromReport, int amount) {
        BM.start("TrackAPI.loadPreviousReports");
        try (Session session = webAppContext.getReportsSessionManager().getSession(Network.VATSIM)) {

            //noinspection JpaQlInspection,unchecked
            List<Report> reports = session
                    .createQuery("from Report where report <= :fromReport and parsed = true order by report desc")
                    .setString("fromReport", fromReport.getReport())
                    .setMaxResults(amount)
                    .list();
            Collections.reverse(reports);
            return reports;

        } finally {
            BM.stop();
        }
    }
}
