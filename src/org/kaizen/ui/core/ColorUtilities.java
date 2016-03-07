package org.kaizen.ui.core;

import java.awt.Color;

/**
 * Common color utilities.
 */
public class ColorUtilities {

	/**
	 * Applies a alpha value to the specified color
	 * @param color
	 * @param alpha
	 * @return 
	 */
	public static Color applyAlpha(Color color, float alpha) {

		return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(255 * alpha));

	}

}
