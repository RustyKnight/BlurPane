/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kaizen.ui;

import core.ui.GraphicsUtilities;
import core.ui.InfiniteProgressPane;
import core.util.LoggerUtilities;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

/** 
 * This is simpler example, which makes use of a static component
 * @author swhitehead
 */
public class MainNormal {

	public static void main(String[] args) {
		LoggerUtilities.configureStandardOutProperties();
		new MainNormal();
	}

	public MainNormal() {
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
					frame.add(new TestPane());
					frame.pack();
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	public class TestPane extends JPanel {

		private BufferedImage doom;

		private double angle;
		private double rotationDelta = 10;

		private BufferedImage background;

		public TestPane() throws IOException {

			background = ImageIO.read(getClass().getResource("/Background.jpg"));

			setLayout(new GridBagLayout());
			add(new NormalPane());

			doom = ImageIO.read(getClass().getResource("/Doom.png"));

			Timer timer = new Timer(40, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					angle += rotationDelta;

					repaint();
				}
			});
			timer.start();
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

			AffineTransform at = new AffineTransform();
			x = (getWidth() - doom.getWidth()) / 2;
			y = (getHeight() - doom.getHeight()) / 2;
			at.translate(x, y);
			at.rotate(Math.toRadians(angle), doom.getWidth() / 2, doom.getHeight() / 2);
			g2d.setTransform(at);
			GraphicsUtilities.applyQualityRenderingHints(g2d);
			g2d.drawImage(doom, 0, 0, this);
			g2d.dispose();
		}

	}

	public class NormalPane extends JPanel {

		public NormalPane() {
			setBorder(new EmptyBorder(10, 10, 10, 10));

			InfiniteProgressPane ipp = new InfiniteProgressPane();
			ipp.setRadius(20);
			ipp.setTickCount(10);
			ipp.setOpaque(false);
			ipp.setTickHighlightColor(Color.WHITE);
			add(ipp);
			ipp.start();

			JLabel label = new JLabel("I'm your master");
			label.setForeground(Color.WHITE);
			add(label);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			RoundRectangle2D border = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 40, 40);
			
			LinearGradientPaint lgp = new LinearGradientPaint(
							new Point(0, 0),
							new Point(0, getHeight()),
							new float[]{0f, 1f},
							new Color[]{Color.GRAY, Color.DARK_GRAY}
			);
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setComposite(AlphaComposite.SrcOver.derive(0.75f));
			g2d.setPaint(lgp);
			g2d.fill(border);
			g2d.dispose();

			RoundRectangle2D shape = (RoundRectangle2D) border.clone();
			shape.setFrame(2, 2, getWidth() - 3, getHeight() - 3);

			g2d = (Graphics2D) g.create();
			GraphicsUtilities.applyQualityRenderingHints(g2d);
			g2d.setColor(Color.DARK_GRAY);
			g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2d.draw(shape);
			g2d.dispose();
		}

	}

}
