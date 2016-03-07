package org.kaizen.ui.blurred;

import org.kaizen.ui.core.ImageUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * This is a series of utilities used to generate a snapshot of the parent
 * component, excluding the child component, within the current paint cycle,
 * so we can use it to generate the blur affect.
 * 
 * This is one big hack, based around using a lot of reflection
 * 
 * I've played around a lot with trying to only generate a snapshot of the area
 * which is covered by the child component, but while it seems to sometimes
 * work, I tend to end up up with a bunch of other artifacts which I can
 * reconcile, so I'm still capturing the entire parent component
 * 
 * The process is some what complex.  The basic idea is, I need to paint the
 * area of the parent covered by the component, without painting the component.
 * 
 * This needs to be done without removing or changing the visibility state of the
 * component in question, as this could generate new paint events, which we
 * want to avoid.
 * 
 * This could be (more easily) done by creating a special container, but I wanted
 * to avoid that and make it so the component could be added to what ever container
 * you had
 * 
 * The answer is, mimic the painting process ... 
 */
public class PaintUtilities {

    public static final Object PAINTING_BLUR = "PaintingBlur";
		
		private static Map<JComponent, BufferedImage> mapBuffers = new WeakHashMap<>(25);

    protected static boolean getFlag(JComponent parent, String flagName) {
        return (boolean) ReflectionToolbox.invoke(parent, "getFlag").with(int.class, (int) ReflectionToolbox.getFieldValue(parent, flagName)).execute();
    }

    protected static void setFlag(JComponent parent, String flagName, boolean value) {
        ReflectionToolbox.invoke(parent, "setFlag").with(int.class, (int) ReflectionToolbox.getFieldValue(parent, flagName)).with(boolean.class, value).execute();
    }
		
