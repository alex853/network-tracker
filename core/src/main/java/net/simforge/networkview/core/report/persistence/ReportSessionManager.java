package net.simforge.networkview.core.report.persistence;

import net.simforge.commons.legacy.misc.Settings;
import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.report.ReportUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ReportSessionManager {
    private static Logger logger = LoggerFactory.getLogger(ReportSessionManager.class.getName());

    private Map<String, SessionFactory> database2sessionFactory = new HashMap<>();

    // It opens session to operational DB which contains most recent report positions and keeps 1-3 months history.
    public synchronized Session getSession(Network network) {
        String databaseName = network.name();

        return getSession(databaseName);
    }

    // It opens session to archive DB.
    public synchronized Session getSession(Network network, String report) {
        int year = ReportUtils.fromTimestampJava(report).getYear();

        return getSession(network, year);
    }

    // It opens session to archive DB.
    public synchronized Session getSession(Network network, int year) {
        String databaseName = network.name() + year;

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
