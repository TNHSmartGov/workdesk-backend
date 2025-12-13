package com.tnh.baseware.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class DateTimeUtils {

    // Múi giờ Việt Nam (UTC+7)
    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static Instant toInstant(String dateTimeString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        return Instant.from(formatter.parse(dateTimeString));
    }

    // Chuyển đổi string thành Instant với múi giờ +7
    public static Instant toInstantWithVietnamZone(String dateTimeString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(VIETNAM_ZONE);
        if (pattern.contains("H") || pattern.contains("h")) {
            return ZonedDateTime.parse(dateTimeString, formatter).toInstant();
        } else {
            LocalDate localDate = LocalDate.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
            return localDate.atStartOfDay(VIETNAM_ZONE).toInstant();
        }
    }

    // Chuyển đổi string date thành Instant với giờ 00:00:00
    public static Instant dateStringToInstant(String dateString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    // Lấy thời gian hiện tại
    public static Instant now() {
        return Instant.now();
    }

    public static String format(Instant instant, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    // Format Instant với múi giờ Việt Nam
    public static String formatWithVietnamZone(Instant instant, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(VIETNAM_ZONE);
        return formatter.format(instant);
    }

    public static LocalDate toLocalDate(String dateString, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateString, formatter);
    }

    public static void main(String[] args) {
        // Ví dụ chuyển đổi string date thành Instant
        String dateStr = "2025-12-21";
        Instant dateTime1 = dateStringToInstant(dateStr, "yyyy-MM-dd");
        System.out.println("Date string to Instant: " + dateTime1);

        // Ví dụ với string datetime
        String dateTimeStr = "2025-12-21 14:30:00";
        Instant dateTime2 = toInstant(dateTimeStr, "yyyy-MM-dd HH:mm:ss");
        System.out.println("DateTime string to Instant: " + dateTime2);

        // Thời gian hiện tại
        Instant now = now();
        System.out.println("Instant now: " + now);

        // Format với múi giờ Việt Nam
        String formattedTime = formatWithVietnamZone(now, "yyyy-MM-dd HH:mm:ss");
        System.out.println("Formatted Vietnam time: " + formattedTime);
    }
}
