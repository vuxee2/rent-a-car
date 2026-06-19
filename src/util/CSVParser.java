package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    public static List<String[]> read(String filePath, String delimiter) {
        List<String[]> rows = new ArrayList<>();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            return rows;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(line.split(delimiter, -1));
            }
        } catch (IOException e) {
            System.err.println("Greska pri citanju fajla: " + filePath);
            e.printStackTrace();
        }
        return rows;
    }

    public static void writeAll(String filePath, List<String[]> rows, String delimiter) {
        ensureParentDirExists(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            for (String[] row : rows) {
                writer.write(String.join(delimiter, row));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Greska pri pisanju fajla: " + filePath);
            e.printStackTrace();
        }
    }

    public static void append(String filePath, String[] row, String delimiter) {
        ensureParentDirExists(filePath);
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(String.join(delimiter, row));
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Greska pri dopisivanju u fajl: " + filePath);
            e.printStackTrace();
        }
    }

    private static void ensureParentDirExists(String filePath) {
        try {
            Path parent = Paths.get(filePath).getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
