package com.dsource.idc.jellowintl.utility;

import com.dsource.idc.jellowintl.R;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FishTest {

    @Test
    public void testGetJellowFish() {
        Fish small = Fish.get(Fish.fish, "small");
        assertEquals(1100, small.soundTime);
        assertEquals(R.id.animFish, small.animViewId);
        assertEquals(R.drawable.fish_jump, small.fishType);
        assertEquals("fish_splash.mp3", small.animSound);

        Fish medium = Fish.get(Fish.fish, "medium");
        assertEquals(900, medium.soundTime);

        Fish large = Fish.get(Fish.fish, "large");
        assertEquals(1000, large.soundTime);
        
        Fish largeCaseInsensitive = Fish.get(Fish.fish, "LaRgE");
        assertEquals(1000, largeCaseInsensitive.soundTime);
    }

    @Test
    public void testGetDolphin() {
        Fish small = Fish.get(Fish.dolphin, "small");
        assertEquals(3300, small.soundTime);
        assertEquals(R.id.animDolphin, small.animViewId);
        assertEquals(R.drawable.dolphin_jump, small.fishType);
        assertEquals("dolphin_splash.mp3", small.animSound);

        Fish medium = Fish.get(Fish.dolphin, "medium");
        assertEquals(2000, medium.soundTime);

        Fish large = Fish.get(Fish.dolphin, "large");
        assertEquals(3100, large.soundTime);
    }

    @Test
    public void testGetWhale() {
        Fish small = Fish.get(Fish.whale, "small");
        assertEquals(3800, small.soundTime);
        assertEquals(R.id.animWhale, small.animViewId);
        assertEquals(R.drawable.whale_jump, small.fishType);
        assertEquals("whale_splash.mp3", small.animSound);

        Fish medium = Fish.get(Fish.whale, "medium");
        assertEquals(3700, medium.soundTime);

        Fish large = Fish.get(Fish.whale, "large");
        assertEquals(3700, large.soundTime);
    }
}
