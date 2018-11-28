package net.simforge.networkview.flights.web;

import net.simforge.networkview.datafeeder.SessionManager;
import net.simforge.networkview.flights.Flights;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

public class WebAppContext {

    private static final Logger logger = LoggerFactory.getLogger(WebAppContext.class.getName());
    private static final String ATTRIBUTE_NAME = WebAppContext.class.getName();

    private SessionFactory flightsSessionFactory;
    private SessionManager reportsSessionManager;

    protected static void create(ServletContext servletContext) {
        servletContext.setAttribute(ATTRIBUTE_NAME, new WebAppContext());
    }

    public static WebAppContext get(ServletContext servletContext) {
        return (WebAppContext) servletContext.getAttribute(ATTRIBUTE_NAME);
    }

    private WebAppContext() {
        logger.info("creating session factory");

        flightsSessionFactory = Flights.buildSessionFactory("flights.VATSIM");
        reportsSessionManager = new SessionManager();

        logger.info("session factory has been built");
    }

    public synchronized void destroy() {
        logger.info("closing session manager");

        flightsSessionFactory.close();
        flightsSessionFactory = null;

        reportsSessionManager.dispose();
        reportsSessionManager = null;

        logger.info("session manager is closed");
    }

    public Session openFlightsSession() {
        return flightsSessionFactory.openSession();
    }

    public SessionManager getReportsSessionManager() {
        return reportsSessionManager;
    }
}
