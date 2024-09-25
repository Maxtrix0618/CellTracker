import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TracePainter {
    private final String workPath = "D:/temp-Java/CellTrace/";
    private final File[] traces = Objects.requireNonNull(new File(workPath + "cell_data/").listFiles());
    private final ArrayList<int[]> colorGroup = ColorGroup.group(traces.length, false);
    public TracePainter() {}

    public void launch() {
        BufferedImage IMG = new BufferedImage(2048, 2048, 5);
        try {
            for (int c = 0; c < traces.length; c++) {
                List<String> lines = Files.readAllLines(Path.of(traces[c].getAbsolutePath()));
                int pX = -1;
                int pY = -1;
                for (String line : lines) {
                    String[] values = line.split(" ");
                    int X = Integer.parseInt(values[0]);
                    int Y = Integer.parseInt(values[1]);
                    if (pX >= 0 && pY >= 0) {
                        IMGKit.drawLine(IMG, colorGroup.get(c), X, Y, pX, pY, 1);
                    }
                    pX = X;
                    pY = Y;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        IMGKit.saveImage(IMG, workPath + "Map.png");
    }

    public void paintPartly(int N) {
        for (int i = 1; i <= N; i++)
            paintPart(i, N);
    }

    public void paintPart(int i, int N) {
        System.out.print(i + "/");
        BufferedImage IMG =IMGKit.CloneNewIMG(Objects.requireNonNull(IMGKit.inputIMG(workPath + "todo/" + i + ".png")), 5);
        try {
            for (int c = 0; c < traces.length; c++) {
                List<String> lines = Files.readAllLines(Path.of(traces[c].getAbsolutePath()));
                int pX = -1;
                int pY = -1;
                for (int j = 0; j < Math.min(i-1, lines.size()); j++) {
                    String[] values = lines.get(j).split(" ");
                    int X = Integer.parseInt(values[0]);
                    int Y = Integer.parseInt(values[1]);
                    if (pX >= 0 && pY >= 0) {
                        IMGKit.drawLine(IMG, colorGroup.get(c), X, Y, pX, pY, 1);
                    }
                    pX = X;
                    pY = Y;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        IMGKit.saveImage(IMG, workPath + "maps/" + i + ".png");
        System.out.println(N);
    }


    public static void main(String[] args) {
        TracePainter TP = new TracePainter();
        TP.launch();
        TP.paintPartly(51);
    }

}
