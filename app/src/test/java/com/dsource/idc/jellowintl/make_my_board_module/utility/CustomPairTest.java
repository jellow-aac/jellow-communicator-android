package com.dsource.idc.jellowintl.make_my_board_module.utility;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CustomPairTest {

    @Test
    public void testCustomPair() {
        CustomPair<String, Integer> pair = new CustomPair<>("test", 100);
        assertEquals("test", pair.getFirst());
        assertEquals(Integer.valueOf(100), pair.getSecond());

        pair.setFirst("updated");
        pair.setSecond(200);

        assertEquals("updated", pair.getFirst());
        assertEquals(Integer.valueOf(200), pair.getSecond());
    }
}
