import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 细胞定位 <br/>
 * CircleLocator / 2023.12.10 by_Maxtrix
 */
public class CircleLocator {
    private final String workPath = "D:/temp-Java/CellTrace/";
    private final String imageFormat = ".png";

    private int I;      // 当前图片序号

    private BufferedImage IMG_DO;
    private BufferedImage IMG_Store;

    private BufferedWriter notepad;

    private final Point stP = new Point();
    private final int[] bgARGB;     // 参数：背景色点色值
    private final int tole;         // 参数：消背景容差度
    private final int kl;           // 参数：膨胀与腐蚀核边长
    private final int dtv;          // 参数：二元化阈值
    private final int gpv;          // 参数：集群初筛阈值
    private final int cnv;          // 参数：目标集群数目


    public static void main(String[] args) {
        CircleLocator CL = new CircleLocator();
        CL.launch(1, 400);
    }


    public CircleLocator() {
        bgARGB = new int[]{0, 0, 0, 0};
        kl = 3;
        tole = 90;
        dtv = 10;
        cnv = 20;
        gpv = 0;
    }

    public void launch(int S, int N) {
        initial();
        for (I = S; I <= N; I++) {
            System.out.print(I + " -> ");
            if (!inputImage(I)) continue;

            ConventionalALGO();

            outputImage(I);
        }
        terminal();
    }

    /**
     * 常规算法
     */
    private void ConventionalALGO() {
        DeBackProgress(tole);
//        ExpCor(kl, false);
//        ExpCor(kl, true);
//        ExpCor(kl, false);
//        ExpCor(kl, true);
        Dualization(dtv);
        Clustering(gpv, cnv);
//        FillCircle();
        TestForClusterResult();
        Fitting();
    }


    /**
     * 局域搜索算法
     */
    private void LocalSearchALGO() {
        TestForSearchC();
        Settling();

        SubMerge();
        FittingL();


//        UpdateSearchC();
    }


    /**
     * 淹没，对每个聚落分别进行，不断提高底色筛选阈值直到剩余特定数目
     */
    private void SubMerge() {
        System.out.print("[Su] S-");
        for (Colony C : colonies) {
            int targetSN = C.size - C.T;

            int tol = tole;
            while (sinkNumber(tol, C) < targetSN) {
                tol += 5;
            }
            for (int i = 0; i < C.size; i++) {
                int[] p = C.get(i);
                if (IMGKit.tolerable(IMG_DO, bgARGB, tol, p[0], p[1]))
                    paintEmpty(p[0], p[1]);
            }
        }
        System.out.print("E. ");
    }
    /**
     * 淹没点数
     */
    private int sinkNumber(int tol, Colony C) {
        int num = 0;
        for (int i = 0; i < C.size; i++) {
            int[] p = C.get(i);
            if (IMGKit.tolerable(IMG_DO, bgARGB, tol, p[0], p[1]))
                num ++;
        }
        return num;
    }


    /**
     * 稀疏化
     */
    private void Thinning() {
        for (Colony C : colonies)
            for (int i = 0; i < C.size; i++) {
                int[] p = C.get(i);
                if (isIn(p[0], p[1]))
                    paintEmpty(p[0], p[1]);
            }

    }
    private boolean isIn(int x, int y) {
        if (getPixel(x, y)[1] == 0) return false;
        for (int k = 0; k < 4; k ++)
            if (IMGKit.getPixel(IMG_DO, x + IMGKit.dxs_4[k], y + IMGKit.dys_4[k])[1] == 0)
                return false;
        return true;
    }


