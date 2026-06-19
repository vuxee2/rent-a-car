package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate parse(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s, FORMAT);
    }

    public static String format(LocalDate date) {
        if (date == null) return "";
        return date.format(FORMAT);
    }
}
