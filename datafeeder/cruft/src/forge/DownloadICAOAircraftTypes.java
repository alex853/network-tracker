package forge;

import forge.commons.io.IOHelper;
import forge.commons.html.Html;

import java.io.IOException;
import java.io.File;

public class DownloadICAOAircraftTypes {
    public static void main(String[] args) throws IOException {
        String content = IOHelper.download("http://www.icao.int/anb/ais/8643/MnfctrerList.cfm");
        IOHelper.saveFile(new File("1.html"), content);
        String content2 = Html.toPlainText(content);
        IOHelper.saveFile(new File("1.txt"), content2);
    }
}
