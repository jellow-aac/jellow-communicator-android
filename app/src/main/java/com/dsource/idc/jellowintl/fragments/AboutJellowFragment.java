package com.dsource.idc.jellowintl.fragments;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.HI_IN;
import static android.content.Context.ACCESSIBILITY_SERVICE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.dsource.idc.jellowintl.BuildConfig;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.AppActivity;
import com.dsource.idc.jellowintl.factories.PathFactory;
import com.dsource.idc.jellowintl.utility.SessionManager;

import java.util.HashMap;

public class AboutJellowFragment extends Fragment {
    private Button mBtnSpeak, mBtnStop;
    private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10, tv11, tv12, tv13, tv14, tv15, tv16,
            tv35;
    private String mSpeechTxt, mGenInfo, mSoftInfo, mTermsOfUse,
            mIntro1, mIntro2, mIntro3, mIntro4, mIntro5, mIntro6, mIntro7, mIntro8,
            mIntro9, mIntro10, mIntro11, mIntro12, mIntro13, mIntro14, mSpeak, mStop;
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
        return inflater.inflate(R.layout.activity_about_jellow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(AboutJellowFragment.class.getSimpleName());
        getAppActivity().setupBottomBar();
        getAppActivity().setupActionBarTitle(view, View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuAbout));
        getAppActivity().setupToolbarMenu(view);
        getAppActivity().applyMonochromeColor();
        initializeViews(view);
        loadStrings();
        setTextToTextViews();
        
        mBtnSpeak.setOnClickListener(v -> {
            if (getSession().getLanguage().equals(SessionManager.MR_IN)){
                String mediaPath = PathFactory.getAudioPath(requireContext())
                        + getString(R.string.about_jellow_speech);
                getAppActivity().playAudio(mediaPath);
            }else {
                getAppActivity().speak(mSpeechTxt);
            }
        });

        mBtnStop.setOnClickListener(v -> {
            getAppActivity().stopSpeaking();
            getAppActivity().stopAudio();
            getAppActivity().stopMediaAudio();
        });
        
