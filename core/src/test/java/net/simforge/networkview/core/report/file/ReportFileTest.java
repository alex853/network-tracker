package net.simforge.networkview.core.report.file;

import junit.framework.TestCase;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.Network;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReportFileTest extends TestCase {
    public void testVatsimReport() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/networkview/core/report/file/vatsim-data.txt");
        String content = IOHelper.readInputStream(is);
        ReportFile reportFile = new ReportFile(Network.VATSIM, content);

        ReportFile.Section general = reportFile.getSection("GENERAL");
        assertNotNull(general);
        assertEquals("20150924055102", general.getValue("UPDATE"));

        // checking section that can be used as "end of file" condition
        assertNotNull(reportFile.getSection("SERVERS"));

        List<ReportFile.ClientInfo> pilotInfos = reportFile.getClientInfos(ReportFile.ClientType.PILOT);

        assertEquals(147, pilotInfos.size());

        ReportFile.ClientInfo pilotInfo = pilotInfos.get(0);
        assertEquals("9M-AFH", pilotInfo.getCallsign());
        assertEquals(1304120, pilotInfo.getCid());
        assertEquals(2.43054, pilotInfo.getLatitude());
        assertEquals(101.95664, pilotInfo.getLongitude());
        assertEquals(2342, pilotInfo.getAltitude());
        assertEquals(192, pilotInfo.getGroundspeed());
        assertEquals("A320/A", pilotInfo.getPlannedAircraft());
        assertEquals("WMKN", pilotInfo.getPlannedDepAirport());
        assertEquals("WMKK", pilotInfo.getPlannedDestAirport());
        assertEquals("WMKN 222300Z 21003KT 9999 -RA SCT020 BKN150 26/24 Q1010 WMKK 222300Z 01003KT 4000 HZ FEW030 BKN140 BKN270 23/22 Q1011 BECMG 5000 HZ /v/ SEL/BQHP", pilotInfo.getPlannedRemarks());
        assertEquals(305, pilotInfo.getHeading());
        assertEquals(1011, pilotInfo.getQnhMb().intValue());
    }

    public void testIvaoReport() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/networkview/core/report/file/ivao-20150924055024.txt");
        String content = IOHelper.readInputStream(is);
        ReportFile reportFile = new ReportFile(Network.IVAO, content);

        ReportFile.Section general = reportFile.getSection("GENERAL");
        assertNotNull(general);
        assertEquals("20150924055024", general.getValue("UPDATE"));

        // checking section that can be used as "end of file" condition
        assertNotNull(reportFile.getSection("SERVERS"));

        List<ReportFile.ClientInfo> pilotInfos = reportFile.getClientInfos(ReportFile.ClientType.PILOT);

        assertEquals(184, pilotInfos.size());

        ReportFile.ClientInfo pilotInfo = pilotInfos.get(0);
        assertEquals("AAL095", pilotInfo.getCallsign());
        assertEquals(446158, pilotInfo.getCid());
        assertEquals(6.14871, pilotInfo.getLatitude());
        assertEquals(-75.4241, pilotInfo.getLongitude());
        assertEquals(7035, pilotInfo.getAltitude());
        assertEquals(0, pilotInfo.getGroundspeed());
        assertEquals(null, pilotInfo.getPlannedAircraft());
        assertEquals(null, pilotInfo.getPlannedDepAirport());
        assertEquals(null, pilotInfo.getPlannedDestAirport());
        assertEquals(null, pilotInfo.getPlannedRemarks());
        assertEquals(71, pilotInfo.getHeading());
        assertTrue(pilotInfo.isOnGround());

        pilotInfo = pilotInfos.get(1);
        assertEquals("ABW985", pilotInfo.getCallsign());
        assertEquals(488466, pilotInfo.getCid());
        assertEquals(23.8765, pilotInfo.getLatitude());
        assertEquals(59.2771, pilotInfo.getLongitude());
        assertEquals(33854, pilotInfo.getAltitude());
        assertEquals(503, pilotInfo.getGroundspeed());
        assertEquals("1/B748/H-SDE1FGHIJ1RWXYZ/LB1", pilotInfo.getPlannedAircraft());
        assertEquals("PGUM", pilotInfo.getPlannedDepAirport());
        assertEquals("OMDB", pilotInfo.getPlannedDestAirport());
        assertEquals("PBN/A1B1C1D1L1O1S1 NAV/RNVD1E2A1 DOF/150923 REG/VQBRH", pilotInfo.getPlannedRemarks());
        assertEquals(291, pilotInfo.getHeading());
        assertFalse(pilotInfo.isOnGround());
    }
}