package com.dsource.idc.jellowintl.utility;

import android.app.Activity;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SystemBars {

    public static void setupSystemBars(Activity activity, @ColorRes int statusBarColor, @ColorRes int navigationBarColor) {

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        decorView.post(() -> {
            WindowInsetsCompat windowInsetsCompat = ViewCompat.getRootWindowInsets(decorView);
            if (windowInsetsCompat == null) {
                return;
            }
            // Setup Status Bar
            int statusBarHeight = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            View statusBarView = new View(activity);
            FrameLayout.LayoutParams statusBarParams =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
            statusBarParams.gravity = Gravity.TOP;
            statusBarView.setLayoutParams(statusBarParams);
            statusBarParams.setMargins(0, -statusBarHeight,0,0);
            statusBarView.setBackgroundColor(ContextCompat.getColor(activity, statusBarColor));
            decorView.addView(statusBarView);

            // Setup Navigation Bar
            int navigationBarHeight = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            View navigationBarView = new View(activity);
            FrameLayout.LayoutParams navigationBarParams =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, navigationBarHeight);
            navigationBarParams.gravity = Gravity.BOTTOM;
            navigationBarView.setLayoutParams(navigationBarParams);
            navigationBarParams.setMargins(0, 0,0,-navigationBarHeight);
            navigationBarView.setBackgroundColor(ContextCompat.getColor(activity, navigationBarColor));
            decorView.addView(navigationBarView);

            decorView.setBackgroundColor(ContextCompat.getColor(activity, statusBarColor));

            decorView.setClipToPadding(false);
            decorView.setClipChildren(false);

            ViewCompat.dispatchApplyWindowInsets(decorView, windowInsetsCompat);

            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(activity.getWindow(), decorView);
            if (windowInsetsController == null){
                return;
            }
            windowInsetsController.setAppearanceLightStatusBars(true);
            windowInsetsController.setAppearanceLightNavigationBars(true);
        });
    }
}
