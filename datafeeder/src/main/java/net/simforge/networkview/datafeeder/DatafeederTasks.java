package net.simforge.networkview.datafeeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatafeederTasks {
    private static Logger logger = LoggerFactory.getLogger(DatafeederTasks.class.getName());

    private static SessionManager sessionManager;

    public static class StartupAction implements Runnable {
        @Override
        public void run() {
            logger.info("creating session manager");
            sessionManager = new SessionManager();
        }
    }

    public static class ShutdownAction implements Runnable {
        @Override
        public void run() {
            logger.info("killing session manager");
            SessionManager _sessionManager = sessionManager;
            sessionManager = null;
            _sessionManager.dispose();
        }
    }

    public static SessionManager getSessionManager() {
        return sessionManager;
    }
}
