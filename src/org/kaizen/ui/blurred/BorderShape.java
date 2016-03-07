package org.kaizen.ui.blurred;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * Defines the border which is painted around the component. The border is important
 * as it provides a means to frame the "overflow" of the blur so it's not so
 * abrupt a change
 * 
 * The intention is, the border can be shared and provides a "shape" which is
 * used to clip the resulting image with
 * @author swhitehead
 */
public interface BorderShape {
    
    public void paint(Graphics2D g2d, JComponent parent);
    public BufferedImage applyMask(BufferedImage img, JComponent parent);
    
    public Shape getShape(JComponent comp);
    public void invalidate(JComponent comp);
    
}
