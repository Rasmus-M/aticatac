package vdp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Rasmus
 * Date: 22-01-2015
 * Time: 20:07
 */
public abstract class VDPFrame extends JFrame implements Runnable, WindowListener, MouseListener {

    public static final int SCALE = 3;
    public static final int WIDTH = VDPCanvas.WIDTH * SCALE;
    public static final int HEIGHT = VDPCanvas.HEIGHT * SCALE;

    protected VDPCanvas vdpCanvas;
    private boolean running;
    protected boolean paused;
    private String[] args;

    public VDPFrame(String[] args) {
        this.args = args;
        setSize(WIDTH, HEIGHT);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGHT) / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
    }

    public void run() {
        vdpCanvas = new VDPCanvas(SCALE, VDPCanvas.TRANSPARENT);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(vdpCanvas, BorderLayout.CENTER);
        pack();
        setVisible(true);
        initVDPCanvas(vdpCanvas);

        running = true;
        Thread drawingThread = new Thread(
            new Runnable() {
                public void run() {
                    VectorGraphicsRenderer renderer = new VectorGraphicsRenderer(vdpCanvas);
                    int frameNo = 0;
                    while (running) {
                        if (!paused) {
                            vdpCanvas.startFrame();
                            vdpCanvas.clear();
                            drawFrame(renderer, frameNo++);
                            vdpCanvas.endFrame();
                        }
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            running = false;
                        }
                    }
                    dispose();
                }
            }
        );
        drawingThread.start();
    }

    protected void initVDPCanvas(VDPCanvas vdpCanvas) {
        vdpCanvas.init();
        vdpCanvas.addMouseListener(this);
    }

    protected abstract void drawFrame(VectorGraphicsRenderer renderer, int frameNo);

    public void savePatternTable(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(vdpCanvas.getBitmapPatternTable());
        fos.close();
    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowClosing(WindowEvent e) {
        running = false;
    }

    public void windowClosed(WindowEvent e) {

    }

    public void windowIconified(WindowEvent e) {

    }

    public void windowDeiconified(WindowEvent e) {

    }

    public void windowActivated(WindowEvent e) {

    }

    public void windowDeactivated(WindowEvent e) {

    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        paused = true;
    }

    public void mouseReleased(MouseEvent e) {
        paused = false;
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }
}
