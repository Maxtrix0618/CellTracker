import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * 手动序列器，逐帧手动标记生成细胞运动序列
 */
public class Sequencer extends JFrame {
    private final String workPath = "D:/temp-Java/CellTrace/";
    private final ArrayList<Frame> frames = new ArrayList<>();
    private Cell target;
    private IMGPanel IMP;
    private BufferedImage IMG;
    private BufferedImage IMG_store;
    private BufferedWriter notepad;
    private boolean clock = false;
    private int I = 1;  // current frame order

    public Sequencer(String title) {
        setTitle(title);
        setSize(540, 560);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void launch() {
        initial();
        setVisible(true);
        inputFilmData();
        addImageLabel();
        NextFrame();
    }

    private void initial() {
        String dataPath = workPath + "cell_data/";
        int N = Objects.requireNonNull(new File(dataPath).listFiles()).length;
        try {
            notepad = new BufferedWriter(new FileWriter(dataPath + (N+1) + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void terminal() {
        System.out.println("/40");
        fin = true;
        try {
            notepad.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        dispose();
        System.out.println("terminal.");
    }
    private boolean fin = false;


    private void NextFrame() {
        clock = false;
        System.out.print(I);
        File imageFile = new File(workPath + "done/c" + I + ".png");
        IMG = IMGKit.inputIMG(imageFile.getAbsolutePath());
        if (I >= 40) {
            terminal();
            return;
        }
        if (IMG != null) {
            IMG_store = IMGKit.CloneNewIMG(IMG);
            IMP.update();
            clock = true;
        }
        I ++;
    }

    /**
     * 初始化图像框及所需Timer
     */
    private void addImageLabel() {
        IMP = new IMGPanel();
        add(IMP);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mainTimerRun();
            }
        }, 0, 100);
    }
    private java.util.Timer timer;
    private void mainTimerRun() {
        if (fin) timer.cancel();
        if (!clock) return;
        Point absPoint = MouseInfo.getPointerInfo().getLocation();
        int mX = 4 * IMGKit.LimitedValue(absPoint.x - this.getLocation().x, 0, 512 - 1);
        int mY = 4 * IMGKit.LimitedValue(absPoint.y - this.getLocation().y, 0, 512 - 1);
        extractIMG();
        target = frames.get(I-2).nearestCell(mX, mY);
        IMGKit.drawCircleD(IMG, new int[]{255, 255, 255, 255}, target.X, target.Y, target.R, 2);
        IMP.update();
    }

    /**
     * 初始化帧胞数据
     */
    private void inputFilmData() {
        try {
            List<String> lines = Files.readAllLines(Path.of(workPath + "film2.txt"));
            for (String line : lines) {
                String[] values = line.split(" ");
                int F = Integer.parseInt(values[0]);
                if (F > frames.size())
                    frames.add(new Frame());
                frames.get(F-1).addCell(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 帧：存储当前帧下所有细胞位置和半径信息
     */
    private static class Frame {
        public ArrayList<Cell> cells = new ArrayList<>();
        public Frame() {}
        public void addCell(String[] values) {
            int X = Integer.parseInt(values[1]);
            int Y = Integer.parseInt(values[2]);
            int R = Integer.parseInt(values[3]);
            if (R > 90) return;
            Cell cell = new Cell(X, Y, R);
            cells.add(cell);
        }
        public Cell nearestCell(int x, int y) {
            Cell t = cells.get(0);
            for (Cell c : cells) {
                if (IMGKit.distance(x, y, c.X, c.Y) < IMGKit.distance(x, y, t.X, t.Y))
                    t = c;
            }
            return t;
        }
    }
    private static class Cell {
        public int X, Y, R;
        public Cell(int x, int y, int r) {X = x; Y = y; R = r;}
        public String message() {return (X + " " + Y + " " + R);}
    }
    /**
     * 图像板
     */
    private class IMGPanel extends JComponent {
        public IMGPanel() {
            setLocation(6, 6);
            setSize(512, 512);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        }
        public void update() {repaint();}
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(IMG, 0, 0, getWidth() , getHeight(), this);
        }
        @Override
        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
            if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1) {     // BUTTON1 左键
                System.out.println("/40");
                try {
                    notepad.write(target.message() + "\n");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                NextFrame();
            }
            if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON3) {     // BUTTON3 右键
                System.out.println("/40");
                NextFrame();
            }
            if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON2) {     // BUTTON2 中键
                terminal();
            }
        }
    }

    private void extractIMG() {IMGKit.IMG_clone(IMG_store, IMG);}

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            timer.cancel();
            dispose();
        }
        super.processWindowEvent(e);
    }


    public static void main(String[] args) {
        Sequencer Sq = new Sequencer("Sequencer");
        Sq.launch();
    }

}
