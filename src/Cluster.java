import java.util.ArrayList;

/**
 * 集群
 */
public class Cluster {
    private final ArrayList<Integer> Xs = new ArrayList<>();   // 成员X坐标列表
    private final ArrayList<Integer> Ys = new ArrayList<>();   // 成员Y坐标列表
    public int size = 0;    // 成员数
    public int V;           // 集群特征数

    public int cX = 0;      // 质心X坐标
    public int cY = 0;      // 质心Y坐标
    public int R = 0;       // 半径
    public int[] color;     // 特征颜色

    public Cluster() {}

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
     */
    public void append(int x, int y) {
        if (hasPoint(x, y)) return;
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


    private final ArrayList<Integer> Xbs = new ArrayList<>();  // 边界成员X坐标列表
    private final ArrayList<Integer> Ybs = new ArrayList<>();  // 边界成员Y坐标列表
    /**
     * 为边界添加一个成员点
     */
    public void appendBound(int x, int y) {
        for (int i = 0; i < Xbs.size(); i++)
            if (Xbs.get(i) == x && Ybs.get(i) == y)
                return;
        Xbs.add(x);
        Ybs.add(y);
    }
    /**
     * 从边界删去一个成员点
     */
    public void removeBound(int x, int y) {
        for (int i = 0; i < Xbs.size(); i++)
            if (Xbs.get(i) == x && Ybs.get(i) == y) {
                Xbs.remove(i);
                Ybs.remove(i);
                break;
            }
    }

    public int lb, rb, ub, db;  // 四顶界
    /**
     * 按边界计算集群的圆心，计算顶界
     */
    public void calculate1() {
        for (int i = 0; i < Xbs.size(); i++) {
            cX += Xbs.get(i);
            cY += Ybs.get(i);
        }
        cX = cX / Xbs.size();
        cY = cY / Ybs.size();

        lb = cX; rb = cX;
        ub = cY; db = cY;
        for (int i = 0; i < Xbs.size(); i++) {
            lb = Math.min(lb, Xbs.get(i));
            rb = Math.max(rb, Xbs.get(i));
            ub = Math.min(ub, Ybs.get(i));
            db = Math.max(db, Ybs.get(i));
        }
    }


    /**
     * 按成员计算圆心，计算集群的半径
     */
    public void Calculate2() {
        for (int i = 0; i < size; i++) {
            cX += Xs.get(i);
            cY += Ys.get(i);
        }
        cX /= size;
        cY /= size;

        R = (int) (Math.sqrt(size));
//        for (int i = 0; i < Xbs.size(); i++) {
//            R += IMGKit.distance(Xbs.get(i), Ybs.get(i), cX, cY);
//        }
//        R = R / Xbs.size();
    }

    /**
     * 按成员计算圆心，按成员计算半径
     */
    public void Calculate3() {
        for (int i = 0; i < size; i++) {
            cX += Xs.get(i);
            cY += Ys.get(i);
        }
        cX = cX / Xs.size();
        cY = cY / Ys.size();

        for (int i = 0; i < size; i++) {
            R += Math.pow(IMGKit.distance(Xs.get(i), Ys.get(i), cX, cY), 2);
        }
        R = (int) (Math.sqrt((double) R / size));
    }


    /**
     * 返回圆信息
     */
    public int[] circle() {
        return new int[]{cX, cY, R};
    }


}