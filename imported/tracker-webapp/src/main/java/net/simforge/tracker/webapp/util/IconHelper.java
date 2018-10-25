package net.simforge.tracker.webapp.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;

public class IconHelper {
    public static BufferedImage load(InputStream is) throws IOException {
        return ImageIO.read(is);
    }

    public static BufferedImage rotateIcon(BufferedImage src, double angle) {
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(angle), src.getWidth() / 2.0, src.getHeight() / 2.0);

        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        op.filter(src, dst);

        return dst;
    }

    public static BufferedImage makeImageGray(BufferedImage sourceImg) {
        BufferedImage image = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < sourceImg.getWidth(); ++x) {
            for (int y = 0; y < sourceImg.getHeight(); ++y) {
                int rgb = sourceImg.getRGB(x, y);

                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                int grayLevel = (r + g + b) / 3;
                int gray = (a << 24) +  (grayLevel << 16) + (grayLevel << 8) + grayLevel;

                image.setRGB(x, y, gray);
            }
        }

        return image;
    }

    public static BufferedImage changeColor(BufferedImage sourceImg, Color targetColor) {
        BufferedImage image = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        double coeffR = targetColor.getRed() / 255.0;
        double coeffG = targetColor.getGreen() / 255.0;
        double coeffB = targetColor.getBlue() / 255.0;

        for (int x = 0; x < sourceImg.getWidth(); ++x) {
            for (int y = 0; y < sourceImg.getHeight(); ++y) {
                int rgb = sourceImg.getRGB(x, y);

                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                r = (int) Math.round(r * coeffR);
                g = (int) Math.round(g * coeffG);
                b = (int) Math.round(b * coeffB);

                rgb = (a << 24) +  (r << 16) + (g << 8) + b;

                image.setRGB(x, y, rgb);
            }
        }

        return image;
    }

    public static BufferedImage makeImageTransparent(BufferedImage sourceImg, int newAlpha) {
        BufferedImage image = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < sourceImg.getWidth(); ++x) {
            for (int y = 0; y < sourceImg.getHeight(); ++y) {
                int rgb = sourceImg.getRGB(x, y);

                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                int newA = a < newAlpha ? a : newAlpha;
                int newRgb = (newA << 24) + (r << 16) + (g << 8) + b;

                image.setRGB(x, y, newRgb);
            }
        }

        return image;
    }
}
