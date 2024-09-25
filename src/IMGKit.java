import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 提供图像处理常用的静态计算方法，勿实例化<br/>
 * IMGKit / 2023.9.7 by_Maxtrix
 */
public class IMGKit {
    private IMGKit() {}

    /**
     * 最大值或最小值
     */
    public static int extreme(boolean max, int a, int b) {
        return (max) ? (Math.max(a,b)) : (Math.min(a,b));
    }

    /**
     * 计算坐标间距离
     */
    public static int distance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * 一个坐标周围的相对坐标
     */
    public static int[] dxs_4 = {-1, 1, 0, 0};
    public static int[] dys_4 = {0, 0, -1, 1};
    public static int[] dxs_8 = {-1, 0, 1, 1, 1, 0, -1, -1};
    public static int[] dys_8 = {-1, -1, -1, 0, 1, 1, 1, 0};

    /**
     * 若in在lo到high之间则返回in; 否则返回lo(in小于lo)或high(in大于high).
     */
    public static int LimitedValue(int in, int low, int high) {
        return Math.min(Math.max(in, low), high);
    }

    /**
     * 判断坐标位置是否超出图像边界
     */
    public static boolean outsideArea(BufferedImage BI, int x, int y) {
        return (x < 0 || y < 0 || x >= BI.getWidth() || y >= BI.getHeight());
    }

    /**
     * 在图像(x,y)处绘制(a,r,g,b)像素点.
     */
    public static void paint(BufferedImage BI, int a, int r, int g, int b, int x, int y) {
        if (outsideArea(BI, x, y)) return;
        setColor(BI, a, r, g, b, x, y);
    }
    public static void paint(BufferedImage BI, int[] ps, int x, int y) {
        paint(BI, ps[0], ps[1], ps[2], ps[3], x, y);
    }
    public static void setColor(BufferedImage BI, int a, int r, int g, int b, int x, int y) {
        int p = (a << 24) | (r << 16) | (g << 8) | b;
        BI.setRGB(x, y, p);
    }

    /**
     * 在图像的(x,y)处绘制空像素（将alpha和rgb色值全改为0）
     */
    public static void paintEmpty(BufferedImage BI, int x, int y) {
        paint(BI, 0, 0, 0, 0, x, y);
    }

    /**
     * 在图像的(x,y)处绘制满像素（将alpha和rgb色值全改为255）
     */
    public static void paintFull(BufferedImage BI, int x, int y) {
        paint(BI, 255, 255, 255, 255, x, y);
    }

    /**
     * 在图像的(x,y)处绘制原像素的反色像素
     */
    public static void paintRev(BufferedImage BI, int x, int y) {
        int[] ARGB = getPixel(BI, x, y);
        paint(BI, ARGB[0], 255-ARGB[1], 255-ARGB[2], 255-ARGB[3], x, y);
    }

