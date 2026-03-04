package com.dsource.idc.jellowintl.activities;

/**
 * Created by Shruti on 15-08-2016.
 */

import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_PHONE;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_SEVEN_INCH_TAB;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.models.GlobalConstants;
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
        View view = inflater.inflate(layoutResId, container, false);
        setupActionBarTitle(view, View.GONE, getString(R.string.intro_to_jellow));
        return view;
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//    }

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
        } else {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            view.findViewById(R.id.dummyStatusBar).setVisibility(View.GONE);
            // Setting up toolbar height for 10' & 7' device
            if (getScreenSize() == GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB ||
                    getScreenSize() == SCREEN_SIZE_SEVEN_INCH_TAB) {
                MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
                if (toolbar == null)
                    return;

                int height = 62;
                int heightInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        height,
                        displayMetrics
                );

                ViewGroup.LayoutParams toolbarParams = toolbar.getLayoutParams();
                toolbarParams.height = heightInPx;
                toolbar.setLayoutParams(toolbarParams);
            }
            if (getScreenSize() == SCREEN_SIZE_PHONE){
                LinearLayout toolbar = view.findViewById(R.id.parent);
                if (toolbar != null) {
                    int height = 32;
                    int heightInPx = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            height,
                            displayMetrics
                    );
                    ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                    param.setMargins(param.leftMargin, heightInPx, param.rightMargin, param.bottomMargin);
                    toolbar.setLayoutParams(param);
                }
            }
        }
    }
    public int getScreenSize(){
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth > 720) {
            //Device is a 10" tablet
            return SCREEN_SIZE_TEN_INCH_TAB;
        }else if (smallestWidth > 600) {
            //Device is a 7" tablet
            return SCREEN_SIZE_SEVEN_INCH_TAB;
        }else
            return SCREEN_SIZE_PHONE;
    }
}