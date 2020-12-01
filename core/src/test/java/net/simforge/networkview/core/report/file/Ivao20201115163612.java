package net.simforge.networkview.core.report.file;

import junit.framework.TestCase;
import net.simforge.commons.io.IOHelper;
import net.simforge.networkview.core.Network;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Ivao20201115163612 extends TestCase {

    //MRB08:574816:574816:PILOT::2.14601e-316:1648:1648:0:1/B738/M-SDE2E3FGHIRWXY/LB1:N0445:ENBR:F390:EFHK:SHARD1:B:4:5314:0:50:0:I:1515:1515:1:34:2:52:EETN:PBN/A1B1C1D1S1S2 DOF/201115 REG/FVOLA EET/ESAA0034 EFIN0059 SEL/BQAF CODE/4B1620 RVR/200 OPR/MRB ORGN/LFJLMRBX PER/C CS/MIRABEL RMK/TCAS:GOKAB L24 EVLAN DCT IBVUT Y363 LAKUT:::::::20201115152947:Altitude/win:1.10.0b:2:4::S:122:0:0:16:

    public void test() throws IOException {
        InputStream is = Class.class.getResourceAsStream("/net/simforge/networkview/core/report/file/ivao-20201115163612.txt");
        String content = IOHelper.readInputStream(is);
        ReportFile reportFile = new ReportFile(Network.IVAO, content);

        List<ReportFile.ClientInfo> pilotInfos = reportFile.getClientInfos(ReportFile.ClientType.PILOT);
        for (ReportFile.ClientInfo pilotInfo : pilotInfos) {
            if (pilotInfo.getCid() == 574816) {
                assertEquals(0.0, pilotInfo.getLatitude(), 1E-321);
            }
        }
    }
}
