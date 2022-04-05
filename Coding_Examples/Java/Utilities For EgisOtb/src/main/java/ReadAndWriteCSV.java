package main.java;

/**
 * Created by a.leonova on 30.06.2017.
 */
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Arrays;

public class ReadAndWriteCSV {
    /* Count lines in file, exclude empty*/
    public static int countLines(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        try {
            int lines = 0;
            String line;
            while ((line = reader.readLine()) != null){
                if(!"".equals(line.trim())){
                    lines++;
                }
            }
            return lines;
        } finally {
            reader.close();
        }
    }
    /* Get modifier to add to Calendar. TODO: merge getDateModifier and getTime methods*/
    public static int getDateModifier(String line) {
        String[] splitDateTime = line.split("T");
        int dateModifier = Integer.parseInt(splitDateTime[0]);
        return dateModifier;
    }
    public static String getTime(String line) {
        String[] splitDateTime = line.split("T");
        String time = splitDateTime[1];
        return time;
    }
    /* method to calculate date (add or extract days)*/
    public static String dateCalculation(int modifier) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, modifier);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String calcDate = dateFormat.format(cal.getTime());
        return calcDate;
    }
    /* method to convert string from  properties to array */
    public static int[] propsToArray(String line) {
            int[] arr = Arrays.stream(line.split(","))
                    .map(String::trim).mapToInt(Integer::parseInt).toArray();
            return arr;
    }
    public static void main(String[] args) throws Exception{
        String configFile = args[0];
        String fileInput = args[1];
        String fileOutput = args[2];
        int segment = Integer.parseInt(args[3]);
        int useDateModifier = Integer.parseInt(args[4]);
        System.out.println("Конфигурационный файл: " + configFile + "\n" +
                "Путь к файлу: " + fileInput + "\n" +
                "Обработанный файл: " + fileOutput + "\n" +
                "Сегмент: " + segment + "\n" +
                "Использовать модификатор: " + useDateModifier);

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(configFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            int linesCount = countLines(fileInput);
            System.out.println("Количество строк в файле: " + linesCount);
            CSVReader reader = new CSVReader((new InputStreamReader(new FileInputStream(fileInput), "UTF-8")), ';');
            List<String[]> csvBody = reader.readAll();
            int autoDateColumns[] = propsToArray(prop.getProperty("autoDateColumns"));
            int railDateColumns[] = propsToArray(prop.getProperty("railDateColumns"));
            int shipDateColumns[] = propsToArray(prop.getProperty("shipDateColumns"));
            int autoDefaultModifiers[] = propsToArray(prop.getProperty("autoDefaultModifiers"));
            int railDefaultModifiers[] = propsToArray(prop.getProperty("railDefaultModifiers"));
            int shipDefaultModifiers[] = propsToArray(prop.getProperty("shipDefaultModifiers"));

            for (int i = 1, size = linesCount; i < size; i++) {
                if (segment == 2 && useDateModifier == 1) {
                    for (int j = 0, columns = autoDateColumns.length; j < columns; j++) {
                        csvBody.get(i)[autoDateColumns[j]] = dateCalculation
                                (getDateModifier(csvBody.get(i)[autoDateColumns[j]]))
                                + "T" + getTime(csvBody.get(i)[autoDateColumns[j]]);
                    }
                } else if (segment == 3 && useDateModifier == 1) {
                    for (int j = 0, columns = railDateColumns.length; j < columns; j++) {
                        csvBody.get(i)[railDateColumns[j]] = dateCalculation
                                (getDateModifier(csvBody.get(i)[railDateColumns[j]]))
                                + "T" + getTime(csvBody.get(i)[railDateColumns[j]]);
                    }
                } else if (segment == 4 && useDateModifier == 1) {
                    for (int j = 0, columns = shipDateColumns.length; j < columns; j++) {
                        csvBody.get(i)[shipDateColumns[j]] = dateCalculation
                                (getDateModifier(csvBody.get(i)[shipDateColumns[j]]))
                                + "T" + getTime(csvBody.get(i)[shipDateColumns[j]]);
                    }
                } else if (segment == 2 && useDateModifier == 0){
                    for (int j = 0, columns = autoDateColumns.length; j < columns; j++) {
                        csvBody.get(i)[autoDateColumns[j]] = dateCalculation(autoDefaultModifiers[j])
                                + "T" + getTime(csvBody.get(i)[autoDateColumns[j]]);
                    }
                } else if (segment == 3 && useDateModifier == 0){
                    for (int j = 0, columns = railDateColumns.length; j < columns; j++) {
                        csvBody.get(i)[railDateColumns[j]] = dateCalculation
                                (railDefaultModifiers[j])
                                + "T" + getTime(csvBody.get(i)[railDateColumns[j]]);
                    }
                } else if (segment == 4 && useDateModifier == 0) {
                    for (int j = 0, columns = shipDateColumns.length; j < columns; j++) {
                        csvBody.get(i)[shipDateColumns[j]] = dateCalculation
                                (shipDefaultModifiers[j])
                                + "T" + getTime(csvBody.get(i)[shipDateColumns[j]]);
                    }
                } else {
                    System.out.println("Укажите сегмент (2 для авто, 3 для ЖД, 4 для Моря).\n" +
                            "Укажите, требуется ли использовать модификатор (1 - да, в файле есть модификатор,\n" +
                            "0 - нет, использовать даты по умолчанию (даты прибытия и отправления на завтра,\n" +
                            "дата покупки вчера, дата регистрации сегодня) ");
                    System.exit(1);

                }
            }
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(fileOutput), "UTF-8"),';', '\0');
            writer.writeAll(csvBody);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        }
    }
