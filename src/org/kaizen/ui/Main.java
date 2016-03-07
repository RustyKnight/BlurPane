/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kaizen.ui;

import core.ui.InfiniteProgressPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.kaizen.ui.blurred.BlurFilter;
import org.kaizen.ui.blurred.BlurPane;
import org.kaizen.ui.blurred.BorderShape;
import org.kaizen.ui.blurred.blur.FastBlurFilter;
import org.kaizen.ui.core.GraphicsUtilities;
import org.kaizen.ui.blurred.RoundedBorderShape;

/**
 * Main example used to demonstrate the use of the blurred component API
 * @author Shane Whitehead
 */
public class Main {
	
	protected static final Logger LOGGER = Logger.getLogger("BlurredComponent");

	public static void main(String[] args) {
		PatternLayout pl = new PatternLayout("[%d{HH:mm:ss}][%t][%-5p] %C.%M:%L: %m%n");
		ConsoleAppender appender = new ConsoleAppender(pl);
		Logger.getRootLogger().addAppender(appender);
		
		new Main();
	}

	public Main() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
						ex.printStackTrace();
					}

					JFrame frame = new JFrame("Testing");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(new BackgroundPane());
					OverlayPane overlayPane = new OverlayPane();
					frame.setGlassPane(overlayPane);
					overlayPane.setVisible(true);
					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public class OverlayPane extends JPanel {

		private BufferedImage doom;

		private Point ballPoint = new Point(0, 0);
		private Point ballDelta;
		private double angle;

		private double rotationDelta = 10;

		private Map<JComponent, Point> mapDeltas = new HashMap<>(25);

		public OverlayPane() throws IOException {
			
			setOpaque(false);

			BorderShape borderShape = new RoundedBorderShape(20, Color.GRAY);
			FastBlurFilter filter = new FastBlurFilter();
			
			// Normally, I'd make use of appropriate layout managers, but, because
			// of the nature of the example, I wanted to animate the components 
			// to demonstrate the blurring effects

			Random rnd = new Random();
			setLayout(null);
			for (int index = 0; index < 5; index++) {
				
				BlurPane blurPane = makeBlurPane(borderShape, filter);
				
				Point blurPoint = new Point(rnd.nextInt(200 - blurPane.getWidth()), rnd.nextInt(200 - blurPane.getHeight()));
				blurPane.setLocation(blurPoint);

				Point blurDelta = new Point();

				blurDelta.x = rnd.nextInt(3) + 1;
				blurDelta.y = rnd.nextInt(3) + 1;

				if (rnd.nextBoolean()) {
					blurDelta.x *= -1;
				}
				if (rnd.nextBoolean()) {
					blurDelta.y *= -1;
				}

				mapDeltas.put(blurPane, blurDelta);

				add(blurPane);
			}

			doom = ImageIO.read(getClass().getResource("/Doom.png"));

			ballDelta = new Point();

			ballDelta.x = rnd.nextInt(3) + 1;
			ballDelta.y = rnd.nextInt(3) + 1;

			if (rnd.nextBoolean()) {
				ballDelta.x *= -1;
			}
			if (rnd.nextBoolean()) {
				ballDelta.y *= -1;
			}

			Timer timer = new Timer(40, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ballPoint.x += ballDelta.x;
					ballPoint.y += ballDelta.y;
					angle += rotationDelta;

					if (ballPoint.x + doom.getWidth() > getWidth()) {
						ballPoint.x = getWidth() - doom.getWidth();
						ballDelta.x *= -1;
					} else if (ballPoint.x < 0) {
						ballPoint.x = 0;
						ballDelta.x *= -1;
					}
					if (ballPoint.y + doom.getHeight() > getHeight()) {
						ballPoint.y = getHeight() - doom.getHeight();
						ballDelta.y *= -1;
					} else if (ballPoint.y < 0) {
						ballPoint.y = 0;
						ballDelta.y *= -1;
					}

					for (Entry<JComponent, Point> entry : mapDeltas.entrySet()) {
						JComponent blurPane = entry.getKey();
						Point blurDelta = entry.getValue();
						Point blurPoint = blurPane.getLocation();

						blurPoint.x += blurDelta.x;
						blurPoint.y += blurDelta.y;
						if (blurPoint.x + blurPane.getWidth() > getWidth()) {
							blurPoint.x = getWidth() - blurPane.getWidth();
							blurDelta.x *= -1;
						} else if (blurPoint.x < 0) {
							blurPoint.x = 0;
							blurDelta.x *= -1;
						}
						if (blurPoint.y + blurPane.getHeight() > getHeight()) {
							blurPoint.y = getHeight() - blurPane.getHeight();
							blurDelta.y *= -1;
						} else if (blurPoint.y < 0) {
							blurPoint.y = 0;
							blurDelta.y *= -1;
						}

						blurPane.setLocation(blurPoint);
					}

					repaint();
				}
			});
			timer.start();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();

			AffineTransform at = new AffineTransform();
			at.translate(ballPoint.x, ballPoint.y);
			at.rotate(Math.toRadians(angle), doom.getWidth() / 2, doom.getHeight() / 2);
			g2d.setTransform(at);
			GraphicsUtilities.applyQualityRenderingHints(g2d);
			g2d.drawImage(doom, 0, 0, this);
			g2d.dispose();
		}

		protected  BlurPane makeBlurPane(BorderShape borderShape, BlurFilter filter) {
				BlurPane blurPane = new BlurPane();
				blurPane.setLayout(new GridBagLayout());

				InfiniteProgressPane iip = new InfiniteProgressPane();
				iip.setOpaque(false);
				iip.setTickHighlightColor(Color.WHITE);
				iip.setRadius(20);
				iip.setTickCount(10);
				blurPane.add(iip);
				iip.start();

				JLabel label = new JLabel("I am your master");
				label.setForeground(Color.WHITE);
				blurPane.add(label);

				blurPane.setBorder(new EmptyBorder(20, 20, 20, 20));
				blurPane.setBorderShape(borderShape);
				blurPane.setBlurFilter(filter);
				blurPane.setSize(blurPane.getPreferredSize());
				return blurPane;
		}

	}

	public class BackgroundPane extends JPanel {

		private BufferedImage background;

		public BackgroundPane() throws IOException {
			background = ImageIO.read(getClass().getResource("/Background.jpg"));
		}

		@Override
		public Dimension getPreferredSize() {
			return background == null ? new Dimension(200, 200) : new Dimension(background.getWidth(), background.getHeight());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g.create();
			int x = (getWidth() - background.getWidth()) / 2;
			int y = (getHeight() - background.getHeight()) / 2;
			g2d.drawImage(background, x, y, this);

			g2d.setColor(Color.WHITE);
			g2d.drawLine(0, 0, getWidth(), getHeight());
			g2d.drawLine(getWidth(), 0, 0, getHeight());
			g2d.dispose();
		}

	}

}
