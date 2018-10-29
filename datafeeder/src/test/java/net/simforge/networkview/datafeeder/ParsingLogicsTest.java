package net.simforge.networkview.datafeeder;

import junit.framework.TestCase;
import net.simforge.networkview.datafeeder.persistence.ReportPilotPosition;

public class ParsingLogicsTest extends TestCase {

    //== parseRegNo tests ==============================================================================================

    public void testParseRegNo_REGinRemarks() {
        assertEquals("GDBAA", ParsingLogics.parseRegNo(mockPosition("BAW2771"), "+VFPS+/V/PBN/A1B1C1D1L1O1S1 NAV/RNVD1E2A1 DOF/151206 REG/GDBAA EET/GMMM0021 LPPC0111 LECM0148 LFFF0225 EGTT0314 RVR/75 PER/C OPR/BAVIRTUAL"));
    }

    public void testParseRegNo_inCallsign() {
        assertEquals("PTISG", ParsingLogics.parseRegNo(mockPosition("PT-ISG"), ""));
    }

    public void testParseRegNo_nowhere() {
        assertNull(ParsingLogics.parseRegNo(mockPosition("VIR33C"), "+VFPS+/T/ OPR/VIRGINVIRTUALGROUP.CO.UK"));
    }

    private ReportPilotPosition mockPosition(String callsign) {
        ReportPilotPosition position = new ReportPilotPosition();
        position.setCallsign(callsign);
        return position;
    }

    //== recognizeRegNo tests ==========================================================================================

    public void testRecognizeRegNo_emptyInput() {
        assertNull(ParsingLogics.recognizeRegNo(""));
        assertNull(ParsingLogics.recognizeRegNo(null));
    }

    public void testRecognizeRegNo_PT_ISC() {
        assertEquals("PT-ISC", ParsingLogics.recognizeRegNo("PT-ISC"));
    }

    public void testRecognizeRegNo_callsign_TOM3AY() {
        assertNull(ParsingLogics.recognizeRegNo("TOM3AY"));
    }

    public void testRecognizeRegNo_GDBAA() {
        assertEquals("GDBAA", ParsingLogics.recognizeRegNo("GDBAA"));
    }

    public void testRecognizeRegNo_G_DBAA() {
        assertEquals("G-DBAA", ParsingLogics.recognizeRegNo("G-DBAA"));
    }

    // postponed - see TRACKER-41
/*    public void testRecognizeRegNo_GDBAAX() {
        assertNull(ParsingLogics.recognizeRegNo("GDBAAX"));
    }*/

    public void testRecognizeRegNo_G_DBAAX() {
        assertNull(ParsingLogics.recognizeRegNo("G_DBAAX"));
    }

    //== parseAircraftType tests =======================================================================================

    public void testParseAircraftType_B_T154_G() {
        assertEquals("T154", ParsingLogics.parseAircraftType("B/T154/G"));
    }

    public void testParseAircraftType_A320_G() {
        assertEquals("A320", ParsingLogics.parseAircraftType("A320/G"));
    }

    public void testParseAircraftType_1_B772_HSDRWY_S() {
        assertEquals("B772", ParsingLogics.parseAircraftType("1/B772/H-SDRWY/S"));
    }
}