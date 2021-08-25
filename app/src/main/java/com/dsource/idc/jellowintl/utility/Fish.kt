package com.dsource.idc.jellowintl.utility

import com.dsource.idc.jellowintl.R

class Fish(
    val soundTime: Long,
    val endTime: Long,
    val view: IntArray,
    val fishType: Int,
    val animSound: String
){
    companion object {
        fun getFish(deviceType: String) : Fish {
            return when(deviceType){
                "small" -> Fish(1550, 1000, intArrayOf(R.id.animFish, R.id.animDolphin, R.id.animWhale),
                    R.drawable.fish_jump,"fish_splash.mp3")
                "medium"-> Fish(1550, 2500, intArrayOf(R.id.animFish, R.id.animDolphin, R.id.animWhale),
                    R.drawable.fish_jump,"fish_splash.mp3")
                "large" -> Fish(1900, 1000, intArrayOf(R.id.animFish, R.id.animDolphin, R.id.animWhale),
                    R.drawable.fish_jump,"fish_splash.mp3")
                else -> Fish(1550, 2500, intArrayOf(R.id.animFish, R.id.animDolphin, R.id.animWhale),
                    R.drawable.fish_jump,"fish_splash.mp3")
            }
        }

        fun getDolphin(deviceType: String) : Fish {
            return when(deviceType){
                "small" -> Fish(2000, 1250, intArrayOf(R.id.animDolphin, R.id.animFish, R.id.animWhale),
                    R.drawable.dolphin_jump,"dolphin_splash.mp3")
                "medium"-> Fish(2000, 3500, intArrayOf(R.id.animDolphin, R.id.animFish, R.id.animWhale),
                    R.drawable.dolphin_jump,"dolphin_splash.mp3")
                "large" -> Fish(2000, 1000, intArrayOf(R.id.animDolphin, R.id.animFish, R.id.animWhale),
                    R.drawable.dolphin_jump,"dolphin_splash.mp3")
                else -> Fish(2000, 3500, intArrayOf(R.id.animDolphin, R.id.animFish, R.id.animWhale),
                    R.drawable.dolphin_jump,"dolphin_splash.mp3")
            }
        }

        fun getWhale(deviceType: String) : Fish {
            return when(deviceType){
                "small" -> Fish(2300, 1000, intArrayOf(R.id.animWhale, R.id.animFish, R.id.animDolphin),
                    R.drawable.whale_jump,"whale_splash.mp3")
                "medium"-> Fish(2300, 3500, intArrayOf(R.id.animWhale, R.id.animFish, R.id.animDolphin),
                    R.drawable.whale_jump,"whale_splash.mp3")
                "large" -> Fish(2300, 1000, intArrayOf(R.id.animWhale, R.id.animFish, R.id.animDolphin),
                    R.drawable.whale_jump,"whale_splash.mp3")
                else -> Fish(2300, 3500, intArrayOf(R.id.animWhale, R.id.animFish, R.id.animDolphin),
                    R.drawable.whale_jump,"whale_splash.mp3")
            }
        }
    }
}