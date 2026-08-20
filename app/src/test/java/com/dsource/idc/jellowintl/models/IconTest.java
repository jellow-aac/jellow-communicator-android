package com.dsource.idc.jellowintl.models;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IconTest {

    @Test
    public void testIconProperties() {
        Icon icon = new Icon();
        
        assertNull(icon.getDisplay_Label());
        assertNull(icon.getEvent_Tag());
        assertNull(icon.getSearchTag());

        icon.setDisplay_Label("Apple");
        icon.setEvent_Tag("fruit_apple");
        icon.setSearchTag("red, fruit");
        
        assertEquals("Apple", icon.getDisplay_Label());
        assertEquals("fruit_apple", icon.getEvent_Tag());
        assertEquals("red, fruit", icon.getSearchTag());
    }
}
