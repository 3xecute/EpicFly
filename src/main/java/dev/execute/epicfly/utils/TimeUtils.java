package dev.execute.epicfly.utils;

public class TimeUtils {

    public static long parseTime(String input) {
        // Supports: 30s, 5m, 2h, 1d
        try {
            if (input.endsWith("d")) return Long.parseLong(input.replace("d", "")) * 86400;
            if (input.endsWith("h")) return Long.parseLong(input.replace("h", "")) * 3600;
            if (input.endsWith("m")) return Long.parseLong(input.replace("m", "")) * 60;
            if (input.endsWith("s")) return Long.parseLong(input.replace("s", ""));
            return Long.parseLong(input) * 60; // default: minutes
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String formatSeconds(long seconds) {
        if (seconds >= 86400) {
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            return days + "g " + hours + "s";
        } else if (seconds >= 3600) {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            return hours + "s " + mins + "d";
        } else if (seconds >= 60) {
            long mins = seconds / 60;
            long secs = seconds % 60;
            return mins + "d " + secs + "s";
        } else {
            return seconds + "s";
        }
    }
}
