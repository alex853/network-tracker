package net.simforge.tracker.webapp.util;

import net.simforge.commons.misc.Str;
import net.simforge.tracker.webapp.util.IconHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class IconTester {
    public static void main(String[] args) throws IOException {
//        String filename = "tracker-webapp\\src\\main\\webapp\\images\\iconfinder\\1449665309_airplane_32x32.png";
        String filename = "plane_20.png";
        BufferedImage img = IconHelper.load(new FileInputStream(filename));

        int angle = 0;
        int step = 10;
        while (angle < 360) {
            BufferedImage rotated = IconHelper.rotateIcon(img, angle);
            ImageIO.write(rotated, "png", new File(filename + "." + Str.z(angle, 3) + ".png"));

            BufferedImage grayed = IconHelper.makeImageGray(rotated);
            ImageIO.write(grayed, "png", new File(filename + "." + Str.z(angle, 3) + ".G.png"));

            BufferedImage transparent = IconHelper.makeImageTransparent(grayed, 128);
            ImageIO.write(transparent, "png", new File(filename + "." + Str.z(angle, 3) + ".GT.png"));

            angle += step;
        }
    }

    public static void main2(String[] args) throws IOException {
        String filename = "tracker-webapp\\src\\main\\webapp\\images\\plane_20.png";
        BufferedImage img = IconHelper.load(new FileInputStream(filename));

        BufferedImage img2 = IconHelper.makeImageGray(img);

        ImageIO.write(img2, "png", new File("plane_20_gray.png"));
    }

    public static void main3(String[] args) throws IOException {
        String filename = "plane_20_gray.png";
        BufferedImage img = IconHelper.load(new FileInputStream(filename));

        BufferedImage img2 = IconHelper.changeColor(img, new Color(180, 40, 40));

        ImageIO.write(img2, "png", new File("plane_20_2.png"));
    }
}
