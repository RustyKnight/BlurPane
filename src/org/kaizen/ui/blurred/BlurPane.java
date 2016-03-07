package org.kaizen.ui.blurred;

import org.kaizen.ui.core.ColorUtilities;
import org.kaizen.ui.blurred.blur.FastBlurFilter;
import core.ui.ImageUtilities;
import core.util.LoggerUtilities;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Base component.
 * @author swhitehead
 */
public class BlurPane extends JPanel {

    private BorderShape borderShape;
    private BlurFilter blurFilter;

    public BlurPane() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setOpaque(false);
        setBlurFilter(new FastBlurFilter());
        // Transparent background color
				// The background color is painted over the blurred image
				// basically ignoring the opaque state, while this might seem
				// counter intutive, this provides a lot of flexibility to deciding
				// just how opaque the blurring should look
        setBackground(ColorUtilities.applyAlpha(getBackground(), 0f));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                invalidateBuffers();
            }
        });
    }

    protected void invalidateBuffers() {
        BorderShape borderShape = getBorderShape();
        if (borderShape != null) {
            borderShape.invalidate(this);
        }
    }

    public BorderShape getBorderShape() {
        return borderShape;
    }

    public void setBorderShape(BorderShape shape) {
        if (shape != null) {
            this.borderShape = shape;
            repaint();
        }
    }

    public BlurFilter getBlurFilter() {
        return blurFilter;
    }

    public void setBlurFilter(BlurFilter filter) {
        if (blurFilter != filter) {
            this.blurFilter = filter;
            repaint();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateBuffers();
    }

    protected BufferedImage captureSnapShot() {
        JComponent parent = (JComponent) getParent();
        BufferedImage snapShot = null;
        if (parent.getClientProperty(PaintUtilities.PAINTING_BLUR) == null) {
            parent.putClientProperty(PaintUtilities.PAINTING_BLUR, true);
            try {
                snapShot = PaintUtilities.makeSnapShotFor((JComponent) parent, getBounds(), this);
            } finally {
                parent.putClientProperty(PaintUtilities.PAINTING_BLUR, null);
            }
        } else {
            LoggerUtilities.LOGGER.warn("Is painting because of blur");
        }

        return snapShot;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BorderShape borderShape = getBorderShape();
        Graphics2D g2d = (Graphics2D) g.create();

        JComponent parent = (JComponent) getParent();
        if (parent.getClientProperty(PaintUtilities.PAINTING_BLUR) == null) {
            BufferedImage snapshot = captureSnapShot();
            try {
                int x = Math.max(0, getX());
                int y = Math.max(0, getY());
                int width = Math.min(getWidth(), snapshot.getWidth());
                int height = Math.min(getHeight(), snapshot.getHeight());

                try {
                    BufferedImage blur = snapshot.getSubimage(x, y, width, height);
                    BlurFilter filter = getBlurFilter();
                    if (filter != null) {
                        blur = filter.blur(this, blur);
                    }

                    if (borderShape != null) {
                        BufferedImage mask = ImageUtilities.createCompatibleImage(ImageUtilities.imageSize(blur));
                        Graphics2D mg = mask.createGraphics();
                        mg.setClip(borderShape.getShape(this));
                        mg.drawImage(blur, 0, 0, this);
                        mg.dispose();
                        blur = mask;
                    }
                    g2d.drawImage(blur, 0, 0, this);
                } catch (RasterFormatException exp) {
                    LoggerUtilities.LOGGER.error("Parent size = " + parent.getSize());
                    LoggerUtilities.LOGGER.error("Cache size = " + snapshot.getWidth() + "x" + snapshot.getHeight());
                    LoggerUtilities.LOGGER.error("Me = " + getBounds());
                    LoggerUtilities.LOGGER.error("SubImageSize = " + x + "x" + y + "x" + width + "x" + height);
                }
            } finally {
                parent.putClientProperty(PaintUtilities.PAINTING_BLUR, null);
            }
        }
        
        g2d.dispose();
        g2d = (Graphics2D) g.create();
        Shape clip = g2d.getClip();
        if (borderShape != null) {
            g2d.setClip(borderShape.getShape(this));
        }
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setClip(clip);

        g2d.dispose();
        g2d = (Graphics2D) g.create();
        if (borderShape != null) {
            borderShape.paint(g2d, this);
        }
        g2d.dispose();
    }

}
