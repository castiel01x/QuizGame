package me.castiel.quizgame.util.strings;

import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class StringUtils {

    private StringUtils() { }

    public static String color(String message) {
        String m = String.valueOf(message);
        return m.replace('&', ChatColor.COLOR_CHAR);
    }

    private static final String formatWeeks = "%weeks%W";
    private static final String formatDays = "%days%D";
    private static final String formatHours = "%hours%H";
    private static final String formatMinutes = "%minutes%M";
    private static final String formatSeconds = "%seconds%S";
    private static final String formatMillis = "%millis%MS";
    private static final String formatSeperator = " ";

    public static String format(long milliseconds) {
        long weeks = milliseconds / 604800000;
        milliseconds = milliseconds % 604800000;
        long days = milliseconds / 86400000;
        milliseconds = milliseconds % 86400000;
        long hours = milliseconds / 3600000;
        milliseconds = milliseconds % 3600000;
        long minutes = milliseconds / 60000;
        milliseconds = milliseconds % 60000;
        long seconds = milliseconds / 1000;
        milliseconds = milliseconds % 1000;

        StringBuilder builder = new StringBuilder();

        if (weeks > 0) {
            builder.append(formatWeeks.replace("%weeks%", String.valueOf(weeks)));
        }

        if (days > 0) {
            if (builder.length() > 0) {
                builder.append(formatSeperator);
            }
            builder.append(formatDays.replace("%days%", String.valueOf(days)));
        }

        if (hours > 0) {
            if (builder.length() > 0) {
                builder.append(formatSeperator);
            }
            builder.append(formatHours.replace("%hours%", String.valueOf(hours)));
        }

        if (minutes > 0) {
            if (builder.length() > 0) {
                builder.append(formatSeperator);
            }
            builder.append(formatMinutes.replace("%minutes%", String.valueOf(minutes)));
        }

        if (seconds > 0) {
            if (builder.length() > 0) {
                builder.append(formatSeperator);
            }
            builder.append(formatSeconds.replace("%seconds%", String.valueOf(seconds)));
        }

        if (milliseconds > 0 || builder.length() == 0) {
            if (builder.length() > 0) {
                builder.append(formatSeperator);
            }
            builder.append(formatMillis.replace("%millis%", String.valueOf(milliseconds)));
        }

        return builder.toString();
    }
}
