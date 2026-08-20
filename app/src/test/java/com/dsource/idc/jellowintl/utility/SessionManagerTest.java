package com.dsource.idc.jellowintl.utility;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SessionManagerTest {

    private SessionManager sessionManager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sessionManager = new SessionManager(context);
    }

    @Test
    public void testUserLoggedIn() {
        assertFalse(sessionManager.isUserLoggedIn());
        sessionManager.setUserLoggedIn(true);
        assertTrue(sessionManager.isUserLoggedIn());
    }

    @Test
    public void testBloodGroup() {
        assertEquals(0, sessionManager.getBlood());
        sessionManager.setBlood(2);
        assertEquals(2, sessionManager.getBlood());
    }

    @Test
    public void testNameAndEmail() {
        assertEquals("", sessionManager.getName());
        assertEquals("", sessionManager.getEmailId());

        sessionManager.setName("John Doe");
        sessionManager.setEmailId("john@example.com");

        assertEquals("John Doe", sessionManager.getName());
        assertEquals("john@example.com", sessionManager.getEmailId());
    }

    @Test
    public void testCaregiverDetails() {
        assertEquals("", sessionManager.getCaregiverName());
        assertEquals("", sessionManager.getCaregiverNumber());

        sessionManager.setCaregiverName("Jane Doe");
        sessionManager.setCaregiverNumber("1234567890");

        assertEquals("Jane Doe", sessionManager.getCaregiverName());
        assertEquals("1234567890", sessionManager.getCaregiverNumber());
    }

    @Test
    public void testLanguageAndVoices() {
        assertEquals("", sessionManager.getLanguage());
        assertEquals("", sessionManager.getAppVoice());
        assertEquals("", sessionManager.getBoardVoice());

        sessionManager.setLanguage(SessionManager.ENG_IN);
        sessionManager.setAppVoice("Voice1");
        sessionManager.setBoardVoice("Voice2");

        assertEquals(SessionManager.ENG_IN, sessionManager.getLanguage());
        assertEquals("Voice1", sessionManager.getAppVoice());
        assertEquals("Voice2", sessionManager.getBoardVoice());
    }

    @Test
    public void testSpeedAndPitch() {
        // Defaults should be 50
        assertEquals(50, sessionManager.getSpeed());
        assertEquals(50, sessionManager.getPitch());

        sessionManager.setSpeed(75);
        sessionManager.setPitch(80);

        assertEquals(75, sessionManager.getSpeed());
        assertEquals(80, sessionManager.getPitch());
    }

    @Test
    public void testGridSizeAndPictureViewMode() {
        assertEquals(0, sessionManager.getGridSize());
        assertEquals(0, sessionManager.getPictureViewMode());

        sessionManager.setGridSize(4);
        sessionManager.setPictureViewMode(1);

        assertEquals(4, sessionManager.getGridSize());
        assertEquals(1, sessionManager.getPictureViewMode());
        assertTrue(sessionManager.isGridSizeKeyExist());
    }

    @Test
    public void testVariousBooleanStates() {
        assertFalse(sessionManager.isCompletedIntro());
        sessionManager.setCompletedIntro(true);
        assertTrue(sessionManager.isCompletedIntro());

        assertFalse(sessionManager.isCallingEnabled());
        sessionManager.setEnableCalling(true);
        assertTrue(sessionManager.isCallingEnabled());

        assertFalse(sessionManager.getTextBarVisibility());
        sessionManager.setTextBarVisibility(true);
        assertTrue(sessionManager.getTextBarVisibility());
    }
}