    /**
     * 在图像上画出两点间的线段，宽度为d
     */
    public static void drawLine(BufferedImage BI, int[] ps, int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2)
            paint(BI, ps, x1, y1);
        else if (Math.abs(x1-x2) >= Math.abs(y1-y2)) {
            for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
                paint(BI, ps, x, ((x - x1) * (y1 - y2)) / (x1 - x2) + y1);
            }
        }
        else {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                paint(BI, ps, ((y - y1) * (x1 - x2)) / (y1 - y2) + x1, y);
            }
        }
    }
    /**
     * 在图像上画出两点间的线段，宽度为d
     */
    public static void drawLine(BufferedImage BI, int[] ps, int x1, int y1, int x2, int y2, int d) {
        for (int dx = -d; dx <= d; dx ++)
            for (int dy = -d; dy <= d; dy ++) {
                if (distance(dx, dy, 0, 0) > d) continue;
                drawLine(BI, ps, x1+dx, y1+dy, x2+dx, y2+dy);
            }
    }

    /**
     * 在图像上以(x,y)为圆心，r为半径画圆
     */
    public static void drawCircle(BufferedImage BI, int[] ps, int cx, int cy, int cr) {
        for (int x = cx-cr; x <= cx+cr; x++)
            for (int y = cy-cr; y <= cy+cr; y++) {
                if (distance(x, y, cx, cy) == cr)
                    paint(BI, ps, x, y);
            }
    }
    /**
     * 在图像上以(x,y)为圆心，r为半径，d为厚度画圆
     */
    public static void drawCircleD(BufferedImage BI, int[] ps, int cx, int cy, int cr, int cd) {
        for (int x = cx-cr; x <= cx+cr; x++)
            for (int y = cy-cr; y <= cy+cr; y++) {
                if (Math.abs(distance(x, y, cx, cy) - cr) <= cd)
                    paint(BI, ps, x, y);
            }
    }

    /**
     * 在图像上以(x,y)为圆心，r为半径画方框
     */
    public static void drawBox(BufferedImage BI, int[] ps, int cx, int cy, int cr) {
        for (int x = cx-cr; x <= cx+cr; x++) {
            paint(BI, ps, x, cy-cr);
            paint(BI, ps, x, cy+cr);
        }
        for (int y = cy-cr; y <= cy+cr; y++) {
            paint(BI, ps, cx-cr, y);
            paint(BI, ps, cx+cr, y);
        }
    }

    /**
     * 将图像全局填充(a,r,g,b)像素.
     */
    public static void fill(BufferedImage BI, int a, int r, int g, int b) {
        for (int x = 0; x < BI.getWidth(); x++)
            for (int y = 0; y < BI.getHeight(); y++)
                setColor(BI, a, r, g, b, x, y);
    }
    /**
     * 将图像全局填充为空像素.
     */
    public static void fillEmpty(BufferedImage BI) {
        fill(BI, 0, 0, 0, 0);
    }


    /**
     * 判断像素点是否“可容忍”：如果该位置像素点与参考背景像素色值bgARGB的r,g,b相差分别都不超过容忍度tole，则返回true，否则返回false.
     */
    public static boolean tolerable(BufferedImage BI, int[] bgARGB, int tole, int x, int y) {
        for (int i = 1; i <= 3; i++)
            if (Math.abs(getPixel(BI, x, y)[i] - bgARGB[i]) > tole)
                return false;
        return true;
    }

    /**
     * 给出图像IMG_DO在(x,y)处的像素色值信息，以int[4]返回（4个int数依此为a,r,g,b的值）
     */
    public static int[] getPixel(BufferedImage BI, int x, int y) {
        if (IMGKit.outsideArea(BI, x, y)) return new int[4];
        return getPixel(BI.getRGB(x, y));
    }
    public static int[] getPixel(int p) {
        int[] ARGB = new int[4];
        ARGB[0] = (p >> 24) & 0xff;
        ARGB[1] = (p >> 16) & 0xff;
        ARGB[2] = (p >> 8) & 0xff;
        ARGB[3] = p & 0xff;
        return ARGB;
    }


    /**
     * 返回一个与OriBI数据数组完全相同的新副本
     */
    public static BufferedImage CloneNewIMG(BufferedImage OriBI) {
        BufferedImage NewBI = new BufferedImage(OriBI.getWidth(), OriBI.getHeight(), OriBI.getType());
        IMG_clone(OriBI, NewBI);
        return NewBI;
    }
    /**
     * 将OriBI的数据数组复制到NewBI上
     */
    public static void IMG_clone(BufferedImage OriBI, BufferedImage NewBI) {
        NewBI.setData(OriBI.getData());
    }

    /**
     * 返回一个与OriBI色彩数组相同，但type不同的新副本
     */
    public static BufferedImage CloneNewIMG(BufferedImage OriBI, int type) {
        BufferedImage NewBI = new BufferedImage(OriBI.getWidth(), OriBI.getHeight(), type);
        for (int x = 0 ; x < OriBI.getWidth(); x++)
            for (int y = 0 ; y < OriBI.getHeight(); y++)
                NewBI.setRGB(x, y, OriBI.getRGB(x, y));
        return NewBI;
    }

    /**
     * 展示窗口背景色库
     */
    public static final Color[] EbgCs = new Color[]{
            new Color(0, 0, 0, 0),
            new Color(255, 255, 255, 255),
            new Color(0, 0, 0, 255),
            new Color(128, 128, 128, 255),
    };


    /**
     * 画虚线
     * @param w1 变坐标1
     * @param w2 变坐标2
     * @param z 定坐标
     * @param segment 间段长
     * @param horizon true:水平; false:竖直.
     */
    public static void drawDashLine(Graphics g, int w1, int w2, int z, int segment, boolean horizon) {
        if (w1 >= w2) return;

        double segN = (w2 - w1 +1.0) / segment;
        int reconSegN = (int) Math.ceil((segN+1) / 2);
        boolean Odd = (reconSegN %2 != 0);

        int start = (w2 + w1) / 2;
        int head = 0;
        int tail = segment/2 -1;
        if (Odd && start+tail <= w2 && start-tail >= w1) drawLineSymmetrically(g, start, head, tail, z, horizon);
        head = tail +1;
        tail = head + segment -1;
        Odd = !Odd;

        while (start+tail <= w2 && start-tail >= w1) {
            if (Odd) drawLineSymmetrically(g, start, head, tail, z, horizon);
            Odd = !Odd;
            head = tail +1;
            tail = head + segment -1;
        }
        if (Odd) drawLineSymmetrically(g, start, head, w2-start, z, horizon);

    }
    private static void drawLineSymmetrically(Graphics g, int start, int head, int tail, int z, boolean horizon) {
        if (horizon) {
            g.drawLine(start + head, z, start + tail, z);
            g.drawLine(start - head, z, start - tail, z);
        }
        else {
            g.drawLine(z, start + head, z, start + tail);
            g.drawLine(z, start - head, z, start - tail);
        }
    }

    /**
     * 从绝对路径读取图片
     */
    public static BufferedImage inputIMG(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 保存图片到绝对路径
     */
    public static void saveImage(BufferedImage BI, String path) {
        try {
            ImageIO.write(BI, "png", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
