package net.simforge.atmosphere;

import junit.framework.TestCase;

public class ActualAltitudeTest extends TestCase {
    public void testStdPressure() {
        ActualAltitude altitude = ActualAltitude.get(10000, 1013.25);
        assertEquals(10000, altitude.getActualAltitude());
        assertEquals(10000, altitude.getActualFlightLevel());
        assertEquals(10000, altitude.getNearestX1000FlightLevel());
        assertTrue(altitude.isOnX1000FlightLevel());
    }

    public void testQnh1016() {
        ActualAltitude altitude = ActualAltitude.get(6000, 1016);
        assertEquals(5925, altitude.getActualAltitude());
        assertEquals(5900, altitude.getActualFlightLevel());
        assertEquals(6000, altitude.getNearestX1000FlightLevel());
        assertTrue(altitude.isOnX1000FlightLevel());
    }

    public void testQnh1008() {
        ActualAltitude altitude = ActualAltitude.get(29883, 1008);
        assertEquals(30027, altitude.getActualAltitude());
        assertEquals(30000, altitude.getActualFlightLevel());
        assertEquals(30000, altitude.getNearestX1000FlightLevel());
        assertTrue(altitude.isOnX1000FlightLevel());
    }

    public void testOutOfX1000FlightLevel() {
        ActualAltitude altitude = ActualAltitude.get(29583, 1008);
        assertEquals(29727, altitude.getActualAltitude());
        assertEquals(29700, altitude.getActualFlightLevel());
        assertEquals(30000, altitude.getNearestX1000FlightLevel());
        assertFalse(altitude.isOnX1000FlightLevel());
    }

    public void testFormatAltitude() {
        assertEquals("A050", ActualAltitude.formatAltitude(5000, AltimeterMode.QNH));

        assertEquals("FL100", ActualAltitude.formatAltitude(10000, AltimeterMode.STD));
        assertEquals("FL100", ActualAltitude.formatAltitude(10010, AltimeterMode.STD));
        assertEquals("FL101", ActualAltitude.formatAltitude(10090, AltimeterMode.STD));
        assertEquals("FL101", ActualAltitude.formatAltitude(10110, AltimeterMode.STD));
    }
}
