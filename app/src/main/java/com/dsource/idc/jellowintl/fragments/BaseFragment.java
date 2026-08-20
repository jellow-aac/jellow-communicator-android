package com.dsource.idc.jellowintl.fragments;

import android.telephony.TelephonyManager;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.dsource.idc.jellowintl.activities.LevelBaseActivity;
import com.dsource.idc.jellowintl.activities.SpeechEngineBaseActivity;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.models.AppDatabase;

public class BaseFragment extends Fragment {
    
    public BaseActivity getBaseActivity() {
        return (BaseActivity) requireActivity();
    }

    public LevelBaseActivity getLevelActivity() {
        return (LevelBaseActivity) requireActivity();
    }
    
    public SessionManager getSession() {
        return getBaseActivity().getSession();
    }
    
    public AppDatabase getAppDatabase() {
        return getBaseActivity().getAppDatabase();
    }

    public void speakAndShowTextBar_(String text) {
        getLevelActivity().speakAndShowTextBar_(text);
    }

    public void speakWithDelay(String text) {
        getLevelActivity().speakWithDelay(text);
    }

    public void speakInQueue(String text) {
        getLevelActivity().speakInQueue(text);
    }

    public void stopSpeaking() {
        getLevelActivity().stopSpeaking();
    }

    public boolean isNoTTSLanguage() {
        return getLevelActivity().isNoTTSLanguage();
    }

    public boolean isAccessibilityTalkBackOn(AccessibilityManager am) {
        return getLevelActivity().isAccessibilityTalkBackOn(am);
    }

    public boolean isDeviceReadyToCall(TelephonyManager tm) {
        return getLevelActivity().isDeviceReadyToCall(tm);
    }

    public void animateIfEnabled() {
        getLevelActivity().animateIfEnabled();
    }

    public void setupActionBarTitle(int visibility, String title) {
        getLevelActivity().setupActionBarTitle(getView(), visibility, title);
    }

    public void setupActionBarTitle(String title) {
        getLevelActivity().setupActionBarTitle(getView(), title);
    }

    public void setupActionBarTitle(View view, int visibility, String title) {
        getLevelActivity().setupActionBarTitle(view, visibility, title);
    }

    public void setupActionBarTitle(View view, String title) {
        getLevelActivity().setupActionBarTitle(view, title);
    }

    public void setupToolbarMenu() {
        getLevelActivity().setupToolbarMenu(getView());
    }

    public void setupToolbarMenu(View view) {
        getLevelActivity().setupToolbarMenu(view);
    }

    public void setupParent() {
        getLevelActivity().setupParent();
    }

    public void applyMonochromeColor() {
        getLevelActivity().applyMonochromeColor();
    }

    public void applyMonochromeColor(View view) {
        getLevelActivity().applyMonochromeColor(view);
    }

    public void setVisibleAct(String name) {
        getLevelActivity().setVisibleAct(name);
    }

    public String getVisibleAct() {
        return getLevelActivity().getVisibleAct();
    }

    public SpeechEngineBaseActivity getSpeechEngineActivity() {
        return (SpeechEngineBaseActivity) requireActivity();
    }

    public void speak(String speechText) {
        getSpeechEngineActivity().speak(speechText);
    }

    public void speakFromMMB(String speechText) {
        getSpeechEngineActivity().speakFromMMB(speechText);
    }

    public void registerSpeechEngineErrorHandle(com.dsource.idc.jellowintl.utility.interfaces.TextToSpeechCallBacks cb) {
        getSpeechEngineActivity().registerSpeechEngineErrorHandle(cb);
    }

    public void initiateSpeechEngineWithLanguage(String voice, String language) {
        getSpeechEngineActivity().initiateSpeechEngineWithLanguage(voice, language);
    }

    public void showGridDialog(com.dsource.idc.jellowintl.make_my_board_module.interfaces.GridSelectListener listener, int size) {
        getBaseActivity().showGridDialog(listener, size);
    }

    public android.view.Menu getMenu() {
        if (getView() != null) {
            com.google.android.material.appbar.MaterialToolbar toolbar = getView().findViewById(com.dsource.idc.jellowintl.R.id.toolbar);
            if (toolbar != null) {
                return toolbar.getMenu();
            }
        }
        return null;
    }

    public boolean isNotchDevice() {
        return getBaseActivity().isNotchDevice();
    }
}
