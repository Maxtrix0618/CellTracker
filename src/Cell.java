import java.util.ArrayList;

/**
 * 细胞（即集群的的拟合，记录在每一帧下的坐标和半径）
 */
public class Cell {
    public boolean called = false;
    private final ArrayList<Integer> X = new ArrayList<>();
    private final ArrayList<Integer> Y = new ArrayList<>();
    private final ArrayList<Integer> R = new ArrayList<>();
    public int day = 0;    // 行程长

    public Cell() {}

    /**
     * 记录行程：x, y, r
     */
    public void log(int x, int y, int r) {
        X.add(x);
        Y.add(y);
        R.add(r);
        day ++;
    }
    public void log(int[] diary) {
        log(diary[1], diary[2], diary[3]);
    }

    /**
     * 是否存在行程
     */
    public boolean reached(int date) {
        return (date > 0 && date <= day);
    }

    /**
     * 返回行程
     */
    public int[] diary(int date) {
        return new int[]{X.get(date-1), Y.get(date-1), R.get(date-1)};
    }

    /**
     * 返回最近的行程
     */
    public int[] late() {
        return diary(day);
    }

}
