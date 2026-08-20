package com.dsource.idc.jellowintl;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.dsource.idc.jellowintl.utility.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static com.dsource.idc.jellowintl.utility.SessionManager.ENG_IN;

@RunWith(AndroidJUnit4.class)
public class SessionManagerTest {
    private SessionManager mSession;

    @Before
    public void setup(){
        Context context = ApplicationProvider.getApplicationContext();
        mSession = new SessionManager(context);
    }

    @Test
    public void checkSetUserLoggedIn(){
        mSession.setUserLoggedIn(true);
        assertTrue(mSession.isUserLoggedIn());
    }

    @Test
    public void checkSetBlood(){
        mSession.setBlood(0);
        assertEquals(0, mSession.getBlood());
    }

    @Test
    public void checkSetName(){
        mSession.setName("Prakash");
        assertEquals("Prakash", mSession.getName());
    }

    @Test
    public void checkSetEmailId(){
        mSession.setEmailId("jellowcommunicator@gmail.com");
        assertEquals("jellowcommunicator@gmail.com", mSession.getEmailId());
    }

    @Test
    public void checkSetCaregiverNumber(){
        mSession.setCaregiverNumber("9653238072");
        assertEquals("9653238072", mSession.getCaregiverNumber());
    }

    @Test
    public void checkSetCaregiverName(){
        mSession.setCaregiverName("Anjali");
        assertEquals("Anjali", mSession.getCaregiverName());
    }

    @Test
    public void checkSetAddress(){
        mSession.setAddress("IIT Bombay");
        assertEquals("IIT Bombay", mSession.getAddress());
    }

    @Test
    public void checkSetLanguage(){
        mSession.setLanguage(ENG_IN);
        assertEquals(ENG_IN, mSession.getLanguage());
    }

    @Test
    public void checkSetPictureViewMode(){
        mSession.setPictureViewMode(0);
        assertEquals(0, mSession.getPictureViewMode());
    }

    @Test
    public void checkSetGridSize(){
        mSession.setGridSize(0);
        assertEquals(0, mSession.getGridSize());
    }

    @Test
    public void checkSetSpeed(){
        mSession.setSpeed(10);
        assertEquals(10, mSession.getSpeed());
        mSession.setSpeed(0);
        assertEquals(50, mSession.getSpeed());
    }

    @Test
    public void checkSetPitch(){
        mSession.setPitch(20);
        assertEquals(20, mSession.getPitch());
        mSession.setPitch(0);
        assertEquals(50, mSession.getPitch());
    }

    @Test
    public void addressTest(){
        String address = "Test";
        mSession.setAddress(address);
        assertEquals(address, mSession.getAddress());
    }
}
