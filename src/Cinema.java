import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 图片逐帧播放器
 */
public class Cinema extends JFrame {
    private IMGPanel IMP;
    private BufferedImage IMG;
    private int I = 1;

    public Cinema(String title) {
        setTitle(title);
        setSize(986, 1010);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void launch() {
        setVisible(true);
        addImageLabel();
    }

    private void NextFrame() {
        System.out.print("-");
        File imageFile = new File("D:/temp-Java/CellTrace/maps/" + I + ".png");
        IMG = IMGKit.inputIMG(imageFile.getAbsolutePath());
        I ++;
        IMP.update();
        if (I >= 276)
            timer.cancel();
    }

    private java.util.Timer timer;
    private void addImageLabel() {
        IMP = new IMGPanel();
        add(IMP);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NextFrame();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NextFrame();
            }
        }, 1000, 10);
    }
    /**
     * 图像板
     */
    private class IMGPanel extends JComponent {
        public IMGPanel() {
            setLocation(6, 6);
            setSize(960, 960);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }
        public void update() {repaint();}
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(IMG, 0, 0, getWidth() , getHeight(), this);
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            timer.cancel();
            dispose();
        }
        super.processWindowEvent(e);
    }


    public static void main(String[] args) {
        Cinema C = new Cinema("Cinema");
        C.launch();
    }

}
