package com.gathin.tvhome;

import android.content.res.Resources;

import android.icu.util.HebrewCalendar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Hebrew (Jewish) calendar date under the date line, e.g. "ל׳ בחשוון תשפ״ז".
 * Shown instead of the lunisolar date when lunar_date is on and the locale is Hebrew.
 * The calendar math is the bundled ICU android.icu.util.HebrewCalendar; only the
 * gematria numerals and month names are ours.
 */
final class Hebrew {

    // ICU HebrewCalendar months are 0-based and Tishri-first: TISHRI=0 … ELUL=12.
    // ADAR_1=5 exists only in a leap year; ADAR=6 is the plain year's Adar and a leap year's Adar II.
    private static final String[] MONTHS = {
            "תשרי", "חשוון", "כסלו", "טבת", "שבט", "אדר", "אדר ב׳",
            "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול"
    };
    private static final int[] VALUES = {
            400, 300, 200, 100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
    private static final char[] LETTERS = {
            'ת', 'ש', 'ר', 'ק', 'צ', 'פ', 'ע', 'ס', 'נ', 'מ', 'ל', 'כ', 'י', 'ט', 'ח', 'ז', 'ו', 'ה', 'ד', 'ג', 'ב', 'א'};

    private Hebrew() {
    }

    /** True when the device speaks Hebrew (Java reports "iw", Android 13+ also "he"). */
    static boolean isLocale() {
        String lang = java.util.Locale.getDefault().getLanguage();
        return "iw".equals(lang) || "he".equals(lang);
    }

    /** Today's Hebrew date. Fixed Hebrew words, as this line is only shown on a Hebrew locale. */
    static String today() {
        HebrewCalendar c = new HebrewCalendar();
        c.setTime(new Date());
        int y = c.get(HebrewCalendar.YEAR);
        int m = c.get(HebrewCalendar.MONTH);
        int d = c.get(HebrewCalendar.DAY_OF_MONTH);
        // ICU numbers the months Tishri-first and jumps from Shevat (4) straight to Adar (6) in a
        // plain year. Adar I (5) exists only in a leap year, when 6 is Adar II.
        return numeral(d) + " ב" + monthName(m, isLeapYear(y)) + " " + numeral(y % 1000);
    }

    /** ICU month index to its Hebrew name; see the comment at MONTHS. */
    static String monthName(int m, boolean leap) {
        return m == 5 ? "אדר א׳" : m == 6 && !leap ? MONTHS[5] : MONTHS[m];
    }

    /** Leap years in the 19-year Hebrew cycle (years 3, 6, 8, 11, 14, 17 and 19 of it). */
    static boolean isLeapYear(int year) {
        int rest = year % 19;
        return rest == 0 || rest == 3 || rest == 6 || rest == 8 || rest == 11 || rest == 14 || rest == 17;
    }

    /**
     * The date line for Home and the clock screensaver: today(), and on Friday/Saturday also the
     * day's shabbat time — candle lighting (sunset - 18 min) on Friday, havdalah (sunset + 42 min)
     * on Saturday — when config.xml's latitude/longitude are set. Plain today() on other days
     * or without coordinates.
     */
    static String line(Resources res) {
        String date = today();
        int sunset = sunsetToday(res);
        if (sunset < 0) return date;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.FRIDAY) return date + " · כניסת שבת " + minuteString(sunset - 18);
        if (day == Calendar.SATURDAY) return date + " · צאת שבת " + minuteString(sunset + 42);
        return date;
    }

    /** Local minutes of today's sunset at config.xml's coordinates, or -1 without them. */
    static int sunsetToday(Resources res) {
        try {
            double lat = Double.parseDouble(res.getString(R.string.latitude));
            double lon = Double.parseDouble(res.getString(R.string.longitude));
            int[] riseSet = Sun.riseAndSet(LocalDate.now(), lat, lon, ZoneId.systemDefault());
            return riseSet == null ? -1 : riseSet[1];
        } catch (RuntimeException e) {
            return -1;   // empty or unparsable coordinates
        }
    }

    /** "16:34" from minutes of day. */
    static String minuteString(int min) {
        return String.format(java.util.Locale.ROOT, "%02d:%02d", min / 60, min % 60);
    }

    /** Gematria numeral: 30 → ל׳, 787 → תשפ״ז. The divine-name pairs יה/יו become טו/טז. */
    static String numeral(int n) {
        if (n <= 0 || n >= 1000) return String.valueOf(n);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (n >= VALUES[i]) {
                s.append(LETTERS[i]);
                n -= VALUES[i];
            }
        }
        String g = s.toString().replace("יה", "טו").replace("יו", "טז");
        return g.length() > 1
                ? g.substring(0, g.length() - 1) + "״" + g.substring(g.length() - 1)
                : g + "׳";
    }
}
