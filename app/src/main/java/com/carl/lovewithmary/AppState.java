package com.carl.lovewithmary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class AppState {
    private static final String PREFS = "love_prefs";
    private static final String KEY_SCHEMA = "schema_version";
    private static final String KEY_DATE = "start_date";
    private static final String KEY_PARTNER = "partner_name";

    // v1 key retained for migration.
    private static final String KEY_OLD_NOTIFICATION = "show_notification";

    private static final String KEY_THEME = "widget_theme";
    private static final String KEY_BG_COLOR = "widget_bg_color";
    private static final String KEY_TEXT_COLOR = "widget_text_color";
    private static final String KEY_BG_STRENGTH = "widget_bg_strength";

    private static final String KEY_MASTER_NOTIFICATIONS = "notifications_master";
    private static final String KEY_PERSISTENT_COUNTER = "notifications_counter";
    private static final String KEY_ANNIVERSARY = "notifications_anniversary";
    private static final String KEY_MONTHSARY = "notifications_monthsary";
    private static final String KEY_MILESTONE = "notifications_milestone";
    private static final String KEY_GIFT = "notifications_gift";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";

    private static final String KEY_ANNIV_30 = "anniv_30";
    private static final String KEY_ANNIV_14 = "anniv_14";
    private static final String KEY_ANNIV_7 = "anniv_7";
    private static final String KEY_ANNIV_3 = "anniv_3";
    private static final String KEY_ANNIV_1 = "anniv_1";
    private static final String KEY_ANNIV_0 = "anniv_0";
    private static final String KEY_ANNIV_FINAL_WEEK = "anniv_final_week";
    private static final String KEY_MONTHSARY_1 = "monthsary_1";
    private static final String KEY_MONTHSARY_0 = "monthsary_0";

    private static final String KEY_LAST_ANNIV = "last_anniv_event";
    private static final String KEY_LAST_MONTHSARY = "last_monthsary_event";
    private static final String KEY_LAST_MILESTONE = "last_milestone_event";
    private static final String KEY_LAST_GIFT = "last_gift_event";

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    private AppState() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void ensureMigration(Context context) {
        SharedPreferences p = prefs(context);
        if (p.getInt(KEY_SCHEMA, 1) >= 2) return;

        SharedPreferences.Editor e = p.edit();

        // Existing v1 installs were built specifically for Mary. Preserve that experience.
        if (!p.contains(KEY_PARTNER) && p.contains(KEY_DATE)) {
            e.putString(KEY_PARTNER, "Mary");
        }

        // Preserve the user's old persistent notification preference.
        if (p.contains(KEY_OLD_NOTIFICATION)) {
            boolean oldEnabled = p.getBoolean(KEY_OLD_NOTIFICATION, false);
            if (!p.contains(KEY_MASTER_NOTIFICATIONS)) e.putBoolean(KEY_MASTER_NOTIFICATIONS, oldEnabled);
            if (!p.contains(KEY_PERSISTENT_COUNTER)) e.putBoolean(KEY_PERSISTENT_COUNTER, oldEnabled);
            // v1 users opted into the counter, not necessarily the new reminder categories.
            if (!p.contains(KEY_ANNIVERSARY)) e.putBoolean(KEY_ANNIVERSARY, false);
            if (!p.contains(KEY_MONTHSARY)) e.putBoolean(KEY_MONTHSARY, false);
            if (!p.contains(KEY_MILESTONE)) e.putBoolean(KEY_MILESTONE, false);
            if (!p.contains(KEY_GIFT)) e.putBoolean(KEY_GIFT, false);
        }

        e.putInt(KEY_SCHEMA, 2).apply();
    }

    public static boolean hasStartDate(Context context) {
        return getStartDate(context) != null;
    }

    public static LocalDate getStartDate(Context context) {
        String value = prefs(context).getString(KEY_DATE, null);
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void setStartDate(Context context, LocalDate date) {
        prefs(context).edit().putString(KEY_DATE, date.toString()).apply();
    }

    public static String getPartnerName(Context context) {
        String value = prefs(context).getString(KEY_PARTNER, "");
        return value == null ? "" : value.trim();
    }

    public static void setPartnerName(Context context, String name) {
        prefs(context).edit().putString(KEY_PARTNER, name == null ? "" : name.trim()).apply();
    }

    public static String partnerDisplayName(Context context) {
        String name = getPartnerName(context);
        return name.isEmpty() ? "your love" : name;
    }

    public static long daysOfLove(Context context) {
        LocalDate start = getStartDate(context);
        if (start == null) return 0;
        long elapsed = ChronoUnit.DAYS.between(start, LocalDate.now());
        return Math.max(0, elapsed + 1); // The first relationship date is Day 1.
    }

    public static String counterText(Context context) {
        if (!hasStartDate(context)) return "Set your love story date ❤️";
        long days = daysOfLove(context);
        String unit = days == 1 ? "day" : "days";
        String name = getPartnerName(context);
        if (name.isEmpty()) return days + " " + unit + " of love ❤️";
        return days + " " + unit + " of love with " + name + " ❤️";
    }

    public static String loveLine(Context context) {
        String name = getPartnerName(context);
        return name.isEmpty() ? "days of love ❤️" : "days of love with " + name + " ❤️";
    }

    public static String formattedDate(Context context) {
        LocalDate date = getStartDate(context);
        return date == null ? "No date selected" : date.format(DISPLAY_FORMAT);
    }

    public static String sinceText(Context context) {
        return hasStartDate(context) ? "Together since " + formattedDate(context) : "Choose the day your story began";
    }

    private static LocalDate anniversaryForYear(LocalDate start, int year) {
        YearMonth ym = YearMonth.of(year, start.getMonthValue());
        int day = Math.min(start.getDayOfMonth(), ym.lengthOfMonth());
        return LocalDate.of(year, start.getMonthValue(), day);
    }

    public static LocalDate nextAnniversaryDate(Context context) {
        LocalDate start = getStartDate(context);
        if (start == null) return null;
        LocalDate today = LocalDate.now();
        LocalDate candidate = anniversaryForYear(start, today.getYear());
        if (candidate.isBefore(today)) candidate = anniversaryForYear(start, today.getYear() + 1);
        return candidate;
    }

    public static int nextAnniversaryNumber(Context context) {
        LocalDate start = getStartDate(context);
        LocalDate next = nextAnniversaryDate(context);
        if (start == null || next == null) return 0;
        return Math.max(0, next.getYear() - start.getYear());
    }

    public static long daysUntilNextAnniversary(Context context) {
        LocalDate next = nextAnniversaryDate(context);
        if (next == null) return -1;
        return Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), next));
    }

    public static String nextAnniversarySummary(Context context) {
        if (!hasStartDate(context)) return "Next anniversary\nSet your date";
        long days = daysUntilNextAnniversary(context);
        int number = nextAnniversaryNumber(context);
        if (days == 0) return ordinal(number) + " anniversary\nToday ❤️";
        return ordinal(number) + " anniversary\n" + days + (days == 1 ? " day" : " days");
    }

    public static String nextAnniversaryNotificationLine(Context context) {
        if (!hasStartDate(context)) return "Set your relationship date";
        long days = daysUntilNextAnniversary(context);
        int number = nextAnniversaryNumber(context);
        if (days == 0) return "Happy " + ordinal(number) + " anniversary ❤️";
        return ordinal(number) + " anniversary in " + days + (days == 1 ? " day" : " days");
    }

    public static final class MonthsaryInfo {
        public final LocalDate date;
        public final long monthNumber;

        MonthsaryInfo(LocalDate date, long monthNumber) {
            this.date = date;
            this.monthNumber = monthNumber;
        }
    }

    private static LocalDate addMonthsClamped(LocalDate start, long months) {
        YearMonth ym = YearMonth.from(start).plusMonths(months);
        int day = Math.min(start.getDayOfMonth(), ym.lengthOfMonth());
        return LocalDate.of(ym.getYear(), ym.getMonthValue(), day);
    }

    public static MonthsaryInfo nextMonthsary(Context context) {
        LocalDate start = getStartDate(context);
        if (start == null) return null;
        LocalDate today = LocalDate.now();

        long approx = ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.from(today));
        long first = Math.max(1, approx - 1);
        for (long month = first; month <= approx + 3; month++) {
            LocalDate date = addMonthsClamped(start, month);
            if (!date.isBefore(today)) return new MonthsaryInfo(date, month);
        }
        long fallback = Math.max(1, approx + 1);
        return new MonthsaryInfo(addMonthsClamped(start, fallback), fallback);
    }

    public static long daysUntilNextMonthsary(Context context) {
        MonthsaryInfo info = nextMonthsary(context);
        return info == null ? -1 : Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), info.date));
    }

    public static String nextMonthsarySummary(Context context) {
        MonthsaryInfo info = nextMonthsary(context);
        if (info == null) return "Next monthsary\nSet your date";
        long days = daysUntilNextMonthsary(context);
        if (days == 0) return AppState.ordinal(info.monthNumber) + " monthsary\nToday 💗";
        return AppState.ordinal(info.monthNumber) + " monthsary\n" + days + (days == 1 ? " day" : " days");
    }

    public static long nextMilestone(Context context) {
        long days = daysOfLove(context);
        long[] milestones = new long[]{100, 250, 500, 750, 1000, 1500, 2000, 2500, 3000, 4000, 5000};
        for (long m : milestones) if (days < m) return m;
        return ((days / 1000) + 1) * 1000;
    }

    public static String nextMilestoneSummary(Context context) {
        if (!hasStartDate(context)) return "Next milestone\nSet your date";
        long next = nextMilestone(context);
        long remaining = Math.max(0, next - daysOfLove(context));
        return next + " days\n" + remaining + (remaining == 1 ? " day to go" : " days to go");
    }

    public static String ordinal(long number) {
        long mod100 = number % 100;
        if (mod100 >= 11 && mod100 <= 13) return number + "th";
        switch ((int) (number % 10)) {
            case 1: return number + "st";
            case 2: return number + "nd";
            case 3: return number + "rd";
            default: return number + "th";
        }
    }

    // Appearance
    public static String widgetTheme(Context context) {
        return prefs(context).getString(KEY_THEME, WidgetThemes.DEFAULT_THEME);
    }

    public static void setWidgetTheme(Context context, String value) {
        prefs(context).edit().putString(KEY_THEME, value).apply();
    }

    public static int widgetBackgroundColor(Context context) {
        if (prefs(context).contains(KEY_BG_COLOR)) return prefs(context).getInt(KEY_BG_COLOR, WidgetThemes.DEFAULT_BG);
        return WidgetThemes.backgroundFor(widgetTheme(context));
    }

    public static void setWidgetBackgroundColor(Context context, int color) {
        prefs(context).edit().putInt(KEY_BG_COLOR, color).apply();
    }

    public static int widgetTextColor(Context context) {
        if (prefs(context).contains(KEY_TEXT_COLOR)) return prefs(context).getInt(KEY_TEXT_COLOR, Color.WHITE);
        return WidgetThemes.textFor(widgetTheme(context));
    }

    public static void setWidgetTextColor(Context context, int color) {
        prefs(context).edit().putInt(KEY_TEXT_COLOR, color).apply();
    }

    public static int widgetBackgroundStrength(Context context) {
        return Math.max(0, Math.min(100, prefs(context).getInt(KEY_BG_STRENGTH, 100)));
    }

    public static void setWidgetBackgroundStrength(Context context, int strength) {
        prefs(context).edit().putInt(KEY_BG_STRENGTH, Math.max(0, Math.min(100, strength))).apply();
    }

    public static void resetAppearance(Context context) {
        prefs(context).edit()
                .putString(KEY_THEME, WidgetThemes.DEFAULT_THEME)
                .putInt(KEY_BG_COLOR, WidgetThemes.DEFAULT_BG)
                .putInt(KEY_TEXT_COLOR, Color.WHITE)
                .putInt(KEY_BG_STRENGTH, 100)
                .apply();
    }

    public static String colorToHex(int color) {
        return String.format(Locale.US, "#%06X", (0xFFFFFF & color));
    }

    // Notification settings
    public static boolean masterNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_MASTER_NOTIFICATIONS, false);
    }

    public static void setMasterNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_MASTER_NOTIFICATIONS, enabled).apply();
    }

    public static boolean persistentCounterEnabled(Context context) {
        return prefs(context).getBoolean(KEY_PERSISTENT_COUNTER, false);
    }

    public static void setPersistentCounterEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_PERSISTENT_COUNTER, enabled).apply();
    }

    public static boolean anniversaryNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ANNIVERSARY, true);
    }

    public static void setAnniversaryNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ANNIVERSARY, enabled).apply();
    }

    public static boolean monthsaryNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_MONTHSARY, true);
    }

    public static void setMonthsaryNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_MONTHSARY, enabled).apply();
    }

    public static boolean milestoneNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_MILESTONE, true);
    }

    public static void setMilestoneNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_MILESTONE, enabled).apply();
    }

    public static boolean giftNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_GIFT, false);
    }

    public static void setGiftNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_GIFT, enabled).apply();
    }

    public static int reminderHour(Context context) {
        int value = prefs(context).getInt(KEY_REMINDER_HOUR, 9);
        return value == 12 || value == 18 || value == 20 ? value : 9;
    }

    public static void setReminderHour(Context context, int hour) {
        prefs(context).edit().putInt(KEY_REMINDER_HOUR, hour).apply();
    }

    public static boolean anniversaryOffsetEnabled(Context context, int daysBefore) {
        switch (daysBefore) {
            case 30: return prefs(context).getBoolean(KEY_ANNIV_30, false);
            case 14: return prefs(context).getBoolean(KEY_ANNIV_14, true);
            case 7: return prefs(context).getBoolean(KEY_ANNIV_7, true);
            case 3: return prefs(context).getBoolean(KEY_ANNIV_3, true);
            case 1: return prefs(context).getBoolean(KEY_ANNIV_1, true);
            case 0: return prefs(context).getBoolean(KEY_ANNIV_0, true);
            default: return false;
        }
    }

    public static boolean anniversaryFinalWeekDailyEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ANNIV_FINAL_WEEK, false);
    }

    public static void setAnniversaryFinalWeekDailyEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ANNIV_FINAL_WEEK, enabled).apply();
    }

    public static void setAnniversaryOffsetEnabled(Context context, int daysBefore, boolean enabled) {
        String key;
        switch (daysBefore) {
            case 30: key = KEY_ANNIV_30; break;
            case 14: key = KEY_ANNIV_14; break;
            case 7: key = KEY_ANNIV_7; break;
            case 3: key = KEY_ANNIV_3; break;
            case 1: key = KEY_ANNIV_1; break;
            case 0: key = KEY_ANNIV_0; break;
            default: return;
        }
        prefs(context).edit().putBoolean(key, enabled).apply();
    }

    public static boolean monthsaryOffsetEnabled(Context context, int daysBefore) {
        if (daysBefore == 1) return prefs(context).getBoolean(KEY_MONTHSARY_1, false);
        if (daysBefore == 0) return prefs(context).getBoolean(KEY_MONTHSARY_0, true);
        return false;
    }

    public static void setMonthsaryOffsetEnabled(Context context, int daysBefore, boolean enabled) {
        if (daysBefore == 1) prefs(context).edit().putBoolean(KEY_MONTHSARY_1, enabled).apply();
        if (daysBefore == 0) prefs(context).edit().putBoolean(KEY_MONTHSARY_0, enabled).apply();
    }

    private static String lastEventKey(String category) {
        switch (category) {
            case "anniversary": return KEY_LAST_ANNIV;
            case "monthsary": return KEY_LAST_MONTHSARY;
            case "milestone": return KEY_LAST_MILESTONE;
            case "gift": return KEY_LAST_GIFT;
            default: return "last_event_" + category;
        }
    }

    public static boolean shouldSendEvent(Context context, String category, String eventKey) {
        return !eventKey.equals(prefs(context).getString(lastEventKey(category), ""));
    }

    public static void markEventSent(Context context, String category, String eventKey) {
        prefs(context).edit().putString(lastEventKey(category), eventKey).apply();
    }
}
