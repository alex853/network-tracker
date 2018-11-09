package net.simforge.networkview.flights.web;

import net.simforge.commons.misc.JavaTime;
import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
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
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

//@Path("statistics")
public class StatisticsAPI {
/*    private static final Logger logger = LoggerFactory.getLogger(StatisticsAPI.class.getName());

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ServletContext servletContext;
    private WebAppContext webAppContext;

    private AuthHelper auth;

    @Context
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.webAppContext = WebAppContext.get(servletContext);
    }

    @Context
    public void setRequest(HttpServletRequest request) {
        this.auth = new AuthHelper(request);
    }

    @GET
    @Path("byAircraftICAO")
    @Produces(MediaType.APPLICATION_JSON)
    public Response byAircraftICAO(@QueryParam("period") String periodCode) throws IOException, SQLException {
        return collectStats("ByAircraftICAO", periodCode, "tuned");
    }

    @GET
    @Path("byAircraftFamily")
    @Produces(MediaType.APPLICATION_JSON)
    public Response byAircraftFamily(@QueryParam("period") String periodCode) throws IOException, SQLException {
        return collectStats("ByAircraftFamily", periodCode, "tuned");
    }

    @GET
    @Path("byAircraftManufacturer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response byAircraftManufacturer(@QueryParam("period") String periodCode) throws IOException, SQLException {
        return collectStats("ByAircraftManufacturer", periodCode, "tuned");
    }

    @GET
    @Path("bySource")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bySource(@QueryParam("period") String periodCode) throws IOException, SQLException {
        return collectStats("BySource", periodCode, "sorted");
    }

    @GET
    @Path("byIcaoRegion")
    @Produces(MediaType.APPLICATION_JSON)
    public Response byIcaoRegion(@QueryParam("period") String periodCode) throws IOException, SQLException {
        return collectStats("ByIcaoRegion", periodCode, "tuned");
    }

    private Response collectStats(String type, String periodCode, String resultType) {
        try (Session session = webAppContext.openSession()) {
            Stats.Strategy strategy = createStrategy(type, session);

            auth.checkLoggedIn();

            Period period = getPeriod(periodCode);

            //noinspection JpaQlInspection,unchecked
            List<Flight> flights = session
                    .createQuery("from Flight " +
                            "where active = true " +
                            "and accountId = :accountId " +
                            "and departureUtc between :fromDt and :toDt " +
                            "order by departureUtc")
                    .setInteger("accountId", auth.getCurrentAccountId())
                    .setParameter("fromDt", period.getFrom())
                    .setParameter("toDt", period.getTo())
                    .list();


            Stats stats = Stats.create(strategy);
            for (Flight flight : flights) {
                stats.count(flight);
            }

            List<Stats.Entry> data;
            if ("raw".equals(resultType)) {
                data = stats.getRawData(strategy);
            } else if ("sorted".equals(resultType)) {
                data = stats.getSortedData(strategy);
            } else if ("tuned".equals(resultType)) {
                data = stats.getTunedData(strategy);
            } else {
                throw new IllegalArgumentException("Unknown result type '" + resultType + "'");
            }

            return Response.ok(RestUtils.success(data)).build();
        } catch (Exception e) {
            logger.error("Could not load flights", e);

            String msg = String.format("Could not load flights: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }

    private Period getPeriod(String periodCode) {
        if ("total".equals(periodCode)) {
            return new Period(LocalDateTime.of(1900, 1, 1, 0, 0), JavaTime.nowUtc());
        } else if ("last-year".equals(periodCode)) {
            LocalDateTime now = JavaTime.nowUtc();
            return new Period(now.minusYears(1), now);
        } else {
            int year = Integer.parseInt(periodCode);
            LocalDateTime from = LocalDateTime.of(year, 1, 1, 0, 0);
            return new Period(from, from.plusYears(1));
        }
    }

    private Stats.Strategy createStrategy(String type, Session session) {
        if ("ByAircraftICAO".equals(type)) {
            return new ByAircraftICAO();
        } else if ("ByAircraftFamily".equals(type)) {
            return new ByAircraftFamily();
        } else if ("ByAircraftManufacturer".equals(type)) {
            return new ByAircraftManufacturer();
        } else if ("ByAircraftType".equals(type)) {
            return new ByAircraftType(session);
        } else if ("ByIcaoRegion".equals(type)) {
            return new ByIcaoRegion();
        } else if ("BySource".equals(type)) {
            return new BySource();
        } else {
            throw new IllegalArgumentException("Unknown strategy type " + type);
        }
    }

    private static class Period {
        private LocalDateTime from;
        private LocalDateTime to;

        public Period(LocalDateTime from, LocalDateTime to) {
            this.from = from;
            this.to = to;
        }

        public LocalDateTime getFrom() {
            return from;
        }

        public LocalDateTime getTo() {
            return to;
        }
    }*/
}
