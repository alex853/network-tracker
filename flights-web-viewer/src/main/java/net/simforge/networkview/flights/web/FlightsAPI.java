package net.simforge.networkview.flights.web;

import net.simforge.commons.legacy.BM;
import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
import net.simforge.networkview.flights2.flight.FlightStatus;
import net.simforge.networkview.flights3.persistence.DBFlight;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("flights")
public class FlightsAPI {
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
    @Path("active-flights")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveFlights() {
        BM.start("FlightsAPI.getActiveFlights");
        try (Session session = webAppContext.openFlightsSession()) {

            //noinspection JpaQlInspection,unchecked
            List<DBFlight> flights = session
                    .createQuery("from Flight where status < :finishedCode or (status >= :finishedCode and lastSeenDt >= :lastSeenDt) order by callsign")
                    .setInteger("finishedCode", FlightStatus.Finished.getCode())
                    .setParameter("lastSeenDt", JavaTime.nowUtc().minusHours(1))
                    .list();

            List<Map<String, Object>> flightDtos = dbFlightsToMaps(flights);

            return Response.ok(RestUtils.success(flightDtos)).build();
        } catch (Exception e) {
            logger.error("Could not load active flights", e);

            String msg = String.format("Could not load active flights: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        } finally {
            BM.stop();
        }
    }

    @GET
    @Path("pilot-flights")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPilotFlights(@QueryParam("pilotNumber") String pilotNumber) {
        BM.start("FlightsAPI.getPilotFlights");
        try (Session session = webAppContext.openFlightsSession()) {

            //noinspection JpaQlInspection,unchecked
            List<DBFlight> flights = session
                    .createQuery("from Flight where pilotNumber = :pilotNumber order by firstSeenDt desc")
                    .setInteger("pilotNumber", Integer.parseInt(pilotNumber))
                    .list();

            List<Map<String, Object>> flightDtos = dbFlightsToMaps(flights);

            return Response.ok(RestUtils.success(flightDtos)).build();
        } catch (Exception e) {
            logger.error("Could not load pilot flights", e);

            String msg = String.format("Could not load pilot flights: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        } finally {
            BM.stop();
        }
    }

    private List<Map<String, Object>> dbFlightsToMaps(List<DBFlight> flights) {
        List<Map<String, Object>> flightDtos = new ArrayList<>();
        for (DBFlight flight : flights) {
            Map<String, Object> flightDto = new HashMap<>();

            flightDto.put("callsign", flight.getCallsign());
            flightDto.put("pilotNumber", String.valueOf(flight.getPilotNumber()));
            flightDto.put("aircraftType", flight.getAircraftType());
            flightDto.put("regNo", flight.getRegNo());

            flightDto.put("status", FlightStatus.byCode(flight.getStatus()).name()); // todo remove it
            flightDto.put("statusText", FlightStatus.byCode(flight.getStatus()).name());
            flightDto.put("statusCode", String.valueOf(flight.getStatus()));

            flightDto.put("firstSeenDt", printTime(flight.getFirstSeenDt()));

            flightDto.put("lastSeenDt", printTime(flight.getLastSeenDt()));

            flightDto.put("takeoffIcao", flight.getTakeoffIcao());
            flightDto.put("takeoffDt", printTime(flight.getTakeoffDt()));
            flightDto.put("takeoffLat", flight.getTakeoffLatitude());
            flightDto.put("takeoffLng", flight.getTakeoffLongitude());

            flightDto.put("landingIcao", flight.getLandingIcao());
            flightDto.put("landingDt", printTime(flight.getLandingDt()));
            flightDto.put("landingLat", flight.getLandingLatitude());
            flightDto.put("landingLng", flight.getLandingLongitude());

            flightDto.put("distance", flight.getDistanceFlown() != null
                    ? String.valueOf(flight.getDistanceFlown().intValue()) + " nm"
                    : "");
            flightDto.put("time", flight.getFlightTime() != null
                    ? JavaTime.toHhmm(Duration.of((long) (flight.getFlightTime() * 60), ChronoUnit.MINUTES))
                    : "");





            flightDto.put("fpRoute", printIcao(flight.getPlannedDeparture()) + " - " + printIcao(flight.getPlannedDestination()));
            flightDto.put("takeoff", flight.getTakeoffIcao() != null || flight.getTakeoffDt() != null
                    ? printIcao(flight.getTakeoffIcao()) + " at " + printTime(flight.getTakeoffDt())
                    : "");
            flightDto.put("landing", flight.getLandingIcao() != null || flight.getLandingDt() != null
                    ? printIcao(flight.getLandingIcao()) + " at " + printTime(flight.getLandingDt())
                    : "");

            flightDtos.add(flightDto);
        }
        return flightDtos;
    }

    private static final DateTimeFormatter hhmmdM = DateTimeFormatter.ofPattern("HH:mm dd/MM");
    private static final DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");

    private String printTime(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }

        LocalDate today = JavaTime.todayUtc();
        LocalDate dtDay = dt.toLocalDate();
        if (today.equals(dtDay)) {
            return hhmm.format(dt);
        } else {
            return hhmmdM.format(dt);
        }
    }

    private String printIcao(String icao) {
        return Misc.mn(icao, "xxxx");
    }
}
