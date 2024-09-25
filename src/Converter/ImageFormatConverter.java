package Converter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 图像格式转换器
 */
public class ImageFormatConverter {
    private static final String OriPath = "D:/temp-Java/CellTrace/row/";
    private static final String NewPath = "D:/temp-Java/CellTrace/ripe/";

    public static void main(String[] args) {
        System.out.println("Running.");
        ImageFormatConverter JP = new ImageFormatConverter("tif", "png");
        File[] images = Objects.requireNonNull(new File(OriPath).listFiles());
        int N = images.length;
        for (int i = 0; i < N; i++) {
            System.out.print((i+1) + " / ");
            JP.converter(i, images[i]);
            System.out.println(N);
        }
        System.out.println("Completed.");
    }

    private final String AFormat;
    private final String BFormat;

    public ImageFormatConverter(String OriFormat, String NewFormat) {
        AFormat = OriFormat;
        BFormat = NewFormat;
    }

    private void converter(int i, File image){
        if (!image.getName().endsWith(AFormat)) return;
        try {
            BufferedImage AImage = ImageIO.read(image);
            ImageIO.write(AImage, BFormat, new File(NewPath + (i+1) + "." + BFormat));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}