package Analyzer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 运动学分析
 */
public class KinematicsAnalyzer {
    private final String workPath = "D:/temp-Java/CellTrace/";
    private final File[] traces = Objects.requireNonNull(new File(workPath + "cell_data/").listFiles());

    private final ArrayList<Double> speeds = new ArrayList<>();
    private final ArrayList<Double> angularSpeeds = new ArrayList<>();

    private static final double SC = 12.46455;  // 面积高度比
    private static final double um_p = 0.1625;    // 像素点长
    private static final double t_f = (double) 1/20;    // 帧时长

    public KinematicsAnalyzer() {}

    public void launch() {
        try {
            BufferedWriter heightBook = new BufferedWriter(new FileWriter(workPath + "height.txt"));
            BufferedWriter speedBook = new BufferedWriter(new FileWriter(workPath + "speed.txt"));
            BufferedWriter speedPBook = new BufferedWriter(new FileWriter(workPath + "speedP.txt"));
            BufferedWriter angularSpeedBook = new BufferedWriter(new FileWriter(workPath + "angular_speed.txt"));
            BufferedWriter angularSpeedPBook = new BufferedWriter(new FileWriter(workPath + "angular_speedP.txt"));
            for (int c = 0; c < traces.length; c++) {
                heightBook.write((c+1) + "\n");
                List<String> lines = Files.readAllLines(Path.of(traces[c].getAbsolutePath()));
                double ppX = -1;
                double ppY = -1;
                double pX = -1;
                double pY = -1;
                for (String line : lines) {
                    String[] values = line.split(" ");
                    double X = um_p * Integer.parseInt(values[0]);
                    double Y = um_p * Integer.parseInt(values[1]);
                    double R = um_p * Integer.parseInt(values[2]);
                    if (pX >= 0 && pY >= 0) {
                        double Z = Math.pow(R, 2) / SC;
                        heightBook.write(S(X) + " " + S(Y) + " " + S(Z) + "\n");

                        double V = distance(X, Y, pX, pY) / t_f;
                        speeds.add(V);

                        if (ppX >= 0 && ppY >= 0) {
                            double ax = pX - ppX;
                            double ay = pY - ppY;
                            double bx = X - pX;
                            double by = Y - pY;

                            double A = Math.acos((ax*bx + ay*by)/(distance(pX, pY, ppX, ppY) * distance(X, Y, pX, pY))) * 180 / Math.PI;
                            double W = A / t_f;
                            angularSpeeds.add(W);
                        }
                    }
                    ppX = pX;
                    ppY = pY;
                    pX = X;
                    pY = Y;
                }
            }
            Collections.sort(speeds);
            Collections.sort(angularSpeeds);

            int c = 0;
            int L = speeds.size();
            double pS = speeds.get(0);
            speeds.add((double) -1);
            for (Double S : speeds) {
                if (S(S).equals(S(pS)))
                    c ++;
                else {
                    speedBook.write(S(pS) + " " + c + "\n");
                    speedPBook.write(S(pS) + " " + ((double) c/L) + "\n");
                    c = 1;
                }
                pS = S;
            }

            c = 0;
            L = angularSpeeds.size();
            double pW = angularSpeeds.get(0);
            angularSpeeds.add((double) -1);
            for (Double W : angularSpeeds) {
                if (S(W).equals(S(pW)))
                    c ++;
                else {
                    angularSpeedBook.write(S(pW) + " " + c + "\n");
                    angularSpeedPBook.write(S(pW) + " " + ((double) c/L) + "\n");
                    c = 1;
                }
                pW = W;
            }

            heightBook.close();
            speedBook.close();
            speedPBook.close();
            angularSpeedBook.close();
            angularSpeedPBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private static String S(double v) {
        return String.format("%.2f", v);
    }


    public static void main(String[] args) {
        KinematicsAnalyzer KA = new KinematicsAnalyzer();
        KA.launch();
    }
}
