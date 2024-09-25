import java.util.ArrayList;

/**
 * 聚落，圆
 */
public class Colony {
    public int X;   // 中心X坐标
    public int Y;   // 中心Y坐标
    public int R;   // 半径
    public int T;   // 淹没算法的剩余成员数

    public int size = 0;    // 成员数

    public Colony(int x, int y, int r, int t) {
        X = x;
        Y = y;
        R = r;
        T = t;
    }
    public Colony(int[] p) {
        X = p[0];
        Y = p[1];
        R = p[2];
        T = p[3];
    }

    private final ArrayList<Integer> Xs = new ArrayList<>();   // 成员X坐标列表
    private final ArrayList<Integer> Ys = new ArrayList<>();   // 成员Y坐标列表
    /**
     * 查询自己是否含有一个成员点
     */
    public boolean hasPoint(int x, int y) {
        for (int i = 0; i < size; i++)
            if (Xs.get(i) == x && Ys.get(i) == y)
                return true;
        return false;
    }

    /**
     * 添加一个成员点
     * @param check 是否要检查是否已有该成员
     */
    public void append(int x, int y, boolean check) {
        if (check && hasPoint(x, y)) return;
        Xs.add(x);
        Ys.add(y);
        size ++;
    }
    /**
     * 获取第order(from 0)个成员的坐标[x,y]
     */
    public int[] get(int order) {
        return new int[]{Xs.get(order), Ys.get(order)};
    }

}
