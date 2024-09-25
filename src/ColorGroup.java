import java.util.ArrayList;
import java.util.Collections;

public class ColorGroup {
    private ColorGroup() {}

    public static ArrayList<int[]> group(int N, boolean ordered) {
        ArrayList<int[]> colors = new ArrayList<>();
        for (int i = 0; i < N; i ++) {
            colors.add(standard(i, N));
        }
        if (!ordered) Collections.shuffle(colors);
        return colors;
    }

    private static int[] standard(int I, int N) {
        int D = 255 / N;
        int R, G, B;

        R = 255 - I*D;
        G = (I*D < 128) ? (I*D*2) : (510 - I*D*2);
        B = I*D;

        return new int[]{255, R, G, B};
    }


    private static int[] rainbow(int I, int N) {
        int D = 400 / N;
        int wavelength = 380 + D*I;
        double gamma = 0.8;
        double IMax = 255.0;
        double r, g, b, alpha;

        if (wavelength < 380.0) {
            r = 0.0;
            g = 0.0;
            b = 0.0;
        } else if (wavelength < 440.0) {
            r = -1.0 * (wavelength - 440.0) / (440.0 - 380.0);
            g = 0.0;
            b = 1.0;
        } else if (wavelength < 490.0) {
            r = 0.0;
            g = (wavelength - 440.0) / (490.0 - 440.0);
            b = 1.0;
        } else if (wavelength < 510.0) {
            r = 0.0;
            g = 1.0;
            b = -1.0 * (wavelength - 510.0) / (510.0 - 490.0);
        } else if (wavelength < 580.0) {
            r = (wavelength - 510.0) / (580.0 - 510.0);
            g = 1.0;
            b = 0.0;
        } else if (wavelength < 645.0) {
            r = 1.0;
            g = -1.0 * (wavelength - 645.0) / (645.0 - 580.0);
            b = 0.0;
        } else if (wavelength <= 780.0) {
            r = 1.0;
            g = 0.0;
            b = 0.0;
        } else {
            r = 0.0;
            g = 0.0;
            b = 0.0;
        }

        if (wavelength < 380.0) {alpha = 0.0;}
        else if (wavelength < 420.0) {alpha = 0.30 + 0.70 * (wavelength - 380.0) / (420.0 - 380.0);}
        else if (wavelength < 701.0) {alpha = 1.0;}
        else if (wavelength <= 780.0) {alpha = 0.30 + 0.70 * (780.0 - wavelength) / (780.0 - 700.0);}
        else {alpha = 0.0;}

        int R = r == 0.0 ? 0 : (int) (IMax * Math.pow(r * alpha, gamma));
        int G = g == 0.0 ? 0 : (int) (IMax * Math.pow(g * alpha, gamma));
        int B = b == 0.0 ? 0 : (int) (IMax * Math.pow(b * alpha, gamma));
        int A = (int) (alpha * 255);

        return new int[]{A, R, G, B};
    }


}
