package org.kaizen.ui.blurred.blur;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.kaizen.ui.blurred.BlurFilter;
import org.kaizen.ui.core.ImageUtilities;

/**
 * This is an implementation of the BlurFilter based around a StackedBlur
 * implementation
 */
public class FastBlurFilter implements BlurFilter {

    private StackBlurImageOpFilter filter = new StackBlurImageOpFilter(2, 4);
    private Map<JComponent, BufferedImage> cache = new WeakHashMap<>(25);

    @Override
    public BufferedImage blur(JComponent comp, BufferedImage source) {
        BufferedImage blur = cache.get(comp);
        if (blur == null || blur.getWidth() != source.getWidth() || blur.getHeight() != source.getHeight()) {
            blur = ImageUtilities.createCompatibleImage(source);
            cache.put(comp, blur);
        }
                
        Graphics2D blurg = blur.createGraphics();
        blurg.drawImage(source, filter, 0, 0);
        blurg.dispose();
        return blur;
    }

}
