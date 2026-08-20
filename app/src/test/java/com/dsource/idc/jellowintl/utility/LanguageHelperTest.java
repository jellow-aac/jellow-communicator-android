package com.dsource.idc.jellowintl.utility;

import android.content.Context;
import android.content.res.Configuration;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28) // Use a stable SDK version for Robolectric
public class LanguageHelperTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testSetLanguage() {
        Locale newLocale = new Locale("hi", "IN");
        Context newContext = LanguageHelper.setLanguage(context, newLocale);
        
        assertNotNull(newContext);
        Configuration config = newContext.getResources().getConfiguration();
        assertEquals("hi", config.locale.getLanguage());
        assertEquals("IN", config.locale.getCountry());
    }

    @Test
    public void testOnAttachWithLanguageString() {
        Context newContext = LanguageHelper.onAttach(context, "mr-rIN");
        
        assertNotNull(newContext);
        Configuration config = newContext.getResources().getConfiguration();
        assertEquals("mr", config.locale.getLanguage());
        assertEquals("IN", config.locale.getCountry());
    }
    
    @Test
    public void testOnAttachWithEmptyLanguageString() {
        Context newContext = LanguageHelper.onAttach(context, "");
        
        assertNotNull(newContext);
        Configuration config = newContext.getResources().getConfiguration();
        assertEquals("en", config.locale.getLanguage());
        assertEquals("IN", config.locale.getCountry());
    }
}
