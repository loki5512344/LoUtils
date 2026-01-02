package xyz.lokili.loutils.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {
    
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");
    
    /**
     * Парсит время из строки типа "13m", "3d", "2h", "30s"
     * @param timeStr строка времени
     * @return время в минутах, или -1 если не удалось распарсить
     */
    public static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return -1;
        }
        
        // Если это просто число - считаем минутами
        try {
            return Integer.parseInt(timeStr);
        } catch (NumberFormatException ignored) {}
        
        // Парсим с суффиксами
        Matcher matcher = TIME_PATTERN.matcher(timeStr.toLowerCase());
        int totalMinutes = 0;
        
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "s" -> totalMinutes += value / 60; // секунды в минуты
                case "m" -> totalMinutes += value;      // минуты
                case "h" -> totalMinutes += value * 60; // часы в минуты
                case "d" -> totalMinutes += value * 60 * 24; // дни в минуты
            }
        }
        
        return totalMinutes > 0 ? totalMinutes : -1;
    }
    
    /**
     * Форматирует минуты в читаемый вид
     */
    public static String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + "м";
        } else if (minutes < 1440) { // меньше дня
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            return hours + "ч" + (remainingMinutes > 0 ? " " + remainingMinutes + "м" : "");
        } else {
            int days = minutes / 1440;
            int remainingHours = (minutes % 1440) / 60;
            return days + "д" + (remainingHours > 0 ? " " + remainingHours + "ч" : "");
        }
    }
}