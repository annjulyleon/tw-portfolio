package main.java;

/**
 * Created by a.leonova on 29.06.2017.
 */
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

public class ExtractFileStream {
    public static void main(String[] args) throws Exception{
        long startTime = System.currentTimeMillis();
        String fileToBeExtracted=args[1];
        String zipPackage=args[0];
        try {
        OutputStream out = new FileOutputStream(fileToBeExtracted);
            FileInputStream fileInputStream = new FileInputStream(zipPackage);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            ZipInputStream zin = new ZipInputStream(bufferedInputStream);
            ZipEntry ze = null;

            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().equals(fileToBeExtracted)) {
                    byte[] buffer = new byte[9000];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    break;
                }
            }
            zin.close();
        }  catch (Exception e) {
            System.out.println("Ошибка: " + e);
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = (endTime - startTime);
        System.out.println("Время выполнения программы: " + totalTime);
    }
}
