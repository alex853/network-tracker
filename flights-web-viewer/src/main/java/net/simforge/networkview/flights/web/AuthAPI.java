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
import java.util.HashMap;
import java.util.Map;

@Deprecated
//@Path("auth")
public class AuthAPI {
/*    private static final Logger logger = LoggerFactory.getLogger(AuthAPI.class.getName());

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

    @POST
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status() throws IOException, SQLException {
        logger.debug("status");

        return Response.ok(RestUtils.success("status", getStatus())).build();
    }

    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormParam("username") String username,
            @FormParam("password") String password) throws IOException, SQLException {
        logger.debug("logging in");

        try (Session session = webAppContext.openSession()) {
            //noinspection JpaQlInspection
            Account account = (Account) session
                    .createQuery("from Account a where a.username = :username")
                    .setString("username", username)
                    .uniqueResult();

            if (account != null) {
                boolean passwordOk = account.getPassword() != null && account.getPassword().equals(password);

                if (passwordOk) {
                    auth.login(account);

                    return Response.ok(RestUtils.success("status", getStatus())).build();
                }
            }

            return Response.ok(RestUtils.failure("Wrong username or password")).build();
        } catch (Exception e) {
            logger.error("Could not log user in", e);

            String msg = String.format("Could not log in due to error: %s", Misc.messagesBr(e));
            return Response.ok(RestUtils.failure(msg)).build();
        }

    }

    @POST
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() throws IOException, SQLException {
        logger.debug("logging out");

        auth.logout();

        return Response.ok(RestUtils.success("status", getStatus())).build();
    }

    private Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("loggedIn", auth.isLoggedIn());
        result.put("username", auth.getCurrentUsername());
        return result;
    }*/
}
