package com.gathin.tvhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HebrewTest {

    @Test
    public void singleDigitsGetGeresh() {
        assertEquals("א׳", Hebrew.numeral(1));
        assertEquals("י׳", Hebrew.numeral(10));
    }

    @Test
    public void tensPairLettersWithGershayim() {
        assertEquals("כ״ט", Hebrew.numeral(29));
        assertEquals("ל׳", Hebrew.numeral(30));
    }

    @Test
    public void divineNamePairsAreSpelledDifferently() {
        assertEquals("ט״ו", Hebrew.numeral(15));
        assertEquals("ט״ז", Hebrew.numeral(16));
        assertEquals("תשט״ו", Hebrew.numeral(715));
        assertEquals("תשט״ז", Hebrew.numeral(716));
    }

    @Test
    public void hundredsChainDown() {
        assertEquals("תשפ״ז", Hebrew.numeral(787));
        assertEquals("שע״ה", Hebrew.numeral(375));
    }

    @Test
    public void outOfRangePrintsTheNumber() {
        assertEquals("0", Hebrew.numeral(0));
        assertEquals("1000", Hebrew.numeral(1000));
    }

    @Test
    public void leapYearsAreTheSevenOfEachNineteenYearCycle() {
        assertFalse(Hebrew.isLeapYear(5786));
        assertTrue(Hebrew.isLeapYear(5787));
        assertFalse(Hebrew.isLeapYear(5788));
    }

    @Test
    public void adarDependsOnTheLeapYear() {
        assertEquals("אדר", Hebrew.monthName(6, false));
        assertEquals("אדר א׳", Hebrew.monthName(5, true));
        assertEquals("אדר ב׳", Hebrew.monthName(6, true));
    }

    @Test
    public void minuteStringPadsZeroes() {
        assertEquals("16:34", Hebrew.minuteString(994));
        assertEquals("00:07", Hebrew.minuteString(7));
        assertEquals("23:59", Hebrew.minuteString(1439));
    }
}
