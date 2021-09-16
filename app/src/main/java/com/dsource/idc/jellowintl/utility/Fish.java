package com.dsource.idc.jellowintl.utility;

import com.dsource.idc.jellowintl.R;

public class Fish {
    private final Long soundTime;
    private final Long endTime;
    private final int[] view;
    private final int fishType;
    private final String animSound;

    public Fish(Long soundTime, Long endTime, int[] view, int fishType, String animSound){
        this.soundTime = soundTime;
        this.endTime = endTime;
        this.view = view;
        this.fishType = fishType;
        this.animSound = animSound;
    }

    public static Fish getFish(String deviceType){
        if(deviceType.equals("small")){
            return new Fish(850L, 1700L, new int[]{R.id.animFish, R.id.animDolphin, R.id.animWhale},
                    R.drawable.fish_jump,"fish_splash.mp3");
        }else if(deviceType.equals("medium")) {
            return new Fish(900L, 1850L, new int[]{R.id.animFish, R.id.animDolphin, R.id.animWhale},
                    R.drawable.fish_jump,"fish_splash.mp3");
        }else {
            return new Fish(900L, 1850L, new int[]{R.id.animFish, R.id.animDolphin, R.id.animWhale},
                    R.drawable.fish_jump,"fish_splash.mp3");
        }
    }

    public static Fish getDolphin(String deviceType){
        if(deviceType.equals("small")){
            return new Fish(2000L, 1250L, new int[]{R.id.animDolphin, R.id.animFish, R.id.animWhale},
                    R.drawable.dolphin_jump,"dolphin_splash.mp3");
        }else if(deviceType.equals("medium")) {
            return new Fish(2000L, 1000L, new int[]{R.id.animDolphin, R.id.animFish, R.id.animWhale},
                    R.drawable.dolphin_jump,"dolphin_splash.mp3");
        }else {
            return new Fish(2000L, 1000L, new int[]{R.id.animDolphin, R.id.animFish, R.id.animWhale},
                    R.drawable.dolphin_jump,"dolphin_splash.mp3");
        }
    }

    public static Fish getWhale(String deviceType){
        if(deviceType.equals("small")){
            return new Fish(2300L, 2500L, new int[]{R.id.animWhale, R.id.animFish, R.id.animDolphin},
                    R.drawable.whale_jump,"whale_splash.mp3");
        }else if(deviceType.equals("medium")) {
            return new Fish(2300L, 3100L, new int[]{R.id.animWhale, R.id.animFish, R.id.animDolphin},
                    R.drawable.whale_jump,"whale_splash.mp3");
        }else {
            return new Fish(2300L, 3100L, new int[]{R.id.animWhale, R.id.animFish, R.id.animDolphin},
                    R.drawable.whale_jump,"whale_splash.mp3");
        }
    }

    public Long getSoundTime() {
        return soundTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public int[] getView() {
        return view;
    }

    public int getFishType() {
        return fishType;
    }

    public String getAnimSound() {
        return animSound;
    }
}
