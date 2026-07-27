package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.SessionManager;

public class AccessibilitySettingsFragment extends Fragment {

    private SessionManager session;

    private SessionManager getSession() {
        if (session == null) session = new SessionManager(requireContext());
        return session;
    }

    private AppActivity getAppActivity() {
        return (AppActivity) requireActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_accessibility_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(AccessibilitySettingsFragment.class.getSimpleName());
        getAppActivity().setupToolbarMenu();
        getAppActivity().setupBottomBar();
        getAppActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuAccessibility));
        getAppActivity().applyMonochromeColor();

        View btnSystemAccessibility = view.findViewById(R.id.btn_system_accessibility);
        if (btnSystemAccessibility != null) {
            btnSystemAccessibility.setOnClickListener(v -> openSystemAccessibilitySetting());
        }
        
        View btnAccessVideoOne = view.findViewById(R.id.btn_access_video_one);
        if (btnAccessVideoOne != null) {
            btnAccessVideoOne.setOnClickListener(v -> playInfoVideo(btnAccessVideoOne));
        }

        View btnAccessVideoTwo = view.findViewById(R.id.btn_access_video_two);
        if (btnAccessVideoTwo != null) {
            btnAccessVideoTwo.setOnClickListener(v -> playInfoVideo(btnAccessVideoTwo));
        }
        
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring("AccessibilitySettingActivity");
    }

    public void openSystemAccessibilitySetting(){
        try {
            startActivity(new Intent().setAction("android.settings.ACCESSIBILITY_SETTINGS"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void playInfoVideo(View view){
        String videoLink = view.equals(getView().findViewById(R.id.btn_access_video_one)) ?
                "http://www.youtube.com/watch?v=QDU1Qp-u2Zs" : "http://www.youtube.com/watch?v=OJiOC0Wkvlk";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink)));
    }
}
