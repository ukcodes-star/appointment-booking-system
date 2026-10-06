package com.booking.appointment.util;

public class TimeUtil {

    public static int toMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    public static String toTimeString(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public static boolean hasOverlap(int startA, int endA, int startB, int endB) {
        return Math.max(startA, startB) < Math.min(endA, endB);
    }
}