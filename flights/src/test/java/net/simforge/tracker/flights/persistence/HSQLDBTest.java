package net.simforge.tracker.flights.persistence;

import net.simforge.networkview.datafeeder.persistence.Report;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HSQLDBTest {
    @Test
    public void testConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.hsqldb.jdbcDriver" );
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:testdb", "sa", "");
    }

    @Test
    public void testHibernate() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        configuration.setProperty("hibernate.connection.url", "jdbc:hsqldb:testdb");
        configuration.setProperty("hibernate.connection.username", "sa");
        configuration.setProperty("hibernate.connection.password", "");
        configuration.setProperty("hibernate.connection.pool_size", "1");

        configuration.setProperty("hibernate.hbm2ddl.auto", "create");

        configuration.addAnnotatedClass(Report.class);

        SessionFactory sessionFactory = configuration.buildSessionFactory();

        Session session = sessionFactory.openSession();

        //noinspection JpaQlInspection
        List list = session.createQuery("from Report").list();
        assertEquals(0, list.size());

        session.getTransaction().begin();
        session.save(new Report());
        session.getTransaction().commit();
        session.clear();

        session = sessionFactory.openSession();
        //noinspection JpaQlInspection
        list = session.createQuery("from Report").list();
        assertEquals(1, list.size());
    }
}
