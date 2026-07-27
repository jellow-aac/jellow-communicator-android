package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.LanguageHelper;

public class TutorialFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_tutorial, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getBaseActivity().setVisibleAct("TutorialActivity");
        getBaseActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuTutorials));
        
        // Find the back button in the custom app bar
        // View backBtn = view.findViewById(R.id.ivBack);
        // if (backBtn != null) {
        //    backBtn.setOnClickListener(v -> requireActivity().onBackPressed());
        // }
        
        // Hide settings button since this is a tutorial
        // View settingsBtn = view.findViewById(R.id.ivSettings);
        // if (settingsBtn != null) {
        //    settingsBtn.setVisibility(View.GONE);
        // }

        getBaseActivity().applyMonochromeColor();
        setImagesToImageViewUsingGlide(view);
    }

    private void setImagesToImageViewUsingGlide(View view) {
        setImageUsingGlide(getResources().getDrawable(R.drawable.categorybuttons), view.findViewById(R.id.pic1));
        setImageUsingGlide(getResources().getDrawable(R.drawable.expressivebuttons), view.findViewById(R.id.pic2));
        setImageUsingGlide(getResources().getDrawable(R.drawable.speakingwithjellowimage2), view.findViewById(R.id.pic4));
        setImageUsingGlide(getResources().getDrawable(R.drawable.eatingcategory1), view.findViewById(R.id.pic5));
        setImageUsingGlide(getResources().getDrawable(R.drawable.eatingcategory2), view.findViewById(R.id.pic6));
        setImageUsingGlide(getResources().getDrawable(R.drawable.eatingcategory3), view.findViewById(R.id.pic7));
        setImageUsingGlide(getResources().getDrawable(R.drawable.settings), view.findViewById(R.id.pic8));
        setImageUsingGlide(getResources().getDrawable(R.drawable.sequencewithoutexpressivebuttons), view.findViewById(R.id.pic9));
        setImageUsingGlide(getResources().getDrawable(R.drawable.sequencewithexpressivebuttons), view.findViewById(R.id.pic10));
        setImageUsingGlide(getResources().getDrawable(R.drawable.gtts1), view.findViewById(R.id.gtts1));
        setImageUsingGlide(getResources().getDrawable(R.drawable.gtts2), view.findViewById(R.id.gtts2));
        setImageUsingGlide(getResources().getDrawable(R.drawable.gtts3), view.findViewById(R.id.gtts3));

        setImageUsingGlide(getResources().getDrawable(R.drawable.my_boards), view.findViewById(R.id.pic11));
        setImageUsingGlide(getResources().getDrawable(R.drawable.my_boards_edit), view.findViewById(R.id.pic12));
        setImageUsingGlide(getResources().getDrawable(R.drawable.add_boards), view.findViewById(R.id.pic13));
        setImageUsingGlide(getResources().getDrawable(R.drawable.add_icons), view.findViewById(R.id.pic14));
        setImageUsingGlide(getResources().getDrawable(R.drawable.add_edit_icon), view.findViewById(R.id.pic15));
        setImageUsingGlide(getResources().getDrawable(R.drawable.edit_icon), view.findViewById(R.id.pic16));
        setImageUsingGlide(getResources().getDrawable(R.drawable.edit_verbiage), view.findViewById(R.id.pic17));
        setImageUsingGlide(getResources().getDrawable(R.drawable.board_home), view.findViewById(R.id.pic18));
    }

    private void setImageUsingGlide(Drawable image, ImageView imgView) {
        if (imgView == null) return;
        Glide.with(this)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .dontAnimate()
                .into(imgView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()){
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);

        stopMeasuring("TutorialActivity");
    }
}