        Button playVideoBtn = view.findViewById(R.id.btn_play_video);
        if (playVideoBtn != null) {
            playVideoBtn.setOnClickListener(v -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=5LXDcPBYCyA")));
            });
        }
        
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getAppActivity().stopSpeaking();
                if (!NavHostFragment.findNavController(AboutJellowFragment.this).popBackStack()) {
                    NavHostFragment.findNavController(AboutJellowFragment.this).navigate(R.id.levelOneFragment);
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        getAppActivity().stopSpeaking();
        getAppActivity().stopAudio();
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppActivity().setVisibleAct(AboutJellowFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(getView(), View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuAbout));
        getAppActivity().setupToolbarMenu(getView());
        if(!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getAppActivity().stopSpeaking();
    }

    private void initializeViews(View view) {
        tv1= view.findViewById(R.id.tv1);
        tv2= view.findViewById(R.id.tv2);
        tv3= view.findViewById(R.id.tv3);
        tv4= view.findViewById(R.id.tv4);
        tv5= view.findViewById(R.id.tv5);
        tv6= view.findViewById(R.id.tv6);
        tv7= view.findViewById(R.id.tv7);
        tv8= view.findViewById(R.id.tv8);
        tv9= view.findViewById(R.id.tv9);
        tv10= view.findViewById(R.id.tv10);
        tv11= view.findViewById(R.id.tv11);
        tv12= view.findViewById(R.id.tv12);
        tv13= view.findViewById(R.id.tv13);
        tv14= view.findViewById(R.id.tv14);
        tv15= view.findViewById(R.id.tv15);
        tv16= view.findViewById(R.id.tv16);
        tv35= view.findViewById(R.id.tv35);
        mBtnSpeak = view.findViewById(R.id.speak);
        mBtnStop = view.findViewById(R.id.stop);

        if(getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            view.findViewById(R.id.llBottom).setVisibility(View.GONE);
        }
    }

    private void loadStrings() {
        String versionCode = prepareRegionalVersionCode(getSession().getLanguage(),
                BuildConfig.VERSION_NAME.
                        replace(".","@").split("@"));
        mGenInfo = getString(R.string.info);
        mIntro1 = getString(R.string.about_je_intro1);
        mIntro2 = getString(R.string.about_je_intro2);
        mIntro3 = getString(R.string.about_je_intro3);
        mIntro4 = getString(R.string.about_je_intro4);
        mIntro5 = getString(R.string.about_je_intro5);
        mIntro6 = getString(R.string.about_je_intro6);
        mIntro7 = getString(R.string.about_je_intro7);
        mSoftInfo = getString(R.string.software_info);
        mIntro8 = getString(R.string.about_je_intro8);
        mSoftInfo = mSoftInfo.replace("_", versionCode);
        mIntro8 = mIntro8.contains("_") ? mIntro8.replace("_", versionCode) : mIntro8;
        mTermsOfUse = getString(R.string.terms_of_use);
        mIntro9 = getString(R.string.about_je_intro9);
        mIntro10 = getString(R.string.about_je_intro10);
        mIntro11 = getString(R.string.about_je_intro11);
        mIntro12 = getString(R.string.about_je_intro12);
        mIntro13 = getString(R.string.about_je_intro13).replace("_", getString(R.string.websiteLink));
        mIntro14 = getString(R.string.about_je_intro14);
        mSpeak = getString(R.string.speak);
        mStop = getString(R.string.stop);

        mSpeechTxt = getString(R.string.about_jellow_speech);
        if(getSession().getLanguage().equals(HI_IN)) {
            versionCode = versionCode.replace(".", " दशम् लक ");
        }
        mSpeechTxt =  mSpeechTxt.replace("_", versionCode);
    }

    private String prepareRegionalVersionCode(String language, String[] versionStArr) {
        StringBuilder newVsnStr = new StringBuilder();
        if (getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            newVsnStr.append(versionStArr[0]);
            newVsnStr.append(" dot ");
            newVsnStr.append(versionStArr[1]);
            newVsnStr.append(" dot ");
            newVsnStr.append(versionStArr[2]);
        }else{
            switch (language) {
                case HI_IN:
                    HashMap<String, String> eng2hindi = new HashMap<String, String>() {
                        {
                            put("0", "०");
                            put("1", "१");
                            put("2", "२");
                            put("3", "३");
                            put("4", "४");
                            put("5", "५");
                            put("6", "६");
                            put("7", "७");
                            put("8", "८");
                            put("9", "९");
                        }
                    };
                    newVsnStr.append(eng2hindi.get(versionStArr[0]));
                    newVsnStr.append(".");
                    newVsnStr.append(eng2hindi.get(versionStArr[1]));
                    newVsnStr.append(".");
                    newVsnStr.append(eng2hindi.get(versionStArr[2]));
                    break;
                case BN_IN:
                    HashMap<String, String> eng2bangla = new HashMap<String, String>() {
                        {
                            put("0", "০");
                            put("1", "১");
                            put("2", "২");
                            put("3", "৩");
                            put("4", "৪");
                            put("5", "৫");
                            put("6", "৬");
                            put("7", "৭");
                            put("8", "৮");
                            put("9", "৯");
                        }
                    };
                    newVsnStr.append(eng2bangla.get(versionStArr[0]));
                    newVsnStr.append(".");
                    newVsnStr.append(eng2bangla.get(versionStArr[1]));
                    newVsnStr.append(".");
                    newVsnStr.append(eng2bangla.get(versionStArr[2]));
                    break;
                default:
                    return BuildConfig.VERSION_NAME;
            }
        }
        return newVsnStr.toString();
    }

    private void setTextToTextViews() {
        tv1.setText(mGenInfo);
        tv2.setText(mIntro1);
        tv3.setText(mIntro2);
        tv4.setText(mIntro3);
        tv5.setText(mIntro4);
        tv6.setText(mIntro5);
        tv7.setText(mIntro7);
        tv8.setText(mSoftInfo);
        tv9.setText(mIntro8);
        tv10.setText(mTermsOfUse);
        tv11.setText(mIntro9);
        tv12.setText(mIntro10);
        tv13.setText(mIntro11);
        tv14.setText(mIntro12);
        tv15.setText(mIntro13);
        tv16.setText(mIntro14);
        tv35.setText(mIntro6);
        mBtnSpeak.setText(mSpeak);
        mBtnStop.setText(mStop);
        Linkify.addLinks(tv15, Linkify.WEB_URLS);
        Linkify.addLinks(tv16, Linkify.EMAIL_ADDRESSES);
        Linkify.addLinks(tv9, Linkify.EMAIL_ADDRESSES);
    }
}
