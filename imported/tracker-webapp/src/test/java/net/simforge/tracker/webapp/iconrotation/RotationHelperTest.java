package net.simforge.tracker.webapp.iconrotation;

import junit.framework.TestCase;
import net.simforge.tracker.webapp.util.IconHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class RotationHelperTest extends TestCase {

    public void testLoad() throws IOException {
        BufferedImage img = loadImage();

        assertNotNull(img);
        assertEquals(20, img.getWidth());
        assertEquals(20, img.getHeight());
    }

    public void testRotateIcon90() throws IOException {
        BufferedImage img = loadImage();
        BufferedImage rotatedImg = IconHelper.rotateIcon(img, 90);

        File outputFile = new File("test-output/rotationHelperTest/testRotateIcon90.png");
        //noinspection ResultOfMethodCallIgnored
        outputFile.getParentFile().mkdirs();
        ImageIO.write(rotatedImg, "png", outputFile);
    }

    private BufferedImage loadImage() throws IOException {
        InputStream is = IconHelper.class.getResourceAsStream("/net/simforge/tracker/webapp/iconrotation/vataware-mapicon.png");
        return IconHelper.load(is);
    }
}
