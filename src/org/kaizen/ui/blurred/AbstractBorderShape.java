package org.kaizen.ui.blurred;

import org.kaizen.ui.core.ImageUtilities;
import org.kaizen.ui.core.GraphicsUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;

/**
 * Abstract implementation of the border
 * @author swhitehead
 */
public abstract class AbstractBorderShape implements BorderShape {

    private final Color color;

    private final Map<JComponent, Map<Integer, Shape>> shapes;

    public AbstractBorderShape(Color color) {
        this.color = color;

        shapes = new WeakHashMap<>(25);
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void invalidate(JComponent comp) {
        shapes.remove(comp);
    }
    
    protected Shape getShapeFor(JComponent parent, int border) {
        Shape shape = null;
        Map<Integer, Shape> cache = shapes.get(parent);
        if (cache != null) {
            shape = cache.get(border);
        }
        return shape;
    }
    
    protected void setShapeFor(JComponent parent, Shape shape, int border) {
        Map<Integer, Shape> cache = shapes.get(parent);
        if (cache == null) {
            cache = new HashMap<>(25);
            shapes.put(parent, cache);
        }
        cache.put(border, shape);
    }

    @Override
    public Shape getShape(JComponent comp) {
        return getBasicShape(comp, 3);
    }

    protected abstract Shape getBasicShape(JComponent parent, int border);

    @Override
    public void paint(Graphics2D g2d, JComponent parent) {
        Shape shape = getBasicShape(parent, 3);
        g2d = (Graphics2D) g2d.create();
        GraphicsUtilities.applyQualityRenderingHints(g2d);
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(shape);
        g2d.dispose();
    }

    @Override
    public BufferedImage applyMask(BufferedImage img, JComponent parent) {
        BufferedImage mask = ImageUtilities.createCompatibleImage(parent.getSize());
        Graphics2D maskg = mask.createGraphics();
        GraphicsUtilities.applyQualityRenderingHints(maskg);
        maskg.setColor(Color.WHITE);
        maskg.fill(getBasicShape(parent, 3));
        maskg.dispose();

        return ImageUtilities.applyMask(img, mask);
    }

}
