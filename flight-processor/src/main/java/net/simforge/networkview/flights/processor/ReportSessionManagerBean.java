package net.simforge.networkview.flights.processor;

import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.report.persistence.ReportSessionManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class ReportSessionManagerBean { // spring wrapper for SessionManager from datafeeder module
    private static final Logger logger = LoggerFactory.getLogger(ReportSessionManagerBean.class);

    private ReportSessionManager reportSessionManager;

    @PostConstruct
    public void init() {
        reportSessionManager = new ReportSessionManager();
        logger.info("init completed");
    }

    @PreDestroy
    public void destroy() {
        reportSessionManager.dispose();
        logger.info("destroy completed");
    }

    public ReportSessionManager getSessionManager() {
        return reportSessionManager;
    }

    public Session getSession(Network network) {
        return reportSessionManager.getSession(network);
    }
}
