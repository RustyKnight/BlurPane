package org.kaizen.ui.blurred;

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * The implementation of the blur algorithm to use. This provides some flexibility
 * to the API as the filter can be changed as needed, without the need to extend
 * the base components
 */
public interface BlurFilter {
    
    public BufferedImage blur(JComponent parent, BufferedImage source);
    
}
