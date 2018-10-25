package net.simforge.tracker.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebAppServletContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(WebAppServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("web-app is starting");

        WebAppContext.create(sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("web-app is being destroyed");

        WebAppContext.get(sce.getServletContext()).destroy();

        log.info("web-app is destroyed");
    }
}
