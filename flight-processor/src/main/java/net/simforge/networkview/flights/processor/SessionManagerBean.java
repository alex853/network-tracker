package net.simforge.networkview.flights.processor;

import net.simforge.networkview.Network;
import net.simforge.networkview.datafeeder.SessionManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class SessionManagerBean { // spring wrapper for SessionManager from datafeeder module
    private static final Logger logger = LoggerFactory.getLogger(SessionManagerBean.class);

    private SessionManager sessionManager;

    @PostConstruct
    public void init() {
        sessionManager = new SessionManager();
        logger.info("init completed");
    }

    @PreDestroy
    public void destroy() {
        sessionManager.dispose();
        logger.info("destroy completed");
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Session getSession(Network network) {
        return sessionManager.getSession(network);
    }
}
