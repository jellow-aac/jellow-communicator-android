package com.dsource.idc.jellowintl.activities;

/**
 * Created by Shruti on 15-08-2016.
 */

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

public class SampleSlideFragment extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private static final String ARG_LAYOUT_NAME = "layoutName";
    private String mLayoutName;

    public static SampleSlideFragment newInstance(int layoutResId, String layoutName) {
        SampleSlideFragment sampleSlide = new SampleSlideFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        args.putString(ARG_LAYOUT_NAME, layoutName);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    private int layoutResId;

    public SampleSlideFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID) && getArguments().containsKey(ARG_LAYOUT_NAME)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
            mLayoutName = getArguments().getString(ARG_LAYOUT_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutResId, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupActionBarTitle(view, View.GONE, getString(R.string.intro_to_jellow));
    }

    public String getLayoutName() {
        return mLayoutName;
    }

    public void setupActionBarTitle(View view, int isBackVisible, String title){
        view.findViewById(R.id.iv_action_bar_back).setVisibility(isBackVisible);
        if (title.contains("("))
            ((TextView)view.findViewById(R.id.tvActionbarTitle)).setText(title.substring(0,title.indexOf("(")));
        else
            ((TextView)view.findViewById(R.id.tvActionbarTitle)).setText(title);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            AppBarLayout appBarLayout = view.findViewById(R.id.app_bar);
            if (appBarLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    // Apply the insets as a margin to the view. This solution sets only the
                    // bottom, left, and right dimensions, but you can apply whichever insets are
                    // appropriate to your layout. You can also update the view padding if that's
                    // more appropriate.
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    mlp.leftMargin = insets.left;
                    mlp.bottomMargin = insets.bottom;
                    mlp.rightMargin = insets.right;
                    mlp.topMargin = 0;
                    v.setLayoutParams(mlp);
                    // Return CONSUMED if you don't want the window insets to keep passing
                    // down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
                appBarLayout.setPadding(32,0,0,0);
            }
            MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
            if (toolbar != null) {
                ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                    int right = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout()).left;
                    v.post(() -> {
                        v.setPadding(right, 0, 0, 0);
                        v.requestLayout();
                    });
                    // Return CONSUMED if you don't want the window insets to keep passing
                    // down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
            }

//            if (layoutResId == R.layout.intro1){
//
//            }
        } else {
            view.findViewById(R.id.dummyStatusBar).setVisibility(View.GONE);
        }
    }
}