package org.kaizen.ui.blurred.blur;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * An abstract implementation of AbstractImageOpFilter
 * @author swhitehead
 */
public abstract class AbstractImageOpFilter implements BufferedImageOp {

    public abstract BufferedImage filter(BufferedImage src, BufferedImage dest);

    /**
     * {@inheritDoc}
     */
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage createCompatibleDestImage(BufferedImage src,
            ColorModel destCM) {
        if (destCM == null) {
            destCM = src.getColorModel();
        }

        return new BufferedImage(destCM,
                destCM.createCompatibleWritableRaster(
                        src.getWidth(), src.getHeight()),
                destCM.isAlphaPremultiplied(), null);
    }

    /**
     * {@inheritDoc}
     */
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        return (Point2D) srcPt.clone();
    }

    /**
     * {@inheritDoc}
     */
    public RenderingHints getRenderingHints() {
        return null;
    }
}
