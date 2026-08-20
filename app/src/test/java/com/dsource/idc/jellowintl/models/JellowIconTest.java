package com.dsource.idc.jellowintl.models;

import com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JellowIconTest {

    @Test
    public void testConstructorsAndGetters() {
        JellowIcon icon1 = new JellowIcon("Title1", "drawable1", 0, 1, 2);
        assertEquals("Title1", icon1.getIconTitle());
        assertEquals("drawable1", icon1.getIconDrawable());
        assertEquals("Title1", icon1.getIconSpeech()); // set to Title1 in this constructor
        assertEquals(0, icon1.getParent0());
        assertEquals(1, icon1.getParent1());
        assertEquals(2, icon1.getParent2());
        
        JellowIcon icon2 = new JellowIcon("Title2", "Speech2", "drawable2", -1, -1, -1);
        assertEquals("Title2", icon2.getIconTitle());
        assertEquals("Speech2", icon2.getIconSpeech());
        assertEquals("drawable2", icon2.getIconDrawable());
        assertEquals(-1, icon2.getParent0());
    }

    @Test
    public void testVerbiageConstructor() {
        // e.g. verbiageID format usually is something like "xx01020003"
        // Let's pass a safe one for the try/catch in constructor
        JellowIcon icon = new JellowIcon("EE02030004", "IconTitle", "IconSpeech", "drawable");
        assertEquals("IconTitle", icon.getIconTitle());
        assertEquals("IconSpeech", icon.getIconSpeech());
        assertEquals("drawable", icon.getIconDrawable());
        // 02 -> 1, 03 -> 2, 0004 -> 3
        assertEquals(1, icon.getParent0());
        assertEquals(2, icon.getParent1());
        assertEquals(3, icon.getParent2());
        assertFalse(icon.isSequenceIcon()); // doesn't contain "SS"
    }

    @Test
    public void testVerbiageConstructorSequence() {
        JellowIcon icon = new JellowIcon("SS01020003", "Sequence", "Speech", "drawable");
        assertEquals(0, icon.getParent0());
        assertEquals(1, icon.getParent1());
        assertEquals(2, icon.getParent2());
        assertTrue(icon.isSequenceIcon());
    }

    @Test
    public void testIsCustomIcon() {
        JellowIcon icon = new JellowIcon("Title", "Speech", "drawable", -1, 0, 0);
        icon.setVerbiageId("shortid");
        assertTrue(icon.isCustomIcon());
        
        icon.setVerbiageId("verylongid12345");
        assertFalse(icon.isCustomIcon());
    }

    @Test
    public void testEqualsAndCompareTo() {
        JellowIcon icon1 = new JellowIcon("EE01020003", "Title1", "Speech1", "drawable1");
        JellowIcon icon2 = new JellowIcon("EE01020003", "Title2", "Speech2", "drawable2");
        JellowIcon icon3 = new JellowIcon("EE01020004", "Title1", "Speech1", "drawable1");

        assertTrue(icon1.equals(icon2));
        assertFalse(icon1.equals(icon3));
        
        assertEquals(0, icon1.compareTo(icon2));
        assertTrue(icon1.compareTo(icon3) < 0);
    }
}
