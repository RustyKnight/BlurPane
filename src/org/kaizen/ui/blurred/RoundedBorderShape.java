package org.kaizen.ui.blurred;

import org.kaizen.ui.core.ImageUtilities;
import org.kaizen.ui.core.GraphicsUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;

/**
 * A rounded border implementation of the border shape
 * @author swhitehead
 */
public class RoundedBorderShape extends AbstractBorderShape {

    private float radius;

    private Map<JComponent, RoundRectangle2D> shapes;

    public RoundedBorderShape(float radius, Color color) {
        super(color);
        this.radius = radius;

        shapes = new WeakHashMap<>(25);
    }

    public float getRadius() {
        return radius;
    }

    @Override
    protected Shape getBasicShape(JComponent parent, int border) {
        RoundRectangle2D shape = shapes.get(parent);
        if (shape == null) {
            
            int x = Math.max(0, border - 1);
            int y = Math.max(0, border - 1);
            int width = parent.getWidth() - border;
            int height = parent.getHeight() - border;

            shape = new RoundRectangle2D.Float(x, y, width, height, getRadius(), getRadius());
        }
        return shape;
    }

    @Override
    public void paint(Graphics2D g2d, JComponent parent) {
        Shape shape = getBasicShape(parent, 3);

        g2d = (Graphics2D) g2d.create();
        GraphicsUtilities.applyQualityRenderingHints(g2d);
        g2d.setColor(getColor());
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(shape);
        g2d.dispose();
    }

    @Override
    public BufferedImage applyMask(BufferedImage img, JComponent parent) {
        BufferedImage mask = ImageUtilities.createCompatibleImage(parent.getSize());
        Graphics2D maskg = mask.createGraphics();
        GraphicsUtilities.applyQualityRenderingHints(maskg);
        maskg.setColor(getColor());
        maskg.fill(getBasicShape(parent, 3));
        maskg.dispose();

        return ImageUtilities.applyMask(img, mask);
    }

}
