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
import java.text.DecimalFormat;
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
        try (Session session = webAppContext.openSession()) {

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
        try (Session session = webAppContext.openSession()) {

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

    /*@GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws IOException, SQLException {
        logger.debug("loading flights");

        try (Session session = webAppContext.openSession()) {
            auth.checkLoggedIn();

            //noinspection JpaQlInspection,unchecked
            List<Flight> flights = session
                    .createQuery("from Flight where active = true and accountId = :accountId order by departureUtc")
                    .setInteger("accountId", auth.getCurrentAccountId())
                    .list();

            List<FlightDto> dtos = new ArrayList<>();
            for (Flight flight : flights) {
                FlightDto dto = FlightTransform.toDto(flight);

                List<ValidationResult> validationResults = validateFlight(flight, flights);// todo there should be "covering flight list" instead of just flight list
                dto.setValidationStatus(validationResultsToJson(validationResults));

                dtos.add(dto);
            }

            return Response.ok(RestUtils.success(dtos)).build();
        } catch (Exception e) {
            logger.error("Could not load flights", e);

            String msg = String.format("Could not load flights: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }

    @POST
    @Path("update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(FlightDto flightDto) {
        logger.debug("updating flight " + flightDto);

        try (Session session = webAppContext.openSession()) {
            auth.checkLoggedIn();

            Flight flightFromClient = FlightTransform.fromDto(flightDto, session);
            Flight flightFromDb = session.get(Flight.class, flightFromClient.getId());

            if (!flightFromDb.getAccountId().equals(auth.getCurrentAccountId())) {
                throw new IllegalStateException("Trying to update a flight of another user");
            }

            if (!flightFromDb.getActive()) {
                throw new IllegalStateException("Trying to update a deleted flight");
            }

            flightFromClient.setAccountId(flightFromDb.getAccountId());
            flightFromClient.setVersion(flightFromDb.getVersion());
            flightFromClient.setActive(flightFromDb.getActive());
            flightFromClient.setSourceId(flightFromDb.getSourceId());

            session.getTransaction().begin();
            session.evict(flightFromDb);
            session.update(flightFromClient);
            session.getTransaction().commit();



            FlightDto resultedFlightDto = FlightTransform.toDto(flightFromClient);

            List<Flight> coveringFlights = loadCoveringFlights(session, flightFromClient);
            List<ValidationResult> validationResults = validateFlight(flightFromClient, coveringFlights);
            resultedFlightDto.setValidationStatus(validationResultsToJson(validationResults));



            return Response.ok(RestUtils.success(Collections.singletonList(resultedFlightDto))).build();
        } catch (Exception e) {
            logger.error("Could not update a flight", e);

            String msg = String.format("Could not update a flight: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }

    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(FlightDto flightDto) {
        logger.debug("creating flight " + flightDto);

        flightDto.setId(null);

        try (Session session = webAppContext.openSession()) {
            auth.checkLoggedIn();

            Flight flight = FlightTransform.fromDto(flightDto, session);

            flight.setActive(true);
            flight.setSourceId(FSLog.Source.User.getId());
            flight.setAccountId(auth.getCurrentAccountId());

            session.getTransaction().begin();
            session.save(flight);
            session.getTransaction().commit();



            FlightDto resultedFlightDto = FlightTransform.toDto(flight);

            List<Flight> coveringFlights = loadCoveringFlights(session, flight);
            List<ValidationResult> validationResults = validateFlight(flight, coveringFlights);
            resultedFlightDto.setValidationStatus(validationResultsToJson(validationResults));



            return Response.ok(RestUtils.success(Collections.singletonList(resultedFlightDto))).build();
        } catch (Exception e) {
            logger.error("Could not create a flight", e);

            String msg = String.format("Could not create a flight: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }

    @POST
    @Path("delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(FlightDto flightDto) {
        logger.debug("marking as deleted flight " + flightDto);

        try (Session session = webAppContext.openSession()) {
            auth.checkLoggedIn();

            Flight flightFromClient = FlightTransform.fromDto(flightDto, session);
            Flight flightFromDb = session.get(Flight.class, flightFromClient.getId());

            if (flightFromDb.getAccountId().equals(auth.getCurrentAccountId())) {
                throw new IllegalStateException("Trying to delete a flight of another user");
            }

            flightFromDb.setActive(false);

            session.getTransaction().begin();
            session.update(flightFromDb);
            session.getTransaction().commit();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error("Could not delete a flight", e);

            String msg = String.format("Could not delete a flight: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }

    private List<ValidationResult> validateFlight(Flight flight, List<Flight> coveringFlights) throws JSONException {
        List<ValidationResult> results = new ArrayList<>();

        LocalDateTime departureUtc = flight.getDepartureUtc();
        if (departureUtc == null) {
            results.add(ValidationResult.noRequiredValue("ATD"));
        }

        LocalDateTime arrivalUtc = flight.getArrivalUtc();
        if (arrivalUtc == null) {
            results.add(ValidationResult.noRequiredValue("ATA"));
        }

        if (departureUtc != null && arrivalUtc != null) {
            for (Flight anotherFlight : coveringFlights) {
                if (flight.getId().equals(anotherFlight.getId())) {
                    continue;
                }

                LocalDateTime anotherDepartureUtc = anotherFlight.getDepartureUtc();
                LocalDateTime anotherArrivalUtc = anotherFlight.getArrivalUtc();
                if (anotherDepartureUtc == null || anotherArrivalUtc == null) {
                    continue;
                }

                if ((departureUtc.isAfter(anotherDepartureUtc) && departureUtc.isBefore(anotherArrivalUtc))
                        || (arrivalUtc.isAfter(anotherDepartureUtc) && arrivalUtc.isBefore(anotherArrivalUtc))
                        || (departureUtc.isBefore(anotherDepartureUtc) && arrivalUtc.isAfter(anotherArrivalUtc))) {
                    ValidationResult validationResult = new ValidationResult();
                    validationResult.setType("overlap");
                    validationResult.setMessage(String.format("Conflicting flight %s-%s %s-%s",
                            Misc.mn(anotherFlight.getFromIcao(), "????"),
                            Misc.mn(anotherFlight.getToIcao(), "????"),
                            JavaTime.toHhmm(anotherDepartureUtc.toLocalTime()),
                            JavaTime.toHhmm(anotherArrivalUtc.toLocalTime())));
                    results.add(validationResult);
                }
            }
        }

        return results;
    }

    private String validationResultsToJson(List<ValidationResult> results) throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray jsonResults = new JSONArray();
        for (ValidationResult result : results) {
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("type", result.getType());
            jsonResult.put("message", result.getMessage());
            jsonResults.put(jsonResult);
        }
        json.put("results", jsonResults);

        return json.toString();
    }

    private List<Flight> loadCoveringFlights(Session session, Flight flight) {
        if (flight.getDepartureUtc() == null || flight.getArrivalUtc() == null) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }

        //noinspection JpaQlInspection,unchecked
        return session
                .createQuery("from Flight where active = true and accountId = :accountId " +
                        "and :fromUtc <= departureUtc and arrivalUtc <= :toUtc order by departureUtc")
                .setInteger("accountId", auth.getCurrentAccountId())
                .setTimestamp("fromUtc", new Date(flight.getDepartureUtc().minusDays(1).atOffset(ZoneOffset.UTC).toEpochSecond()*1000)) // todo oh my god!
                .setTimestamp("toUtc", new Date(flight.getArrivalUtc().plusDays(1).atOffset(ZoneOffset.UTC).toEpochSecond()*1000)) // todo oh my god!
                .list();
    }

    private static class ValidationResult {
        private String type;
        private String message;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public static ValidationResult noRequiredValue(String fieldName) {
            ValidationResult result = new ValidationResult();
            result.setType("noRequiredValue");
            result.setMessage(String.format("Please specify %s", fieldName));
            return result;
        }

    }*/
}
