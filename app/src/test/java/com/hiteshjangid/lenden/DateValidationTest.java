package com.hiteshjangid.lenden;

import org.junit.Test;
import static org.junit.Assert.*;
import com.hiteshjangid.lenden.util.AppSettingsManager;

public class DateValidationTest {

    @Test
    public void testLeapYearValidation() {
        assertTrue("February 29, 2024 should be valid (leap year)", 
                   AppSettingsManager.isValidDateFormat("29-02-2024"));
        
        assertFalse("February 29, 2023 should be invalid (not leap year)",
                    AppSettingsManager.isValidDateFormat("29-02-2023"));
        
        assertTrue("February 28, 2023 should be valid",
                   AppSettingsManager.isValidDateFormat("28-02-2023"));
    }

    @Test
    public void testMonthSpecificDayLimits() {
        assertTrue("January 31 should be valid", 
                   AppSettingsManager.isValidDateFormat("31-01-2024"));
        assertTrue("March 31 should be valid", 
                   AppSettingsManager.isValidDateFormat("31-03-2024"));
        
        assertTrue("April 30 should be valid", 
                   AppSettingsManager.isValidDateFormat("30-04-2024"));
        assertFalse("April 31 should be invalid", 
                    AppSettingsManager.isValidDateFormat("31-04-2024"));
        
        assertFalse("September 31 should be invalid", 
                    AppSettingsManager.isValidDateFormat("31-09-2024"));
    }

    @Test
    public void testCenturyLeapYears() {
        assertFalse("February 29, 1900 should be invalid (not leap year)",
                    AppSettingsManager.isValidDateFormat("29-02-1900"));
        assertTrue("February 29, 2000 should be valid (leap year)",
                   AppSettingsManager.isValidDateFormat("29-02-2000"));
    }

    @Test
    public void testEdgeCases() {
        assertTrue("December 31, 2024 should be valid",
                   AppSettingsManager.isValidDateFormat("31-12-2024"));
        assertTrue("January 1, 2024 should be valid", 
                   AppSettingsManager.isValidDateFormat("01-01-2024"));
        
        assertFalse("Invalid format should be rejected",
                    AppSettingsManager.isValidDateFormat("32-01-2024"));
        assertFalse("Invalid month should be rejected",
                    AppSettingsManager.isValidDateFormat("15-13-2024"));
    }
}
