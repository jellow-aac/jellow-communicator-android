package com.dsource.idc.jellowintl.utility;

import com.dsource.idc.jellowintl.R;

public class Fish {
    public final long soundTime;
    public final int animViewId, fishType;
    public final String animSound;

    private Fish(long soundTime, int view, int type, String snd) {
        this.soundTime = soundTime;
        this.animViewId = view; this.fishType = type; this.animSound = snd;
    }

    public static Fish get(int species, String size) {
        if (species == 1) return Dolphin.get(size);
        if (species == 2) return Whale.get(size);
        return JellowFish.get(size);
    }

    private static Fish select(String size, Fish s, Fish m, Fish l) {
        return "small".equalsIgnoreCase(size) ? s : ("medium".equalsIgnoreCase(size) ? m : l);
    }

    public static class JellowFish extends Fish {
        public static final Fish SMALL = new JellowFish(850), MEDIUM = new JellowFish(900), LARGE = new JellowFish(1000);
        private JellowFish(long splashTime) { super(splashTime, R.id.animFish, R.drawable.fish_jump, "fish_splash.mp3"); }
        public static Fish get(String s) { return select(s, SMALL, MEDIUM, LARGE); }
    }

    public static class Dolphin extends Fish {
        public static final Fish SMALL = new Dolphin(2000), MEDIUM = new Dolphin(2000), LARGE = new Dolphin(3100);
        private Dolphin(long splashTime) { super(splashTime, R.id.animDolphin, R.drawable.dolphin_jump, "dolphin_splash.mp3"); }
        public static Fish get(String s) { return select(s, SMALL, MEDIUM, LARGE); }
    }

    public static class Whale extends Fish {
        public static final Fish SMALL = new Whale(3700), MEDIUM = new Whale(3700), LARGE = new Whale(3700);
        private Whale(long splashTime) { super(splashTime, R.id.animWhale, R.drawable.whale_jump, "whale_splash.mp3"); }
        public static Fish get(String s) { return select(s, SMALL, MEDIUM, LARGE); }
    }
}
