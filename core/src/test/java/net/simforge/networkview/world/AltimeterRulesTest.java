package net.simforge.networkview.world;

import junit.framework.TestCase;
import net.simforge.atmosphere.Atmosphere;
import net.simforge.commons.misc.Geo;

public class AltimeterRulesTest extends TestCase {
    public void testInvalid() {
        AltimeterRules rules = AltimeterRules.get(new Geo.Coords(-90, 0), 1013);
        assertFalse(rules.isValid());

        try {
            rules.getAltimeterPolicy();
            fail();
        } catch (Exception e) {
            // no op
        }

        try {
            rules.getTransitionAltitude();
            fail();
        } catch (Exception e) {
            // no op
        }

        try {
            rules.getTransitionLevel();
            fail();
        } catch (Exception e) {
            // no op
        }

        try {
            rules.getActualAltitude(10000);
            fail();
        } catch (Exception e) {
            // no op
        }
    }

    // See document "Approach Radar Control" at http://www.vatsim-uk.co.uk/download/
    public void testTL() {
        Geo.Coords coords = new Geo.Coords(50, 10); // somewhere in Germany, TA is 5000

        assertEquals(6000, AltimeterRules.get(coords, Atmosphere.QNH_STD_PRECISE).getTransitionLevel());
        assertEquals(5000, AltimeterRules.get(coords, 1051).getTransitionLevel());
        assertEquals(7000, AltimeterRules.get(coords, 1010).getTransitionLevel());
    }
}
