package net.simforge.networkview.flights.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebAppServletContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WebAppServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("web-app is starting");

        WebAppContext.create(sce.getServletContext());

        logger.info("web-app is started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("web-app is being destroyed");

        WebAppContext.get(sce.getServletContext()).destroy();

        logger.info("web-app is destroyed");
    }
}
