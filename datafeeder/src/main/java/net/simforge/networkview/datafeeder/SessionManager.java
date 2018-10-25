package net.simforge.networkview.datafeeder;

import net.simforge.commons.legacy.misc.Settings;
import net.simforge.networkview.datafeeder.persistence.Report;
import net.simforge.networkview.datafeeder.persistence.ReportLogEntry;
import net.simforge.networkview.datafeeder.persistence.ReportPilotFpRemarks;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;
import net.simforge.tracker.tools.ReportUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static Logger logger = LoggerFactory.getLogger(SessionManager.class.getName());

    private Map<String, SessionFactory> database2sessionFactory = new HashMap<>();

    // It opens session to operational DB which contains most recent report positions and keeps 1-3 months history.
    public synchronized Session getSession(Network network) {
        String databaseName = network.name();

        return getSession(databaseName);
    }

    // It opens session to archive DB.
    public synchronized Session getSession(Network network, String report) {
        String databaseName = network.name() + ReportUtils.fromTimestampJava(report).getYear();

        return getSession(databaseName);
    }

    private Session getSession(String databaseName) {
        SessionFactory sessionFactory = database2sessionFactory.get(databaseName);

        if (sessionFactory == null) {
            Configuration configuration = new Configuration();

            String driverClass = Settings.get("datafeeder.db.driver-class");
            String urlPattern  = Settings.get("datafeeder.db.url-pattern");
            String username    = Settings.get("datafeeder.db.username");
            String password    = Settings.get("datafeeder.db.password");
            String poolSize    = Settings.get("datafeeder.db.pool-size");

            String url = urlPattern.replace("%DB%", databaseName);

            configuration.setProperty("hibernate.connection.driver_class", driverClass);
            configuration.setProperty("hibernate.connection.url",          url);
            configuration.setProperty("hibernate.connection.username",     username);
            configuration.setProperty("hibernate.connection.password",     password);
            configuration.setProperty("hibernate.connection.pool_size",    poolSize);

            configuration.addAnnotatedClass(Report.class);
            configuration.addAnnotatedClass(ReportLogEntry.class);
            configuration.addAnnotatedClass(ReportPilotPosition.class);
            configuration.addAnnotatedClass(ReportPilotFpRemarks.class);

            sessionFactory = configuration.buildSessionFactory();

            database2sessionFactory.put(databaseName, sessionFactory);
        }

        return sessionFactory.openSession();
    }

    public synchronized void dispose() {
        for (Map.Entry<String, SessionFactory> entry : database2sessionFactory.entrySet()) {
            SessionFactory sessionFactory = entry.getValue();

            logger.info("disposing session factory for " + entry.getKey());
            sessionFactory.close();
        }
        database2sessionFactory.clear();
    }
}
