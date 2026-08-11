package com.dsource.idc.jellowintl.fragments;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.setCrashlyticsCustomKey;
import static com.dsource.idc.jellowintl.utility.Analytics.setUserProperty;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.ACCESSIBILITY_SERVICE;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.AppActivity;
import com.dsource.idc.jellowintl.cache.CacheManager;
import com.dsource.idc.jellowintl.factories.TextFactory;
import com.dsource.idc.jellowintl.make_my_board_module.dataproviders.databases.TextDatabase;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.utility.AppUpdateUtil;
import com.dsource.idc.jellowintl.utility.AppUpdateUtil.AppUpdateCallback;
import com.dsource.idc.jellowintl.utility.async.CreateDataBase;
import com.dsource.idc.jellowintl.utility.async.InternetTest;
import com.dsource.idc.jellowintl.utility.interfaces.CheckNetworkStatus;
import com.dsource.idc.jellowintl.utility.interfaces.CompletionCallback;

public class SplashFragment extends BaseFragment implements CheckNetworkStatus, CompletionCallback, AppUpdateCallback {
    private InternetTest internetTest;
    private CreateDataBase createDataBase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Hide toolbar logic
        getBaseActivity().setupActionBarTitle(View.GONE, "");
        if (view.findViewById(R.id.toolbar) != null) {
            view.findViewById(R.id.toolbar).setVisibility(View.GONE);
        }
        getBaseActivity().applyMonochromeColor();
        
        ImageView pGif = view.findViewById(R.id.viewGif);
        if (pGif != null) {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.jellow_j)
                    .apply(new RequestOptions()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .fitCenter()
                    .into(pGif);
        }

        if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
            (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED))
            getSession().setEnableCalling(false);

        TextDatabase textDb = new TextDatabase(requireContext(), getSession().getLanguage(), getAppDatabase());
        if(getSession().getLanguageDataUpdateState(getSession().getLanguage()) ==
                GlobalConstants.LANGUAGE_STATE_CREATE_DB || !textDb.checkForTableExists()) {
            CacheManager.clearCache();
            TextFactory.clearJson();
            createDataBase = new CreateDataBase();
            createDataBase.registerReceiver(this);
            createDataBase.execute(requireContext(), getAppDatabase());
        }else {
            internetTest = new InternetTest();
            internetTest.registerReceiver(this);
            internetTest.execute(requireContext());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        
        // Note: Edge-to-edge logic is now handled automatically by AppActivity.
        // We no longer need to manually set SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
        // or setNavigationBarColor here.

        setUserParameters();
        new AppUpdateUtil().executeUpdateFlow(AppUpdateUtil.UpdateStatus.RUNNING, getBaseActivity(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==AppUpdateUtil.UPDATE_REQUEST_CODE && resultCode == RESULT_OK){
            restartTheAppAfterUpdate();
        }else if(requestCode == AppUpdateUtil.UPDATE_REQUEST_CODE){
            continueLoadingTheApp();
        }
    }

    @Override
    public void onReceiveNetworkState(int state) {
        if (internetTest != null) {
            internetTest.unRegisterReceiver();
        }
        if(state == GlobalConstants.NETWORK_CONNECTED){
            AppUpdateUtil updateUtil = new AppUpdateUtil();
            updateUtil.executeUpdateFlow(AppUpdateUtil.UpdateStatus.INIT, getBaseActivity(), this);
        }else{
            continueLoadingTheApp();
        }
    }

    @Override
    public void onTaskComplete(int status) {
        if (createDataBase != null) {
            createDataBase.unRegisterReceiver();
        }
        if(status == GlobalConstants.STATUS_SUCCESS)
            getSession().setLanguageDataUpdateState(getSession().getLanguage(),
                    GlobalConstants.LANGUAGE_STATE_NO_CHANGE);
        else
            getSession().setLanguageDataUpdateState(getSession().getLanguage(),
                    GlobalConstants.LANGUAGE_STATE_CREATE_DB);
        continueLoadingTheApp();
    }

    private void setUserParameters() {
        if(getSession().isGridSizeKeyExist()) {
            // setGridSize() implementation was part of BaseActivity? 
            // Wait, SplashActivity called setGridSize() but it wasn't defined in SplashActivity.
            // Let's call it via getBaseActivity().
            getBaseActivity().setGridSize();
        }else{
            setUserProperty("GridSize", "9");
            setCrashlyticsCustomKey("GridSize", "9");
        }

        if(getSession().getPictureViewMode() == GlobalConstants.DISPLAY_PICTURE_TEXT) {
            setUserProperty("PictureViewMode", "PictureText");
            setCrashlyticsCustomKey("PictureViewMode", "PictureText");
        }else{
            setUserProperty("PictureViewMode", "PictureOnly");
            setCrashlyticsCustomKey("PictureViewMode", "PictureOnly");
        }

        if (getBaseActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))){
            setUserProperty("VisualAccessibility", "true");
        }else{
            setUserProperty("VisualAccessibility", "false");
        }
    }

    @Override
    public void continueLoadingTheApp() {
        if (isAdded()) {
            try {
                androidx.navigation.fragment.NavHostFragment.findNavController(SplashFragment.this)
                        .navigate(R.id.action_splashFragment_to_mainFragment);
            } catch (Exception e) {
                try {
                    androidx.navigation.fragment.NavHostFragment.findNavController(SplashFragment.this)
                            .navigate(R.id.mainFragment);
                } catch (Exception ignored) {}
            }
        }
    }

    private void restartTheAppAfterUpdate() {
        Intent mStartActivity = new Intent(requireContext(), AppActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(requireContext(), mPendingIntentId,
                mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager mgr = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        }
        System.exit(0);
    }
}
