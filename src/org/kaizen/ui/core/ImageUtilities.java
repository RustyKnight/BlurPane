package org.kaizen.ui.core;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Common image related utilities
 */
public class ImageUtilities {

    public static Dimension imageSize(BufferedImage cache) {
        return cache == null ? new Dimension(0, 0) : new Dimension(cache.getWidth(), cache.getHeight());
    }

    public static void clear(BufferedImage image) {

        Graphics2D g2d = image.createGraphics();

        GraphicsUtilities.clear(g2d, image.getWidth(), image.getHeight());

        g2d.dispose();

    }

    public static BufferedImage createCompatibleImage(Dimension size) {

        return createCompatibleImage(size.width, size.height);

    }

    public static BufferedImage createCompatibleImage(int width, int height) {

        return createCompatibleImage(width, height, Transparency.TRANSLUCENT);

    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency) {

        BufferedImage image = getGraphicsConfiguration().createCompatibleImage(width, height, transparency);
        image.coerceData(true);
        return image;

    }

    public static GraphicsConfiguration getGraphicsConfiguration() {

        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    }

    public static BufferedImage createCompatibleImage(BufferedImage image) {
        BufferedImage target = createCompatibleImage(image, image.getWidth(), image.getHeight());
        Graphics2D g2d = target.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return target;
    }

    public static BufferedImage createCompatibleImage(BufferedImage image,
            int width, int height) {
        return getGraphicsConfiguration().createCompatibleImage(width, height, image.getTransparency());
    }

    public static Image applyMask(Image sourceImage, Image maskImage) {
        return applyMask(sourceImage, maskImage, AlphaComposite.DST_IN);
    }

    public static Image applyMask(Image sourceImage, Image maskImage, int method) {

        BufferedImage maskedImage = null;
        if (sourceImage != null) {

            int width = maskImage.getWidth(null);
            int height = maskImage.getHeight(null);

            maskedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D mg = maskedImage.createGraphics();

            int x = (width - sourceImage.getWidth(null)) / 2;
            int y = (height - sourceImage.getHeight(null)) / 2;

            mg.drawImage(sourceImage, x, y, null);
            mg.setComposite(AlphaComposite.getInstance(method));

            mg.drawImage(maskImage, 0, 0, null);

            mg.dispose();
        }

        return maskedImage;

    }

    public static BufferedImage applyMask(BufferedImage sourceImage, BufferedImage maskImage, int method) {
        return (BufferedImage) applyMask((Image) sourceImage, (Image) maskImage, method);
    }

    public static BufferedImage applyMask(BufferedImage sourceImage, BufferedImage maskImage) {
        return (BufferedImage) applyMask((Image) sourceImage, (Image) maskImage, AlphaComposite.DST_IN);
    }
}
