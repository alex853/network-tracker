package net.simforge.networkview.core.report.file;

import junit.framework.TestCase;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.Network;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Ivao20161031020356 extends TestCase {

    // CPV1097:507617:507617:PILOT::-23.9969:-50.5843:12396:291:1/E190/M-S/C:M450:SBPA:F340:SBLO:EU4:B:3:2007:0:50:0:I:200:0:4:50:3:0:SBMG:EQPT/SDE2FGHIM1RWXYZ:CXS UZ5 BOLIP:::::::20161031002605:X-IvAp/win:0.3.4:2:3::G:175:31012905:IvAp:2.0.2:2:4::S:288:102:0:9:
    // CPV1097:507617:507617:PILOT::-23.9678:-50.6148:12104:289:1/E190/M-S/C:M450:SBPA:F340:SBLO:EU4:B:3:2007:0:50:0:I:200:0:4:50:3:0:SBMG:EQPT/SDE2FGHIM1RWXYZ:CXS UZ5 BOLIP:::::::20161031002605:X-IvAp/win:0.3.4:2:3::G:175:316:0:14:

    // heading on the first row is incorrect

    public void test() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/networkview/core/report/file/ivao-20161031020356.txt");
        String content = IOHelper.readInputStream(is);
        ReportFile reportFile = new ReportFile(Network.IVAO, content);

        List<ReportFile.ClientInfo> pilotInfos = reportFile.getClientInfos(ReportFile.ClientType.PILOT);
        for (int i = 0; i < pilotInfos.size(); i++) {
            ReportFile.ClientInfo pilotInfo = pilotInfos.get(i);
            if (pilotInfo.getCid() == 507617) {
                assertTrue(pilotInfo.getHeading() >= 0 && pilotInfo.getHeading() <= 360);
            }
        }
    }
}
