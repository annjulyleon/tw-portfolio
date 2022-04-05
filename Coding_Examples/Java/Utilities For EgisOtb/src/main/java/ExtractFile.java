package main.java;

/**
 * Created by a.leonova on 29.06.2017.
 */
import java.io.IOException;
import java.nio.file.*;

public class ExtractFile {
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        /*Path zipFile = Paths.get("D:/files/test_message.zip");
        String fileName = "/data/route.csv";
        Path outputFile = Paths.get("D:/files/" + fileName + "_ext");*/
        Path zipFile= Paths.get(args[0]);
        String fileName = args[1];
        Path outputFile = Paths.get(args[2]);
        try (FileSystem fileSystem = FileSystems.newFileSystem(zipFile, null)) {
            Path fileToExtract = fileSystem.getPath(fileName);
            Files.copy(fileToExtract, outputFile);
            fileSystem.close(); //? not sure if needed
        } catch (IOException ioe) {
            System.out.println("Ошибка открытия архива: " + ioe);
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = (endTime - startTime);
        System.out.println("Время выполнения программы: " + totalTime);
    }
}
