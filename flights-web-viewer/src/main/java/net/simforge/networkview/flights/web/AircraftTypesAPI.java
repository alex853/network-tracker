package net.simforge.networkview.flights.web;

import net.simforge.commons.misc.Misc;
import net.simforge.commons.misc.RestUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//@Path("aircraft-types")
public class AircraftTypesAPI {
/*    private static final Logger logger = LoggerFactory.getLogger(AircraftTypesAPI.class.getName());

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
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws IOException, SQLException {
        logger.debug("loading list of aircraft types");

        try (Session session = webAppContext.openSession()) {
            auth.checkLoggedIn();

            //noinspection JpaQlInspection,unchecked
            List<AircraftType> types = session
                    .createQuery("from AircraftType order by icaoCode")
                    .list();



            List<Object> dtos = new ArrayList<>();
            for (AircraftType type : types) {
                //noinspection unused
                dtos.add(new Object() {
                    public String getId() {
                        return String.valueOf(type.getId());
                    }

                    public String getIcaoCode() {
                        return type.getIcaoCode();
                    }
                });
            }

            return Response.ok(RestUtils.success(dtos)).build();
        } catch (Exception e) {
            logger.error("Could not load aircraft types", e);

            String msg = String.format("Could not load aircraft types: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }
    }*/
}
