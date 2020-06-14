package net.simforge.networkview.flights.web;

import net.simforge.commons.legacy.BM;
import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.report.ReportUtils;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.networkview.core.report.persistence.ReportPilotPosition;
import net.simforge.networkview.flights1.processors.eventbased.Position;
import net.simforge.networkview.flights1.processors.eventbased.datasource.DBReportDatasource;
import net.simforge.networkview.flights1.processors.eventbased.datasource.ReportDatasource;
import net.simforge.networkview.flights1.processors.eventbased.persistence.DBFlight;
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
import static net.simforge.networkview.flights1.processors.eventbased.FlightStatus.Lost;

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
    @Path("track-behind")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrackBehind(@QueryParam("pilotNumber") String pilotNumber, @QueryParam("_beforeReport") String _beforeReport) {
        BM.start("TrackAPI.getTrackBehind");
        try {
            Report latestReport = loadLatestReport();

            ReportDatasource datasource = new DBReportDatasource(Network.VATSIM, webAppContext.getReportsSessionManager());
            Report beforeReport = null;
            if (_beforeReport != null) {
                beforeReport = datasource.loadReport(_beforeReport);
            }
            if (beforeReport == null) {
                beforeReport = latestReport;
            }

            List<Report> reports = loadPreviousReports(beforeReport, 50);

            List<Map<String, Object>> trackDtos = buildTrackDtos(Integer.parseInt(pilotNumber), reports, latestReport);

            return Response.ok(RestUtils.success(trackDtos)).build();
        } catch (Exception e) {
            logger.error("Could not load track data", e);

            String msg = String.format("Could not load track data: %s", Misc.messagesBr(e));
            return Response.serverError().entity(RestUtils.failure(msg)).build();
        } finally {
            BM.stop();
        }
    }

    @GET
    @Path("track-ahead")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrackAhead(@QueryParam("pilotNumber") String pilotNumber, @QueryParam("afterReport") String _afterReport) {
        BM.start("TrackAPI.getTrackAhead");
        try {
            Report latestReport = loadLatestReport();

            ReportDatasource datasource = new DBReportDatasource(Network.VATSIM, webAppContext.getReportsSessionManager());
            Report afterReport = null;
            if (_afterReport != null) {
                afterReport = datasource.loadReport(_afterReport);
            }
            if (afterReport == null) {
                throw new IllegalArgumentException("Unable to find report " + _afterReport);
            }

            List<Report> reports = loadNextReports(afterReport, 50);

            List<Map<String, Object>> trackDtos = buildTrackDtos(Integer.parseInt(pilotNumber), reports, latestReport);

            return Response.ok(RestUtils.success(trackDtos)).build();
        } catch (Exception e) {
            logger.error("Could not load track data", e);

            String msg = String.format("Could not load track data: %s", Misc.messagesBr(e));
            return Response.serverError().entity(RestUtils.failure(msg)).build();
        } finally {
            BM.stop();
        }
    }

    private List<Map<String, Object>> buildTrackDtos(int pilotNumber, List<Report> reports, Report latestReport) {
        ReportDatasource datasource = new DBReportDatasource(Network.VATSIM, webAppContext.getReportsSessionManager());

        long fromReportId = reports.get(0).getId();
        long toReportId = reports.get(reports.size() - 1).getId();
        List<ReportPilotPosition> reportPilotPositions = datasource.loadPilotPositions(pilotNumber, fromReportId, toReportId);
        Map<Long, ReportPilotPosition> reportPilotPositionMap = reportPilotPositions.stream().collect(toMap(p -> p.getReport().getId(), Function.identity()));

        List<DBFlight> flights;
        try (Session session = webAppContext.openFlightsSession()) {
            //noinspection unchecked,JpaQlInspection
            flights = session
                    .createQuery("from Flight where pilotNumber = :pilotNumber " +
                            "and (firstSeenReportId between :fromReportId and :toReportId " +
                            "or lastSeenReportId between :fromReportId and :toReportId " +
                            "or (firstSeenReportId <= :fromReportId and lastSeenReportId >= :toReportId))")
                    .setInteger("pilotNumber", pilotNumber)
                    .setLong("fromReportId", fromReportId)
                    .setLong("toReportId", toReportId)
                    .list();
        }

        List<Map<String, Object>> trackDtos = new ArrayList<>();

        for (Report report : reports) {
            ReportPilotPosition reportPilotPosition = reportPilotPositionMap.get(report.getId());
            DBFlight flight = findFlight(flights, report);

            Map<String, Object> map = new HashMap<>();
            map.put("report", report.getReport());
            map.put("reportDt", JavaTime.Hms.format(ReportUtils.fromTimestampJava(report.getReport())));
            if (report.getId().equals(latestReport.getId())) {
                map.put("reportLatest", true);
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

            trackDtos.add(0, map); // making it in reverse order
        }

        return trackDtos;
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

    private Report loadLatestReport() {
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

    private List<Report> loadNextReports(Report afterReport, int amount) {
        BM.start("TrackAPI.loadNextReports");
        try (Session session = webAppContext.getReportsSessionManager().getSession(Network.VATSIM)) {

            //noinspection JpaQlInspection,unchecked
            List<Report> reports = session
                    .createQuery("from Report where report >= :afterReport and parsed = true order by report asc")
                    .setString("afterReport", afterReport.getReport())
                    .setMaxResults(amount)
                    .list();
            return reports;

        } finally {
            BM.stop();
        }
    }

    private List<Report> loadPreviousReports(Report beforeReport, int amount) {
        BM.start("TrackAPI.loadPreviousReports");
        try (Session session = webAppContext.getReportsSessionManager().getSession(Network.VATSIM)) {

            //noinspection JpaQlInspection,unchecked
            List<Report> reports = session
                    .createQuery("from Report where report <= :beforeReport and parsed = true order by report desc")
                    .setString("beforeReport", beforeReport.getReport())
                    .setMaxResults(amount)
                    .list();
            Collections.reverse(reports);
            return reports;

        } finally {
            BM.stop();
        }
    }
}
