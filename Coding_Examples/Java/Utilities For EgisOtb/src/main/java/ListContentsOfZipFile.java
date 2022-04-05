package main.java;

/**
 * Created by a.leonova on 26.06.2017.
 */

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ListContentsOfZipFile {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String filePath = args[0];
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(filePath);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                String entryName = entry.getName();
                System.out.println("Файл: " + entryName);
            }
        } catch (IOException ioe) {
            System.out.println("Ошибка открытия архива" + ioe);
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
            } catch (IOException ioe) {
                System.out.println("Ошибка закрытия архива" + ioe);
            }
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = (endTime - startTime);
        System.out.println("Время выполнения программы: " + totalTime);
    }
}