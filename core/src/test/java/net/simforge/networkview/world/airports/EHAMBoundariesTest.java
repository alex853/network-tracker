package net.simforge.networkview.world.airports;

import net.simforge.commons.misc.Geo;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EHAMBoundariesTest {
    @Test
    public void test() throws IOException {
        Airports airports = AirportsLoader.load();
        Airport eham = airports.getByIcao("EHAM");
        assertNotNull(eham);
        assertTrue(eham.isWithinBoundary(Geo.coords(52.288625, 4.777120))); // ~36R
        assertTrue(eham.isWithinBoundary(Geo.coords(52.287961, 4.734172))); // ~06
        assertTrue(eham.isWithinBoundary(Geo.coords(52.299601, 4.732929))); // Taxiway near to 36C
        assertTrue(eham.isWithinBoundary(Geo.coords(52.321257, 4.733173))); // Taxiway to 36L
        assertTrue(eham.isWithinBoundary(Geo.coords(52.328242, 4.708723))); // ~36L
        assertTrue(eham.isWithinBoundary(Geo.coords(52.362706, 4.711956))); // ~18R
        assertTrue(eham.isWithinBoundary(Geo.coords(52.331442, 4.739987))); // ~18C
        assertTrue(eham.isWithinBoundary(Geo.coords(52.334908, 4.736189))); // Taxiway near to ~18C
        assertTrue(eham.isWithinBoundary(Geo.coords(52.334630, 4.744642))); // Taxiway near to ~18C
        assertTrue(eham.isWithinBoundary(Geo.coords(52.316648, 4.746146))); // ~09
        assertTrue(eham.isWithinBoundary(Geo.coords(52.321409, 4.780173))); // ~18L
        assertTrue(eham.isWithinBoundary(Geo.coords(52.318406, 4.796876))); // ~27
        assertTrue(eham.isWithinBoundary(Geo.coords(52.311530, 4.809542))); // The most eastern apron
    }
}
