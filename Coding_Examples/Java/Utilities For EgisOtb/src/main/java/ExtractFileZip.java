package main.java;

/**
 * Created by a.leonova on 29.06.2017.
 */

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExtractFileZip {
    public static void main(String[] args) throws Exception{
        long startTime = System.currentTimeMillis();
        String zip = args[0];
        String fileName = args[1];
        String outputFile = args[2];
        try {
            ZipFile zipFile = new ZipFile(zip);
            ZipEntry zipEntry = zipFile.getEntry(fileName);
            InputStream is = zipFile.getInputStream(zipEntry);
            Files.copy(is, Paths.get(outputFile));
            is.close();
        } catch (Exception e) {
            System.out.println("Ошибка: " + e);
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = (endTime - startTime);
        System.out.println("Время выполнения программы: " + totalTime);
    }
}
