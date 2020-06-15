package net.simforge.networkview.flights.method.eventbased;

import net.simforge.commons.misc.Misc;
import net.simforge.networkview.flights.method.eventbased.persistence.DBPersistenceLayer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public abstract class DbBaseTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(DbBaseTest.class.getName());

    private SessionFactory flightsSessionFactory;

    @Before
    public void before() {
        flightsSessionFactory = Flights.buildSessionFactoryWithSchema("flights-testdb");
    }

    protected void initDBPersistence() {
        persistenceLayer = new DBPersistenceLayer(flightsSessionFactory, reportDatasource);
    }

    // === DB Specific Checks ==========================================================================================

    protected void checkDBNoPilotContext() throws IOException {
        countCheckMethod();

        assertNull(persistenceLayer.loadContext(pilotNumber));
        logger.info("\tOK:DB No Pilot Context");
    }

    protected void checkDBFlightsCount(int expectedCount) {
        countCheckMethod();

        try (Session session = flightsSessionFactory.openSession()) {
            //noinspection JpaQlInspection
            Long actualCount = (Long) session
                    .createQuery("select count(f) from Flight f where pilotNumber = :pilotNumber")
                    .setInteger("pilotNumber", pilotNumber)
                    .uniqueResult();
            assertEquals(expectedCount, actualCount.intValue());
        }

        logger.info("\tOK:DB Flights Count");
    }

    protected void checkDBCurrFlightStatus(FlightStatus expectedStatus) throws IOException {
        countCheckMethod();

        PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
        Flight currFlight = pilotContext.getCurrFlight();
        assertNotNull(currFlight);
        assertTrue(currFlight.getStatus().is(expectedStatus));
        logger.info("\tOK:DB Flight Status is " + expectedStatus);
    }

    protected void checkDBCurrFlightRoute(String expectedDeparture, String expectedDestination) throws IOException {
        countCheckMethod();

        PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
        Flight currFlight = pilotContext.getCurrFlight();
        assertNotNull(currFlight);

        if (expectedDeparture != null) {
            assertEquals(expectedDeparture, currFlight.getTakeoff().getAirportIcao());
        }

        if (expectedDestination != null) {
            assertEquals(expectedDestination, currFlight.getLanding().getAirportIcao());
        } else {
            assertNull(currFlight.getLanding());
        }

        logger.info(String.format("\tOK:DB Flight Route: %s-%s", Misc.mn(expectedDeparture, "[--]"), Misc.mn(expectedDestination, "[--]")));
    }

    protected void checkDBCurrFlightCallsign(String callsign) throws IOException {
        countCheckMethod();

        PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
        Flight currFlight = pilotContext.getCurrFlight();
        assertNotNull(currFlight);

        assertEquals(callsign, currFlight.getCallsign());
        logger.info("\tOK:DB Callsign");
    }

    protected void checkDBCurrFlightLastSeen(String expectedLastSeenIcao) throws IOException {
        countCheckMethod();

        PilotContext pilotContext = persistenceLayer.loadContext(pilotNumber);
        Flight currFlight = pilotContext.getCurrFlight();
        assertNotNull(currFlight);

        assertEquals(expectedLastSeenIcao, currFlight.getLastSeen().getAirportIcao());
        logger.info("\tOK:DB Callsign");
    }
}