    /**
     * 局域拟合
     */
    private void FittingL() {
        System.out.print("[Fi] S-");
        clusters = new ArrayList<>();
        for (Colony Co : colonies) {
            Cluster cl = new Cluster();
            clusters.add(cl);
            for (int i = 0; i < Co.size; i++) {
                int[] p = Co.get(i);
                if (getPixel(p[0], p[1])[1] > 0)
                    cl.append(p[0], p[1]);
            }
            cl.Calculate3();
            try {
                notepad.write(I + " " + cl.cX + " " + cl.cY + " " + cl.R + " \n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int CP = 255 / clusters.size();
        for (int T = 0; T < clusters.size(); T++) {
            clusters.get(T).color = new int[]{255, 255-(CP*T), CP*T, CP*T};
        }
        System.out.print("E. ");
        TestForFittingResult();
    }


    /**
     * 最初搜索圆
     */
    private void inputInitC() {
        colonies = new ArrayList<>();
//        colonies.add(new Colony(1310, 518, 140, 4800));
        colonies.add(new Colony(778, 1396, 140, 4800));
        colonies.add(new Colony(593, 1190, 140, 4800));
        colonies.add(new Colony(693, 1721, 140, 4800));
        colonies.add(new Colony(927, 715, 180, 4800));
    }



    /**
     * 聚落化
     */
    private void Settling() {
        System.out.print("[St] S-");
        for (int x = 0; x < IMG_DO.getWidth(); x++)
            for (int y = 0; y < IMG_DO.getHeight(); y++) {
                Colony C = InSearchC(x, y);
                if (C != null)
                    C.append(x, y, false);
                else paintEmpty(x, y);
            }
        System.out.print("E. ");
    }

    /**
     * 判断坐标是否在搜索圆范围之内，是则返回聚落，否则返回null
     */
    private Colony InSearchC(int x, int y) {
        for (Colony C : colonies)
            if (IMGKit.distance(x, y, C.X, C.Y) < C.R)
                return C;
        return null;
    }
    private ArrayList<Colony> colonies;   // 搜索圆（聚落）

    /**
     * 根据本帧数据刷新搜索圆范围，提供给下一帧使用
     */
    private void UpdateSearchC() {
        System.out.print("[UI] S-");
        ArrayList<Colony> new_colonies = new ArrayList<>();
        for (int i = 0; i < colonies.size(); i++)
            new_colonies.add(new Colony(calSearchC(clusters.get(i).circle())));
        colonies = new_colonies;
        System.out.print("E. ");
    }
    private int[] calSearchC(int[] circle) {
        int searchR = (circle[2] + 20);
        return new int[]{circle[0], circle[1], searchR, 4800};
    }


    /**
     * 开场
     */
    private void initial() {
        System.out.println("Locating Start.");
        try {
            notepad = new BufferedWriter(new FileWriter(workPath + "film.txt"));
            notepad.write("F  X  Y  R" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputInitC();
    }
    /**
     * 谢幕
     */
    private void terminal() {
        try {
            notepad.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ALL Missions Completed.");
    }


    /**
     * 去除底色
     */
    private void DeBackProgress(int tole) {
        System.out.print("[DB] S-");
        // BFS
        boolean[][] called = new boolean[IMG_DO.getWidth()][IMG_DO.getHeight()];    // 是否已加入即访队列
        Queue<Point> await = new LinkedList<>();    // 用于存储即将访问的节点的队列
        await.add(stP);

        while (!await.isEmpty()) {
            Point curr = await.poll();
            paintEmpty(curr.x, curr.y);
            for (int i = 0; i < 4; i++) {
                int x = curr.x + IMGKit.dxs_4[i];
                int y = curr.y + IMGKit.dys_4[i];
                if (outsideArea(x, y) || called[x][y]) continue;
                called[x][y] = true;
                if (IMGKit.tolerable(IMG_DO, bgARGB, tole, x, y)) {
                    await.add(new Point(x, y));
                }
            }
        }
        System.out.print("E.");
    }

    /**
     * 膨胀或腐蚀
     * 对于腐蚀，最小值为0
     * @param Kl 核边长
     * @param E - True:Exp, False:Corr.
     */
    private void ExpCor(int Kl, boolean E) {
        System.out.print((E ? "[Ex]" : "[Co]") + " S-");
        storageIMG(IMG_Store);
        for (int x = 0; x < IMG_DO.getWidth(); x++)
            for (int y = 0; y < IMG_DO.getHeight(); y++)
                doPaint(E, Kl, x, y);
        System.out.print("E. ");
    }
    private void doPaint(boolean E, int Kl, int x, int y) {
        int[] cp = IMGKit.getPixel(IMG_Store, x, y);
        if (E && cp[1] == 255) return;
        if (!E && (cp[1]+cp[2]+cp[3]) == 0) return;
        int KC = (Kl - 1) / 2;
        for (int i = 0; i < Kl; i++)
            for (int j = 0; j < Kl; j++) {
                int[] np = IMGKit.getPixel(IMG_Store, x + (i - KC), y + (j - KC));
                if (E && np[1] == 255) {
                    IMGKit.paintFull(IMG_DO, x, y);
                    return;
                }
                if (!E && (np[1]+np[2]+np[3]) == 0) {
                    IMGKit.paintEmpty(IMG_DO, x, y);
                    return;
                }
                for (int k = 0; k < cp.length; k++)
                    cp[k] = IMGKit.extreme(E, cp[k], np[k]);
                IMGKit.paint(IMG_DO, cp, x, y);
            }
    }


    /**
     * 平滑模糊
     * @param Kl 核边长
     */
    private void Smoothen(int Kl) {
        System.out.print("[Sm] S-");
        int KC = (Kl - 1) / 2;
        double[][] Kernel = new double[Kl][Kl];
        double KV = -1 / Math.pow(Kl, 2);
        for (int i = 0; i < Kernel.length; i++)
            for (int j = 0; j < Kernel[0].length; j++)
                Kernel[i][j] = KV;
        Kernel[KC][KC] += 2;
        storageIMG(IMG_Store);
        for (int x = 0; x < IMG_DO.getWidth(); x++)
            for (int y = 0; y < IMG_DO.getHeight(); y++) {
                int[] P = new int[4];
                for (int i = 0; i < Kernel.length; i++)
                    for (int j = 0; j < Kernel[i].length; j++)
                        for (int k = 0; k < P.length; k++)
                            P[k] += Kernel[i][j] * IMGKit.getPixel(IMG_Store, x + (i - KC), y + (j - KC))[k];
                IMGKit.paint(IMG_DO, P, x, y);
            }
        System.out.print("E. ");
    }


    /**
     * 二元化，呈现更清晰的明暗信息
     * @param dtv 色值和的阈值
     */
    private void Dualization(int dtv) {
        System.out.print("[Du] S-");
        IMGData = new int[IMG_DO.getWidth()][IMG_DO.getHeight()];
        for (int x = 0; x < IMG_DO.getWidth(); x++)
            for (int y = 0; y < IMG_DO.getHeight(); y++)
                if ((getPixel(x, y)[1] + getPixel(x, y)[2] + getPixel(x, y)[3] > dtv)) {
                    IMGData[x][y] = 1;
                    IMGKit.paintFull(IMG_DO, x, y);
                }
        System.out.print("E. ");
    }
    private int[][] IMGData;    // 二元化图像数据组，0-空 | 1-目标（无集群） | >1-目标（集群序号=数据-2）

    /**
     * 集群化
     * @param cnv 集群筛选的大小阈值（只取size最大的前cnv个集群，其余则抹除）
     */
    private void Clustering(int gpv, int cnv) {
        System.out.print("[Cl] S-");
        clusters = new ArrayList<>();
        for (int x = 0; x < IMGData.length; x++)
            for (int y = 0; y < IMGData[0].length; y++)
                if (IMGData[x][y] == 1) {
                    Cluster cluster = new Cluster();
                    int nv = clusters.size() + 2;       // 数据值更新到此值
                    int V = 0;

                    // BFS
                    boolean[][] called = new boolean[IMG_DO.getWidth()][IMG_DO.getHeight()];
                    Queue<Point> await = new LinkedList<>();
                    await.add(new Point(x, y));
                    while (!await.isEmpty()) {
                        Point curr = await.poll();
                        cluster.append(curr.x, curr.y);
                        cluster.V = nv;
                        V ++;
                        IMGData[curr.x][curr.y] = nv;
                        for (int i = 0; i < 4; i++) {
                            int xi = curr.x + IMGKit.dxs_4[i];
                            int yi = curr.y + IMGKit.dys_4[i];
                            if (outsideArea(xi, yi) || called[xi][yi]) continue;
                            called[xi][yi] = true;
                            if (IMGData[xi][yi] == 1)
                                await.add(new Point(xi, yi));
                            else
                                cluster.appendBound(curr.x, curr.y);
                        }
                    }
                    if (V >= gpv)
                        clusters.add(cluster);
                }
        // 集群筛选，只取size最大的前cnv个集群
        ArrayList<Cluster> stay = new ArrayList<>();
        while (stay.size() < cnv && clusters.size() > 0) {
            Cluster maxC = clusters.get(0);
            for (Cluster C : clusters)
                if (C.size > maxC.size)
                    maxC = C;
            clusters.remove(maxC);
            stay.add(maxC);
            maxC.calculate1();
        }
        clusters = stay;

        // 重整数据组与图像
        IMGData = new int[IMG_DO.getWidth()][IMG_DO.getHeight()];
        IMGKit.fillEmpty(IMG_DO);
        int order = 2;
        for (Cluster C : clusters) {
            for (int i = 0 ; i < C.size; i++) {
                int[] p = C.get(i);
                IMGData[p[0]][p[1]] = order;
                C.V = order;
                IMGKit.paintFull(IMG_DO, p[0], p[1]);
            }
            order ++;
        }

        // 为集群分配特征颜色
        int CP = 255 / clusters.size();
        for (int T = 0; T < clusters.size(); T++) {
            clusters.get(T).color = new int[]{255, 255-(CP*T), CP*T, CP*T};
        }

        System.out.print("E. ");
    }
    private ArrayList<Cluster> clusters;    // 集群列

    /**
     * 集群填充
     */
    private void FillCircle() {
        System.out.print("[Fi] S-");
        for (Cluster C : clusters)
            for (int x = C.lb; x <= C.rb; x++)
                for (int y = C.ub; y <= C.db; y++)
                    if (IMGData[x][y] == 0)
                        if (beClosed(C, x, y)) {
                            C.append(x, y);
                            IMGData[x][y] = C.V;
                            IMGKit.paintFull(IMG_DO, x, y);
                            for (int i = 0; i < 4; i++) {
                                int xi = x + IMGKit.dxs_4[i];
                                int yi = y + IMGKit.dys_4[i];
                                if (IMGData[xi][yi] > 0)
                                    C.removeBound(xi, yi);      // 填充后的内部边界失效
                            }
                        }
        System.out.print("E. ");
    }
    /**
     * 判断某点是否被封闭于集群C内部
     */
    private boolean beClosed(Cluster C, int x, int y) {
        for (int i1 = C.lb; i1 <= x; i1++)
            if (IMGData[i1][y] > 0)
                for (int i2 = x; i2 <= C.rb; i2++)
                    if (IMGData[i2][y] > 0)
                        for (int j1 = C.ub; j1 <= y; j1++)
                            if (IMGData[x][j1] > 0)
                                for (int j2 = x; j2 <= C.db; j2++)
                                    if (IMGData[j2][y] > 0)
                                        return true;
        return false;
    }

    /**
     * 集群拟合圆，计算并写入数据
     */
    private void Fitting() {
        System.out.print("[Ft] S-");
        try {
            for (Cluster C : clusters) {
                C.Calculate2();
                notepad.write(I + " " + C.cX + " " + C.cY + " " + C.R + " \n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print("E. ");
        TestForFittingResult();
    }



    private void paintEmpty(int x, int y) {IMGKit.paintEmpty(IMG_DO, x, y);}
    private boolean outsideArea(int x, int y) {return IMGKit.outsideArea(IMG_DO, x, y);}
    private int[] getPixel(int x, int y) {return IMGKit.getPixel(IMG_DO, x, y);}
    
    private void storageIMG(BufferedImage BI) {IMGKit.IMG_clone(IMG_DO, BI);}


    /**
     * 集群化结果测试 <br/>
     * 给出一张带前缀c的图片，用不同颜色区分所辨别到的不同集群
     */
    private void TestForClusterResult() {
        int CP = 255 / clusters.size();
        BufferedImage CR = new BufferedImage(IMG_DO.getWidth(), IMG_DO.getHeight(), 5);
        for (int x = 0; x < CR.getWidth(); x++)
            for (int y = 0; y < CR.getHeight(); y++)
                if (IMGData[x][y] > 1) {
                    int T = IMGData[x][y] - 2;
                    IMGKit.paint(CR, 255, 255-(CP*T), CP*T, CP*T, x, y);
                }
        IMGKit.saveImage(CR, workPath + "done/c" + I + imageFormat);
    }
    /**
     * 拟合结果测试 <br/>
     * 给出一张带前缀f的图片，展示拟合圆
     */
    private void TestForFittingResult() {
//        BufferedImage FR = new BufferedImage(IMG_DO.getWidth(), IMG_DO.getHeight(), 5);
//        IMGKit.fillEmpty(FR);
//        for (Cluster C : clusters)
//            IMGKit.drawCircle(FR, C.color, C.cX, C.cY, C.R);
//        IMGKit.saveImage(FR, workPath + "done/f" + I + imageFormat);

        BufferedImage Ft = IMGKit.CloneNewIMG((Objects.requireNonNull(IMGKit.inputIMG(workPath + "todo/" + I + imageFormat))), 5);
        for (Cluster C : clusters)
            IMGKit.drawCircle(Ft, C.color, C.cX, C.cY, C.R);
        IMGKit.saveImage(Ft, workPath + "done/t" + I + imageFormat);
    }


    private void TestForSearchC() {
        BufferedImage FS = IMGKit.CloneNewIMG((Objects.requireNonNull(IMGKit.inputIMG(workPath + "todo/" + I + imageFormat))), 5);
        for (Colony colony : colonies) {
            IMGKit.drawCircle(FS, new int[]{255, 255, 0, 0}, colony.X, colony.Y, colony.R);
        }
        IMGKit.saveImage(FS, workPath + "done/z" + I + imageFormat);
    }


    /**
     * 导入图片
     */
    private boolean inputImage(int o) {
        File imageFile = new File(workPath + "todo/" + o + imageFormat);
        IMG_DO = IMGKit.inputIMG(imageFile.getAbsolutePath());
        if (IMG_DO == null) {
            System.out.println("x");
            return false;
        }
        IMG_Store = IMGKit.CloneNewIMG(IMG_DO);
        System.out.print("<In> ");
        return true;
    }
    /**
     * 导出图片
     */
    private void outputImage(int o) {
//        IMGKit.saveImage(IMG_DO, workPath + "done/" + o + imageFormat);
        System.out.println(" <Out>");
    }


}
