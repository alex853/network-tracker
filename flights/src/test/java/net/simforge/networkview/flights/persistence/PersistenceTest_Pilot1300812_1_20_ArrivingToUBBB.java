package net.simforge.networkview.flights.persistence;

import net.simforge.commons.hibernate.SessionFactoryBuilder;
import net.simforge.commons.io.Csv;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.Network;
import net.simforge.networkview.flights.FlightRecognition;
import net.simforge.networkview.flights.PersistenceStrategy;
import net.simforge.networkview.flights.datasource.CsvDatasource;
import net.simforge.networkview.flights.model.FlightStatus;
import net.simforge.networkview.flights.model.TrackingTest;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Ignore;

import java.io.File;
import java.io.InputStream;
import java.util.List;

@Ignore
public class PersistenceTest_Pilot1300812_1_20_ArrivingToUBBB extends TrackingTest {

    private SessionFactory sessionFactory;

    @Override
    protected void setUp() throws Exception {
//        System.setProperty("simforge.settings", new File(Class.class.getResource("/simforge.properties").toURI()).getParentFile().getAbsolutePath());

        sessionFactory = SessionFactoryBuilder
                .forDatabase("flights")
                .entities(FlightRecognition.entities)
                .createSchemaIfNeeded()
                .build();
        setStrategy(new PersistenceStrategy(Network.VATSIM, sessionFactory));

        InputStream is = Class.class.getResourceAsStream("/snapshots/pilot-1300812_from-1_amount-60.csv");
        String csvContent = IOHelper.readInputStream(is);
        setDatasource(new CsvDatasource(Csv.fromContent(csvContent)));

        init(1300812, 1, 20);
    }

    @Override
    protected void tearDown() {
        sessionFactory.close();
    }

    public void report_1_13() {
        try (Session session = sessionFactory.openSession()) {
            //noinspection JpaQlInspection
            checkFlightCount(session, 1);

            Flight flight = getFlight(session, 1);
            assertEquals(FlightStatus.Flying.getCode(), flight.getStatus().intValue());
            assertEquals(null, flight.getOriginIcao());
            assertEquals(null, flight.getDestinationIcao());
            assertEquals("USPP", flight.getPlannedOrigin());
            assertEquals("UBBB", flight.getPlannedDestination());
        }
    }

    public void report_14_16() {
        try (Session session = sessionFactory.openSession()) {
            //noinspection JpaQlInspection
            checkFlightCount(session, 1);

            Flight flight = getFlight(session, 1);
            assertEquals(FlightStatus.Arrival.getCode(), flight.getStatus().intValue());
            assertEquals(null, flight.getOriginIcao());
            assertEquals("UBBB", flight.getDestinationIcao());
            assertEquals("USPP", flight.getPlannedOrigin());
            assertEquals("UBBB", flight.getPlannedDestination());
        }
    }

    public void report_17_20() {
        try (Session session = sessionFactory.openSession()) {
            //noinspection JpaQlInspection
            checkFlightCount(session, 1);

            Flight flight = getFlight(session, 1);
            assertEquals(FlightStatus.Finished.getCode(), flight.getStatus().intValue());
            assertEquals(null, flight.getOriginIcao());
            assertEquals("UBBB", flight.getDestinationIcao());
            assertEquals("USPP", flight.getPlannedOrigin());
            assertEquals("UBBB", flight.getPlannedDestination());
        }
    }

    private Flight getFlight(Session session, int firstSeenReportId) {
        //noinspection JpaQlInspection
        return (Flight) session.createQuery("from Flight where firstSeenReportId = :firstSeenReportId").setInteger("firstSeenReportId", firstSeenReportId).uniqueResult();
    }

    private void checkFlightCount(Session session, int size) {
        //noinspection JpaQlInspection
        List list = session.createQuery("from Flight").list();
        assertEquals(size, list.size());
    }
}
