package net.simforge.networkview.core.report.file;

import junit.framework.TestCase;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.Network;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Ivao20151207212608 extends TestCase {

    // ATN511:178484:178484:PILOT::-37.7166:147.341:21311:424:1/B752/M-SDFGKRWY/S:N0470:YMES:F370:YBTH:EU8:B:4:2000:0:50:0:I:2125:2125:1:10:3:30:YSSY:OPR/AIR TRANSPORT INT REG/N620DL OPR/VIRTUAL PILOTS ASSOCIATIONS RMK/AIS/MET CHARTS ONBOARD  RMK/ TEXT ONLY RMK/AU2015V8SC LEG 11 PBN/A1B1C1D1L1O1S1 NAV/RNVD1E2A1 DOF/151128 REG/ATI RVR/200:ESL W290 CB W423 BTH:::::::20151207210641:IvAp:2.0.2:2:4::N:4:27:0:9:
    // ATR11D1L1O1S1 NAV/RNVD1E2A1 DOF/151128 REG/ATI RVR/200:ESL W290 CB W423 BTH:::::::20151207210641:IvAp:2.0.2:2:4::N:4:28:0:9:
    // ATR1077:507634:507634:PILOT::35.6819:51.3325:3977:0:1/A320/M-SDE1FGHJ3RWY/SD1:M078:OIII:F330:OIMM:EU8:B:3:2000:0:50:0:I:2100:2100:1:5:2:40:OIMS:PBN/A1B1 RMK/TCAS EQUIPPED  REG/EPIEE OPR/ATRAK VIRTUAL AIRLINE DOF/081215:DHN B411 METKI:::::::20151207204339:IvAp:2.0.2:2:3:OIKK:S:175:82:1:0:

    // 1st and 3rd lines are correct, the 2nd line is invalid
    // there is no clientInfo for the 2nd line, test checks it
    // log entry for invalid line can be out of database fields limits, test checks is

    public void test() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/networkview/core/report/file/ivao-20151207212608.txt");
        String content = IOHelper.readInputStream(is);
        ReportFile reportFile = new ReportFile(Network.IVAO, content);

        List<ReportFile.ClientInfo> pilotInfos = reportFile.getClientInfos(ReportFile.ClientType.PILOT);
        for (int i = 0; i < pilotInfos.size(); i++) {
            ReportFile.ClientInfo pilotInfo = pilotInfos.get(i);
            if (pilotInfo.getCid() == 178484) {
                ReportFile.ClientInfo nextPilotInfo = pilotInfos.get(i + 1);
                assertEquals(507634, nextPilotInfo.getCid());
                break;
            }
        }

        List<ReportFile.LogEntry> log = reportFile.getLog();
        for (ReportFile.LogEntry logEntry : log) {
            assertTrue("Section length shall be shorter than 50: " + logEntry.getSection(), logEntry.getSection().length() <= 50);
            assertTrue("Object length shall be shorter than 50: " + logEntry.getObject(), logEntry.getObject().length() <= 50);
            assertTrue("Msg length shall be shorter than 200: " + logEntry.getMsg(), logEntry.getMsg().length() <= 200);
            assertTrue("Value length shall be shorter than 1000: " + logEntry.getValue(), logEntry.getValue().length() <= 1000);
        }
    }
}
