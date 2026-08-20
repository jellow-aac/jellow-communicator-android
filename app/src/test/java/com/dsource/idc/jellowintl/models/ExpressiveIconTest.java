package com.dsource.idc.jellowintl.models;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExpressiveIconTest {

    @Test
    public void testExpressiveIcon() {
        ExpressiveIcon icon = new ExpressiveIcon();
        
        assertNull(icon.getL());
        assertNull(icon.getLL());
        assertNull(icon.getTitle());

        icon.setL("Level");
        icon.setLL("LanguageLevel");
        icon.setTitle("My Title");

        assertEquals("Level", icon.getL());
        assertEquals("LanguageLevel", icon.getLL());
        assertEquals("My Title", icon.getTitle());
    }
}