		/**
		 * This maintains a cache of BufferedImages which makes it faster to paint
		 * then having to create (and destroy) them repeatedly
		 * @param comp
		 * @return 
		 */
		protected static BufferedImage getBufferFor(JComponent comp) {
			BufferedImage img = mapBuffers.get(comp);
			if (img == null || !ImageUtilities.imageSize(img).equals(comp.getSize())) {
				img = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_ARGB);
				mapBuffers.put(comp, img);
			}
			return img;
		}
		
		/**
		 * Creates a snapshot of the area of the parent covered by the specified
		 * component. This is done by painting the parent without the specified
		 * component.
		 * @param component
		 * @param clippingBounds
		 * @param ignore
		 * @return 
		 */
		public static BufferedImage makeSnapShotFor(JComponent component, Rectangle clippingBounds, JComponent... ignore) {
			BufferedImage snapShot = getBufferFor(component);
			ImageUtilities.clear(snapShot);
			Graphics2D shg = snapShot.createGraphics();
			Container parent = component.getParent();
			if (!component.isOpaque() && parent != null && parent instanceof JComponent) {
				BufferedImage parentSnapShot = makeSnapShotFor((JComponent)parent, component.getBounds(), component);
				BufferedImage subImage = parentSnapShot.getSubimage(component.getX(), component.getY(), component.getWidth(), component.getHeight());
				shg.drawImage(subImage, 0, 0, null);
			}
			shg.setClip(clippingBounds);
			paint(component, shg, ignore);
			shg.dispose();
			return snapShot;
		}

		/**
		 * Mimiced painting process
		 * @param parent
		 * @param g
		 * @param ignore 
		 */
    protected static void paint(JComponent parent, Graphics g, JComponent... ignore) {
        setFlag(parent, "IS_PRINTING_ALL", true);
        setFlag(parent, "IS_PRINTING", true);
        try {
            boolean shouldClearPaintFlags = false;
            if ((parent.getWidth() <= 0) || (parent.getHeight() <= 0)) {
                return;
            }
            //			Graphics componentGraphics = getComponentGraphics(g);
            //			Graphics co = componentGraphics.create();
            Graphics co = g.create();
            try {
                RepaintManager repaintManager = RepaintManager.currentManager(parent);
                Rectangle clipRect = co.getClipBounds();
                int clipX;
                int clipY;
                int clipW;
                int clipH;
                if (clipRect == null) {
                    clipX = clipY = 0;
                    clipW = parent.getWidth();
                    clipH = parent.getHeight();
                } else {
                    clipX = clipRect.x;
                    clipY = clipRect.y;
                    clipW = clipRect.width;
                    clipH = clipRect.height;
                }
                if (clipW > parent.getWidth()) {
                    clipW = parent.getWidth();
                }
                if (clipH > parent.getHeight()) {
                    clipH = parent.getHeight();
                }
                if (parent.getParent() != null && !(parent.getParent() instanceof JComponent)) {
                    ReflectionToolbox.invoke(parent, "adjustPaintFlags").execute();
                    //					adjustPaintFlags();
                    shouldClearPaintFlags = true;
                }
                int bw;
                int bh;
                boolean printing = getFlag(parent, "IS_PRINTING");
                //				boolean printing = getFlag(IS_PRINTING);
                //				if (!printing && repaintManager.isDoubleBufferingEnabled()
                //						&& !getFlag(ANCESTOR_USING_BUFFER) && isDoubleBuffered()
                //						&& (getFlag(IS_REPAINTING) || repaintManager.isPainting())) {
                boolean isRepaintManagerPainting = (boolean) ReflectionToolbox.invoke(repaintManager, "isPainting").execute();
                if (!printing && repaintManager.isDoubleBufferingEnabled() && !getFlag(parent, "ANCESTOR_USING_BUFFER") && parent.isDoubleBuffered() && (getFlag(parent, "IS_REPAINTING") || isRepaintManagerPainting)) {
                    //					repaintManager.beginPaint();
                    ReflectionToolbox.invoke(repaintManager, "beginPaint");
                    try {
                        //						repaintManager.paint(this, this, co, clipX, clipY, clipW, clipH);
                        ReflectionToolbox.invoke(repaintManager, "paint").with(JComponent.class, parent).with(JComponent.class, parent).with(Graphics.class, co).with(int.class, clipX).with(int.class, clipY).with(int.class, clipW).with(int.class, clipH).execute();
                    } finally {
                        //						repaintManager.endPaint();
                        ReflectionToolbox.invoke(repaintManager, "endPaint");
                    }
                } else {
                    // Will ocassionaly happen in 1.2, especially when printing.
                    if (clipRect == null) {
                        co.setClip(clipX, clipY, clipW, clipH);
                    }
                    boolean isObsured = (boolean) ReflectionToolbox.invoke(parent, "rectangleIsObscured").with(int.class, clipX).with(int.class, clipY).with(int.class, clipW).with(int.class, clipH).execute();
                    //					if (!rectangleIsObscured(clipX, clipY, clipW, clipH)) {
                    if (!isObsured) {
                        if (!printing) {
                            //							paintComponent(co);
                            //							paintBorder(co);
                            //							ReflectionToolbox.invoke(parent, "paintComponent").
                            //									with(Graphics.class, co).
                            //									execute();
                            //							ReflectionToolbox.invoke(parent, "paintBorder").
                            //									with(Graphics.class, co).
                            //									execute();
                            paintComponent(parent, co);
                            paintBorder(parent, co);
                        } else {
                            //							printComponent(co);
                            //							printBorder(co);
                            //							ReflectionToolbox.invoke(parent, "printComponent").
                            //									with(Graphics.class, co).
                            //									execute();
                            //							ReflectionToolbox.invoke(parent, "printBorder").
                            //									with(Graphics.class, co).
                            //									execute();
                            printComponent(parent, co);
                            printBorder(parent, co);
                        }
                    }
                    if (!printing) {
                        //						paintChildren(co);
                        //							ReflectionToolbox.invoke(parent, "paintChildren").
                        //									with(Graphics.class, co).
                        //									execute();
                        paintChildren(parent, co, ignore);
                    } else {
                        //						printChildren(co);
                        //							ReflectionToolbox.invoke(parent, "printChildren").
                        //									with(Graphics.class, co).
                        //									execute();
                        printChildren(parent, co, ignore);
                    }
                }
            } finally {
                co.dispose();
                if (shouldClearPaintFlags) {
                    //					setFlag(ANCESTOR_USING_BUFFER, false);
                    //					setFlag(IS_PAINTING_TILE, false);
                    //					setFlag(IS_PRINTING, false);
                    //					setFlag(IS_PRINTING_ALL, false);
                    setFlag(parent, "ANCESTOR_USING_BUFFER", false);
                    setFlag(parent, "IS_PAINTING_TILE", false);
                    setFlag(parent, "IS_PRINTING", false);
                    setFlag(parent, "IS_PRINTING_ALL", false);
                }
            }
        } finally {
            setFlag(parent, "IS_PRINTING", false);
            setFlag(parent, "IS_PRINTING_ALL", false);
        }
    }

    protected static void paintComponent(JComponent parent, Graphics co) {
        ReflectionToolbox.invoke(parent, "paintComponent").with(Graphics.class, co).execute();
    }

    protected static void paintBorder(JComponent parent, Graphics co) {
        ReflectionToolbox.invoke(parent, "paintBorder").with(Graphics.class, co).execute();
    }

    protected static void printComponent(JComponent parent, Graphics co) {
        ReflectionToolbox.invoke(parent, "printComponent").with(Graphics.class, co).execute();
    }

    protected static void printBorder(JComponent parent, Graphics co) {
        ReflectionToolbox.invoke(parent, "printBorder").with(Graphics.class, co).execute();
    }

    protected static void paintChildren(JComponent parent, Graphics g, JComponent... ignore) {
        Graphics sg = g;
        List<Component> toIgnore = new ArrayList<>(25);
        if (ignore != null) {
            toIgnore.addAll(Arrays.asList(ignore));
        }
        synchronized (parent.getTreeLock()) {
            int i = parent.getComponentCount() - 1;
            if (i < 0) {
                return;
            }
            // If we are only to paint to a specific child, determine
            // its index.
            Component paintingChild = (Component) ReflectionToolbox.getFieldValue(parent, "paintingChild");
            if (paintingChild != null && (paintingChild instanceof JComponent) && paintingChild.isOpaque()) {
                for (; i >= 0; i--) {
                    if (parent.getComponent(i) == paintingChild) {
                        break;
                    }
                }
            }
            //				Rectangle tmpRect = fetchRectangle();
            Rectangle tmpRect = (Rectangle) ReflectionToolbox.invoke(parent, "fetchRectangle").execute();
            boolean checkIfChildObscuredBySibling = (boolean) ReflectionToolbox.invoke(parent, "checkIfChildObscuredBySibling").execute();
            boolean checkSiblings = !parent.isOptimizedDrawingEnabled() && checkIfChildObscuredBySibling;
            Rectangle clipBounds = null;
            if (checkSiblings) {
                clipBounds = sg.getClipBounds();
                if (clipBounds == null) {
                    clipBounds = new Rectangle(0, 0, parent.getWidth(), parent.getHeight());
                }
            }
            boolean printing = getFlag(parent, "IS_PRINTING");
            final Window window = SwingUtilities.getWindowAncestor(parent);
            final boolean isWindowOpaque = window == null || window.isOpaque();
            for (; i >= 0; i--) {
                Component comp = parent.getComponent(i);
                if (comp == null) {
                    continue;
                }
                if (!toIgnore.contains(comp)) {
                    final boolean isJComponent = comp instanceof JComponent;
                    // Enable painting of heavyweights in non-opaque windows.
                    // See 6884960
                    if ((!isWindowOpaque || isJComponent || JComponent.isLightweightComponent(comp)) && comp.isVisible()) {
                        Rectangle cr;
                        cr = comp.getBounds(tmpRect);
                        boolean hitClip = g.hitClip(cr.x, cr.y, cr.width, cr.height);
                        if (hitClip) {
                            if (checkSiblings && i > 0) {
                                int x = cr.x;
                                int y = cr.y;
                                int width = cr.width;
                                int height = cr.height;
                                SwingUtilities.computeIntersection(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, cr);
                                int obscuredState = (int) ReflectionToolbox.invoke(parent, "getObscuredState").with(int.class, i).with(int.class, cr.x).with(int.class, cr.y).with(int.class, cr.width).with(int.class, cr.height).execute();
                                int completelyObsured = (int) ReflectionToolbox.getFieldValue(parent, "COMPLETELY_OBSCURED");
                                if (obscuredState == completelyObsured) {
                                    continue;
                                }
                                cr.x = x;
                                cr.y = y;
                                cr.width = width;
                                cr.height = height;
                            }
                            Graphics cg = sg.create(cr.x, cr.y, cr.width, cr.height);
                            cg.setColor(comp.getForeground());
                            cg.setFont(comp.getFont());
                            boolean shouldSetFlagBack = false;
                            try {
                                if (isJComponent) {
                                    if (getFlag(parent, "ANCESTOR_USING_BUFFER")) {
                                        setFlag((JComponent) comp, "ANCESTOR_USING_BUFFER", true);
                                        shouldSetFlagBack = true;
                                    }
                                    if (getFlag(parent, "IS_PAINTING_TILE")) {
                                        setFlag((JComponent) comp, "IS_PAINTING_TILE", true);
                                        shouldSetFlagBack = true;
                                    }
                                    if (!printing) {
                                        comp.paint(cg);
                                    } else if (!getFlag(parent, "IS_PRINTING_ALL")) {
                                        comp.print(cg);
                                    } else {
                                        comp.printAll(cg);
                                    }
                                } else // The component is either lightweight, or
                                // heavyweight in a non-opaque window
                                {
                                    if (!printing) {
                                        comp.paint(cg);
                                    } else if (!getFlag(parent, "IS_PRINTING_ALL")) {
                                        comp.print(cg);
                                    } else {
                                        comp.printAll(cg);
                                    }
                                }
                            } finally {
                                cg.dispose();
                                if (shouldSetFlagBack) {
                                    setFlag((JComponent) comp, "ANCESTOR_USING_BUFFER", false);
                                    setFlag((JComponent) comp, "IS_PAINTING_TILE", false);
                                }
                            }
                        }
                    }
                }
            }
            ReflectionToolbox.invoke(parent, "recycleRectangle").with(Rectangle.class, tmpRect).execute();
        }
    }

    protected static void printChildren(JComponent parent, Graphics co, JComponent... ignore) {
        paintChildren(parent, co, ignore);
    }

}
