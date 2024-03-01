/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package guesstimator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author rider
 */
public class Guesstimator extends JFrame {

    private final BufferedImage originalPic;
    private BufferedImage testPic;
    private BufferedImage currentPic;

    private int currentGoodness;

    public Guesstimator(final String originalPicPath) throws IOException {
        super();

        setExtendedState(MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        originalPic = ImageIO.read(new File(originalPicPath));
        currentPic = new BufferedImage(originalPic.getWidth(), originalPic.getHeight(), BufferedImage.TYPE_INT_ARGB);
        testPic = new BufferedImage(originalPic.getWidth(), originalPic.getHeight(), BufferedImage.TYPE_INT_ARGB);

        currentGoodness = calculateGoodness(originalPic, currentPic);

        add(new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                g.drawImage(originalPic, 0, 0, getWidth(), getHeight(), null);
            }
        });

        final JPanel currentPanel = new JPanel() {
            @Override
            public void paint(final Graphics g) {
                super.paint(g);

                g.drawImage(currentPic, 0, 0, getWidth(), getHeight(), null);
            }
        };
        
        add(currentPanel);

        setVisible(true);

        new Thread("blobagen") {
            @Override
            public void run() {
                Graphics2D g2 = testPic.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                long startTime = System.currentTimeMillis();
                    
                while (true) {
                    g2.drawImage(currentPic, 0, 0, testPic.getWidth(), testPic.getHeight(), null);

                    g2.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random()));
                    
                    int cap = 1000;
                    int x = (int) (Math.random() * testPic.getWidth());
                    int y = (int) (Math.random() * testPic.getHeight());
                    int width = (int) (Math.random() * (Math.min(cap, testPic.getWidth() - x)));
                    int height = (int) (Math.random() * (Math.min(cap, testPic.getHeight() - y)));
                    
                    if (Math.random() > 0.5) {
                        g2.fillRect(x, y, width, height);
                    } else {
                        g2.fillOval(x, y, width, height);
                    }

                    int newGoodness = calculateGoodness(originalPic, testPic);

                    if (newGoodness < currentGoodness) {
                        System.out.println("new goodness = " + newGoodness + " after " + (System.currentTimeMillis() - startTime) + "ms");
                        startTime = System.currentTimeMillis();
                        currentGoodness = newGoodness;

                        currentPic.getGraphics().drawImage(testPic, 0, 0, currentPic.getWidth(), currentPic.getHeight(), null);

                        currentPanel.repaint();
                    }
                }
            }

        }.start();
    }

    int calculateGoodness(final BufferedImage originalImage,
            final BufferedImage newImage) {        
        int goodness = 0;
        
        int[] origPix = originalImage.getRGB(0, 0, originalImage.getWidth(), originalImage.getHeight(), null, 0, originalImage.getWidth());
        int[] newPix = newImage.getRGB(0, 0, newImage.getWidth(), newImage.getHeight(), null, 0, newImage.getWidth());

        for (int index = 0; index < newPix.length; ++index) {
            goodness += Math.abs((origPix[index] & 0xff) - (newPix[index] & 0xff));
            goodness += Math.abs(((origPix[index] & 0xff00) >> 8) - ((newPix[index] & 0xff00) >> 8));
            goodness += Math.abs(((origPix[index] & 0xff0000) >> 16) - ((newPix[index] & 0xff0000) >> 16));
        }

        return goodness;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Guesstimator gm = new Guesstimator("/home/rider/Downloads/IMG_1465(1).bmp");
        //Guesstimator gm = new Guesstimator(args[0]);
    }

}
