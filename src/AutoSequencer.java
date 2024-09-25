import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动序列器
 * （程序仍有一些瑕疵：会生成非常多额外的空文档文件，原因是细胞时间与程序时间对log的perspective不一致）
 */
public class AutoSequencer {
    private final String workPath = "D:/temp-Java/CellTrace/";
    private ArrayList<Cell> cells;
    private List<String> frames;
    private int F;
    public AutoSequencer() {}

    public void launch() {
        initial();
        tracking();
        telling();
    }

    private void initial() {
        cells = new ArrayList<>();
        try {
            frames = Files.readAllLines(Path.of(workPath + "film2.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tracking() {
        int pF = 0;
        for (int i = 0; i < frames.size(); i++) {
            int[] V = line(i);
            F = V[0];
            if (F != pF)
                for (Cell c : cells) c.called = false;
            Cell nearest = nearest_uncalled(V[1], V[2]);
            if (nearest != null)
                nearest.log(V[1], V[2], V[3]);
            else {
                Cell newC = new Cell();
                newC.log(V[1], V[2], V[3]);
                newC.called = true;
                cells.add(newC);
                System.out.print(".");
            }
            pF = F;
        }
        System.out.println();
    }

    private void telling() {
        String dataPath = workPath + "cell_data/";
        try {
            for (int i = 0; i < cells.size(); i++) {
                BufferedWriter np = new BufferedWriter(new FileWriter(dataPath + (i+1) + ".txt"));
                Cell c = cells.get(i);
                for (int f = 1; f < c.day; f++) {
                    int[] D = c.diary(f);
                    np.write(D[0] + " " + D[1] + " " + D[2] + "\n");
                }
                np.close();
                System.out.print(".");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
    }


    private static final int maxDistance = 40;
    private Cell nearest_uncalled(int x, int y) {
        ArrayList<Cell> Cs = new ArrayList<>();
        for (Cell c : cells)
            if (!c.called && c.reached(F-1) && IMGKit.distance(x, y, c.diary(F-1)[0], c.diary(F-1)[1]) <= maxDistance)
                Cs.add(c);
        if (Cs.size() == 0) return null;
        Cell nst = Cs.get(0);
        for (Cell c : Cs)
            if (IMGKit.distance(x, y, c.diary(F-1)[0], c.diary(F-1)[1]) < IMGKit.distance(x, y, nst.diary(F-1)[0], nst.diary(F-1)[1]))
                nst = c;
        nst.called = true;
        return nst;
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
        AutoSequencer ASq = new AutoSequencer();
        ASq.launch();
    }


}
