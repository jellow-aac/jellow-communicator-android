package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.bundleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.setCrashlyticsCustomKey;
import static com.dsource.idc.jellowintl.utility.Analytics.setUserProperty;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.HI_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.LangMap;
import static com.dsource.idc.jellowintl.utility.SessionManager.LangValueMap;
import static com.dsource.idc.jellowintl.utility.SessionManager.MR_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.TA_IN;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.cache.CacheManager;
import com.dsource.idc.jellowintl.factories.LanguageFactory;
import com.dsource.idc.jellowintl.factories.TextFactory;
import com.dsource.idc.jellowintl.package_updater_module.UpdatePackageCheckUtils;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Arrays;

public class LanguageSelectFragment extends Fragment {
    private static final String LCODE = "LCODE";
    private static final String CLOSE = "CLOSE";
    private String selectedLanguage, mLangChanged, availVoices, selectedVoice;
    private Button save, languageSelect, voiceSelect;
    private String mStep2, mStep3;
    String[] langList = new String[LangValueMap.size()];
    String[] voiceList;
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
        return inflater.inflate(R.layout.activity_language_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(LanguageSelectFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.Language));
        getAppActivity().setupToolbarMenu();
        getAppActivity().applyMonochromeColor();
        LanguageFactory.deleteOldLanguagePackagesInBackground(requireContext());
        new UpdatePackageCheckUtils().checkLanguagePackageUpdateAvailable(requireContext());
        mStep2 = getString(R.string.change_language_tts_wifi);
        mStep3 = getString(R.string.change_language_line5);
        mLangChanged = getString(R.string.languageChanged);

        languageSelect = view.findViewById(R.id.btn_lang_select);
        languageSelect.setOnClickListener(v -> showAvailableLanguageDialog());
        
        {
            ArrayList<String> rawLangList = new ArrayList<>();
            rawLangList.addAll(Arrays.asList(LanguageFactory.getAvailableLanguages()));
            rawLangList.remove(LangValueMap.get(getSession().getLanguage()));
            rawLangList.add(0, LangValueMap.get(getSession().getLanguage()));
            for (int i = 0; i < LangValueMap.size(); i++) {
                langList[i] = rawLangList.get(i);
            }
        }
        voiceSelect = view.findViewById(R.id.btn_voic_select);
        voiceSelect.setOnClickListener(v -> showVoiceSelectDialog());
        
        {
            availVoices = getAppActivity().getAvailableVoicesForLanguage(getSession().getLanguage());
            voiceList = new String[availVoices.split(",").length];
            for (int i = 0; i < availVoices.split(",").length; i++) {
                voiceList[i] = "Voice "+ getAppActivity().getRomanNumber(i+1)+getAppActivity().getGender(availVoices.split(",")[i]);
            }
        }

        languageSelect.setText(langList[0]);
        selectedLanguage = langList[0];
        if(getSession().getAppVoice().split(",").length != 2) {
            voiceSelect.setText(voiceList[0]);
            selectedVoice = availVoices.split(",")[0];
        }else {
            selectedVoice = getSession().getAppVoice().split(",")[0];
            voiceSelect.setText(getSession().getAppVoice().split(",")[1]);
        }

        setImageUsingGlide(R.drawable.tts_wifi_1, view.findViewById(R.id.ivAddLang1));
        setImageUsingGlide(R.drawable.tts_wifi_2, view.findViewById(R.id.ivAddLang2));
        setImageUsingGlide(R.drawable.tts_wifi_3, view.findViewById(R.id.ivAddLang3));
        setImageUsingGlide(R.drawable.gtts3, view.findViewById(R.id.ivTtsVoiceDat));
        setImageUsingGlide(R.drawable.arrow, view.findViewById(R.id.ivArrow1));
        setImageUsingGlide(R.drawable.arrow, view.findViewById(R.id.ivArrow2));

        view.findViewById(R.id.btnTTsSetting).setOnClickListener(v -> openSpeechSetting());
        view.findViewById(R.id.btnDownloadVoiceData).setOnClickListener(v -> openSpeechDataSetting());

