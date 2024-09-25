import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 细胞寻踪 <br/>
 * CircleTracker / 2023.12.12 by_Maxtrix
 */
public class CircleTracker {
    private final String workPath = "D:/temp-Java/CellTrace/";

    private ArrayList<Cell> cells;  // 细胞队伍
    private List<String> frames;    // 定位数据scale

    private final int cn;   // 参数：细胞数目（目标集群数目）

    public CircleTracker() {
        cn = 22;
    }


    public void launch() {
        initial();
        logging();
        report();
        terminal();
    }


    /**
     * 开场
     */
    private void initial() {
        System.out.println("Tracking Start.");
        cells = new ArrayList<>();
        for (int i = 0; i < cn; i++)
            cells.add(new Cell());
        try {
            frames = Files.readAllLines(Path.of(workPath + "film.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 谢幕
     */
    private void terminal() {
        System.out.println("ALL Missions Completed.");
    }


    /**
     * 细胞录入frame日志
     */
    private void logging() {
        System.out.print("[Lg] S-");
        int L = 1;          // 当前行数
        while (L < cn+1) {
            System.out.print(".");
            cells.get(L-1).log(line(L));
            L ++;
        }
        while (true) {
            System.out.print(".");
            int[] line = line(L);
            if (line == null) break;
            nearest(line).log(line);
            L ++;
        }
        System.out.print("E.");
    }


    /**
     * 返回上一刻的细胞队伍里距该行数据坐标最近的细胞
     */
    private Cell nearest(int[] line) {
        int t = Integer.MAX_VALUE;
        Cell ct = cells.get(0);
        for (int i = 0; i < cn; i++) {
            Cell c = cells.get(i);
            int[] cl = c.late();
            int v = IMGKit.distance(line[1], line[2], cl[0], cl[1]);
            if (v == 0) continue;
            if (v < t) {
                t = v;
                ct = c;
            }
        }
        return ct;
    }


    /**
     * 细胞汇报轨迹
     */
    private void report() {
        System.out.print("[Rp] S-");
        // 轨迹列表
        try {
            BufferedWriter notepad = new BufferedWriter(new FileWriter(workPath + "trace.txt"));
            notepad.write("C  F  X  Y  R" + "\n");

            for (int c = 0; c < cells.size(); c++) {
                Cell C = cells.get(c);
                for (int f = 0; f < C.day; f++) {
                    int[] d = C.diary(f+1);
                    notepad.write((c+1) + " " + (f+1) + " " + d[0] + " " + d[1] + " " + d[2] + "\n");
                }
            }
            notepad.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 航程表格
        try {
            BufferedWriter notepad = new BufferedWriter(new FileWriter(workPath + "table.txt"));
            notepad.write("F H X X X X Y Y Y Y R R R R\n" +
                              "f um p p p p p p p p p p p p\n" +
                              "- - 1 2 3 4 1 2 3 4 1 2 3 4\n");

            for (int f = 0; f < cells.get(0).day; f++) {
                notepad.write((f+1)+ " ");
                notepad.write(String.format("%.2f ", ((f+1)*0.2)));
                for (int k = 0; k < 3; k++)
                    for (Cell C : cells)
                        notepad.write(C.diary(f+1)[k] + " ");
                notepad.write("\n");
            }
            notepad.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print("E.");
    }



    /**
     * 返回frames第i(from 1)行的4个数据
     */
    public int[] line(int i) {
        if (i >= frames.size()) return null;
        int[] data = new int[4];
        String[] values = frames.get(i).split(" ");
        for (int v = 0; v < 4; v++)
            data[v] = Integer.parseInt(values[v]);
        return data;
    }



    public static void main(String[] args) {
        CircleTracker CT = new CircleTracker();
        CT.launch();
    }

}
