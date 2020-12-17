package com.dsource.idc.jellowintl.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;

import com.dsource.idc.jellowintl.BuildConfig;
import com.dsource.idc.jellowintl.R;

import java.util.HashMap;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.HI_IN;

/**
 * Created by user on 5/27/2016.
 */

public class AboutJellowActivity extends SpeechEngineBaseActivity {
    private Button mBtnSpeak, mBtnStop;
    private TextView tv1, tv2, tv3, tv4, tv5, tv6, tv7, tv8, tv9, tv10, tv11, tv12, tv13, tv14, tv15, tv16,
            tv35;
    private String mSpeechTxt, mGenInfo, mSoftInfo, mTermsOfUse,
            mIntro1, mIntro2, mIntro3, mIntro4, mIntro5, mIntro6, mIntro7, mIntro8,
            mIntro9, mIntro10, mIntro11, mIntro12, mIntro13, mIntro14, mSpeak, mStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_jellow);
        enableNavigationBack();
        setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuAbout));
        setNavigationUiConditionally();
        initializeViews();
        loadStrings();
        setTextToTextViews();
        mBtnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (getSession().getLanguage().equals(SessionManager.MR_IN)){
                    String mediaPath = PathFactory.getAudioPath(AboutJellowActivity.this)
                            + getString(R.string.about_jellow_speech);
                    playAudio(mediaPath);
                }else {*/
                    speak(mSpeechTxt);
                //}
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSpeaking();
                stopAudio();
                stopMediaAudio();
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopSpeaking();
        stopAudio();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVisibleAct(AboutJellowActivity.class.getSimpleName());
        if(!isAnalyticsActive()) {
            resetAnalytics(this, getSession().getUserId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSpeaking();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopSpeaking();
        finish();
    }

    private void initializeViews() {
        tv1= findViewById(R.id.tv1);
        tv2= findViewById(R.id.tv2);
        tv3= findViewById(R.id.tv3);
        tv4= findViewById(R.id.tv4);
        tv5= findViewById(R.id.tv5);
        tv6= findViewById(R.id.tv6);
        tv7= findViewById(R.id.tv7);
        tv8= findViewById(R.id.tv8);
        tv9= findViewById(R.id.tv9);
        tv10= findViewById(R.id.tv10);
        tv11= findViewById(R.id.tv11);
        tv12= findViewById(R.id.tv12);
        tv13= findViewById(R.id.tv13);
        tv14= findViewById(R.id.tv14);
        tv15= findViewById(R.id.tv15);
        tv16= findViewById(R.id.tv16);
        tv35= findViewById(R.id.tv35);
        mBtnSpeak = findViewById(R.id.speak);
        mBtnStop = findViewById(R.id.stop);

        if(isAccessibilityTalkBackOn((AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))) {
            findViewById(R.id.bottomControls).setVisibility(View.GONE);
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
        if (isAccessibilityTalkBackOn((AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))) {
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

    public void playInfoVideo(View view){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=5LXDcPBYCyA")));
    }
}