        save = view.findViewById(R.id.saveBut);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseCrashlytics.getInstance().log("LanguageSelect Apply");
                if(selectedLanguage.equals(LangValueMap.get(MR_IN)) && !LanguageFactory.isMarathiPackageAvailable
                        (requireContext())){
                    
                    Bundle bundle = new Bundle();
                    bundle.putString(LCODE, MR_IN);
                    bundle.putBoolean(CLOSE, true);
                    Navigation.findNavController(v).navigate(R.id.languageDownloadFragment, bundle);
                } else {
                    saveLanguage();
                }
            }
        });
        updateViewsForNewLangSelect(view);
        
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring("ChangeLanguageActivity");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()){
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();

        if(!getSession().getToastMessage().isEmpty()) {
            Toast.makeText(requireContext(), getSession().getToastMessage(), Toast.LENGTH_SHORT).show();
            getSession().setToastMessage("");
        }
    }

    public void showAvailableLanguageDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setSingleChoiceItems(langList, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedLanguage = langList[which];
                languageSelect.setText(selectedLanguage);
                availVoices = getAppActivity().getAvailableVoicesForLanguage(LangMap.get(selectedLanguage));
                voiceList = new String[availVoices.split(",").length];
                for (int i = 0; i < availVoices.split(",").length; i++) {
                    voiceList[i] = "Voice "+getAppActivity().getRomanNumber(i+1)+getAppActivity().getGender(availVoices.split(",")[i]);
                }
                selectedVoice = availVoices.split(",")[0];
                voiceSelect.setText(voiceList[0]);
                updateViewsForNewLangSelect(getView());
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.getListView().setScrollbarFadingEnabled(false);
        dialog.getListView().setScrollBarSize(12);
        getAppActivity().applyMonochromeColor(dialog.getListView());
    }

    public void showVoiceSelectDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        int selectPos = 0;
        {
            String selectVoice = voiceSelect.getText().toString().trim();
            for (int i=0; i< voiceList.length;i++){
                if(voiceList[i].contains(selectVoice)) {
                    selectPos = i;
                    break;
                }
            }
        }
        builder.setSingleChoiceItems(voiceList, selectPos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedVoice = availVoices.split(",")[which];
                voiceSelect.setText(voiceList[which]);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        dialog.getListView().setScrollbarFadingEnabled(false);
        dialog.getListView().setScrollBarSize(12);
        getAppActivity().applyMonochromeColor(dialog.getListView());
    }

    public void openSpeechDataSetting(){
        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void openSpeechSetting(){
        startActivity(new Intent().setAction("com.android.settings.TTS_SETTINGS"));
    }

    private void saveLanguage() {
        if(!LanguageFactory.checkIfVerbiageAvailableForTheLanguage(requireContext(), LangMap.get(selectedLanguage))){
            Toast.makeText(requireContext(), getString(R.string.update_the_package), Toast.LENGTH_SHORT).show();
            return;
        }
        getSession().setLanguage(LangMap.get(selectedLanguage));
        getSession().setAppVoice(selectedVoice
                +","+voiceSelect.getText().toString().trim());
        Bundle bundle = new Bundle();
        bundle.putString("LanguageSet", "Switched to "+ LangMap.get(selectedLanguage));
        bundleEvent("Language",bundle);
        setUserProperty("UserLanguage", LangMap.get(selectedLanguage));
        setCrashlyticsCustomKey("UserLanguage",  LangMap.get(selectedLanguage));
        Toast.makeText(requireContext(), mLangChanged, Toast.LENGTH_SHORT).show();
        CacheManager.clearCache();
        TextFactory.clearJson();
        requireActivity().recreate();
    }

    private String getTTsLanguage() {
        String language = selectedLanguage;
        if(language.equals(LangValueMap.get(HI_IN)))
            return  "Hindi (India)";
        else if(language.equals(LangValueMap.get(BN_IN)))
            return "Bengali (India)";
        else if(language.equals(LangValueMap.get(TA_IN)))
            return "English (India)";
        return selectedLanguage;
    }

    private int getDelimitedStringLength(String text){
        return getSession().getLanguage().equals(BN_IN) ?
                text.indexOf("ঃ")+1 : text.indexOf(":")+1;
    }

    private void updateViewsForNewLangSelect(View view) {
        if (selectedLanguage.equals(LangValueMap.get(MR_IN))) {
            view.findViewById(R.id.ll_hidden_view).setVisibility(View.GONE);
            view.findViewById(R.id.tv_language_not_working_info).setVisibility(View.GONE);
            view.findViewById(R.id.tv_voic_step_info).setVisibility(View.GONE);
            view.findViewById(R.id.btn_voic_select).setVisibility(View.GONE);
            view.findViewById(R.id.more_setting_title).setVisibility(View.GONE);
            return;
        }
        view.findViewById(R.id.ll_hidden_view).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_language_not_working_info).setVisibility(View.VISIBLE);
        view.findViewById(R.id.tv_voic_step_info).setVisibility(View.VISIBLE);
        view.findViewById(R.id.btn_voic_select).setVisibility(View.VISIBLE);
        view.findViewById(R.id.more_setting_title).setVisibility(View.VISIBLE);

        /*step 2*/
        SpannableString spannedStr = new SpannableString(mStep2);
        spannedStr.setSpan(new StyleSpan(Typeface.BOLD),0, getDelimitedStringLength(mStep2),0);
        spannedStr.setSpan(new UnderlineSpan(), 0, getDelimitedStringLength(mStep2), 0);
        ((TextView)view.findViewById(R.id.tv_step2_info)).setText(spannedStr);

        /*step 3*/
        spannedStr = new SpannableString(mStep3.replace("_", getTTsLanguage()));
        spannedStr.setSpan(new StyleSpan(Typeface.BOLD),0, getDelimitedStringLength(mStep3),0);
        spannedStr.setSpan(new UnderlineSpan(), 0, getDelimitedStringLength(mStep3), 0);
        int start = spannedStr.toString().indexOf(getTTsLanguage()),
                end = start + getTTsLanguage().length();
        spannedStr.setSpan(new StyleSpan(Typeface.BOLD), start, end,0);
        ((TextView)view.findViewById(R.id.tv_step3_info)).setText(spannedStr);
    }

    private void setImageUsingGlide(int image, ImageView imgView) {
        Glide.with(this)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .dontAnimate()
                .into(imgView);
    }
}
