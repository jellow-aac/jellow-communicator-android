package com.dsource.idc.jellowintl.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.utility.interfaces.TextToSpeechCallBacks;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import static com.dsource.idc.jellowintl.factories.PathFactory.UNIVERSAL_FOLDER;
import static com.dsource.idc.jellowintl.factories.PathFactory.getAudioPath;
import static com.dsource.idc.jellowintl.utility.SessionManager.BE_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_BD;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.DE_DE;
import static com.dsource.idc.jellowintl.utility.SessionManager.ENG_AU;
import static com.dsource.idc.jellowintl.utility.SessionManager.ENG_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.ENG_NG;
import static com.dsource.idc.jellowintl.utility.SessionManager.ENG_UK;
import static com.dsource.idc.jellowintl.utility.SessionManager.ENG_US;
import static com.dsource.idc.jellowintl.utility.SessionManager.ES_ES;
import static com.dsource.idc.jellowintl.utility.SessionManager.FR_FR;
import static com.dsource.idc.jellowintl.utility.SessionManager.GU_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.HI_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.KHA_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.KN_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.MAI_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.ML_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.MR_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.MR_TTS;
import static com.dsource.idc.jellowintl.utility.SessionManager.OR_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.PA_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.SR_RS;
import static com.dsource.idc.jellowintl.utility.SessionManager.TA_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.TE_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.UR_IN;

public class SpeechEngineBaseActivity extends BaseActivity{
    private static TextToSpeech sTts;
    private static int mFailedToSynthesizeTextCount = 0;

    private HashMap<String, String> map;
    private static TextToSpeechCallBacks mErrorHandler;

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, getPackageName());
    }

    private void setupSpeechEngine(final String voice, String language) {
        sTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                try {
                    if(status == TextToSpeech.ERROR || (sTts != null &&
                            !sTts.getEngines().toString().contains(getTTsEngineNameForLanguage("")))){
                        mErrorHandler.speechEngineNotFoundError();
                        return;
                    }
                    if(sTts == null)
                        return;

                    sTts.setVoice(getVoiceObject(voice));
                    sTts.setSpeechRate(getTTsSpeedForLanguage(language));
                    sTts.setPitch(getTTsPitchForLanguage(language));
                    if (voice.endsWith(MR_IN))
                        createUserProfileRecordingsUsingTTS();
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }, getTTsEngineNameForLanguage(language));

        sTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}

            @Override public void onDone(String utteranceId) {
                mErrorHandler.speechSynthesisCompleted();
            }

            @Override
            public void onError(String utteranceId) {
                /***
                 * Text synthesize process failed two times and voice data not available for
                 * user selected language then
                 * send error callback to user to correct language setting for selected language
                 * from Language menu.
                 ***/
                if(++mFailedToSynthesizeTextCount > 1 &&
                        !isVoiceAvailableForLanguage(getSession().getLanguage())) {
                    mErrorHandler.sendSpeechEngineLanguageNotSetCorrectlyError();
                }
            }
        });
    }

    private float getTTsPitchForLanguage(String language) {
        switch(language){
            case ENG_UK:
            case ENG_US:
            case ENG_AU:
            case ENG_NG:
            case HI_IN:
            case ENG_IN:
            case BN_IN:
            case BE_IN:
            case MR_IN:
            case MR_TTS:
            case ES_ES:
            case TA_IN:
            case DE_DE:
            case FR_FR:
            case BN_BD:
            case TE_IN:
            case GU_IN:
            case PA_IN:
            case KHA_IN:
            case KN_IN:
            case ML_IN:
            case OR_IN:
            case UR_IN:
            case MAI_IN:
            case SR_RS:
            default:
                return (float) getSession().getPitch()/50;
        }
    }

    private float getTTsSpeedForLanguage(String language){
        switch(language){
            case ENG_UK:
            case ENG_US:
            case ENG_AU:
            case ENG_NG:
            case HI_IN:
            case ENG_IN:
            case BN_IN:
            case BE_IN:
            case MR_IN:
            case MR_TTS:
            case ES_ES:
            case TA_IN:
            case DE_DE:
            case FR_FR:
            case BN_BD:
            case TE_IN:
            case GU_IN:
            case PA_IN:
            case KHA_IN:
            case KN_IN:
            case ML_IN:
            case OR_IN:
            case UR_IN:
            case MAI_IN:
            case SR_RS:
            default:
                return (float) (getSession().getSpeed()/50);
        }
    }

    private String getTTsEngineNameForLanguage(String language) {
        switch(language){
            case PA_IN:
            case ENG_UK:
            case ENG_US:
            case ENG_AU:
            case ENG_NG:
            case HI_IN:
            case ENG_IN:
            case BN_IN:
            case BE_IN:
            case MR_IN:
            case MR_TTS:
            case ES_ES:
            case TA_IN:
            case DE_DE:
            case FR_FR:
            case BN_BD:
            case TE_IN:
            case GU_IN:
            case KHA_IN:
            case KN_IN:
            case ML_IN:
            case OR_IN:
            case UR_IN:
            case MAI_IN:
            case SR_RS:
            default:
                return "com.google.android.tts";
        }
    }


    public void registerSpeechEngineErrorHandle(TextToSpeechCallBacks handler){
        mErrorHandler = handler;
    }

    private Voice getVoiceObject(String voice){
        for (Voice v : sTts.getVoices()) {
            if (v.getName().equals(voice)){
                return v;
            }
        }
        return null;
    }

    public static String getAvailableVoicesForLanguage(String lang){
        if(lang.equals(KHA_IN))
            lang=BN_IN;
        lang = lang.replace("-r","-").toLowerCase();
        StringBuilder csvVoices = new StringBuilder();
        for (String voice : voiceGender.keySet()) {
            if(voice.startsWith(lang)){
                csvVoices.append(voice).append(",");
            }
        }
        return csvVoices.toString();
    }

    public void speak(String speechText){
        stopSpeaking();
        /*Extra symbol '_' is appended to end of every string from custom keyboard utterances.
         *Extra symbol '-' is appended to end of every string from make my board speak request.
         * Hence utterances will use tts engine to speak irrespective of type of language
         * (tts language or non tts) */
        if (speechText.contains("_") || speechText.contains("-") || !isNoTTSLanguage())
            sTts.speak(speechText.replace("_","").replace("-",""), TextToSpeech.QUEUE_FLUSH, map);
        else
            playAudio(getAudioPath(this)+speechText);
    }

    public void speakWithDelay(final String speechText){
        final int interval = 1000; // 1 Second
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                stopSpeaking();
                /*Extra symbol '_' is appended to end of every string from custom keyboard utterances.
                 *Extra symbol '-' is appended to end of every string from make my board speak request.
                 * Hence utterances will use tts engine to speak irrespective of type of language
                 * (tts language or non tts) */
                if (speechText.contains("_") || speechText.contains("-") || !isNoTTSLanguage())
                    sTts.speak(speechText.replace("_","").
                            replace("-",""), TextToSpeech.QUEUE_FLUSH, map);
                else
                    playAudio(getAudioPath(SpeechEngineBaseActivity.this)+speechText);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);
    }

    public void speakFromMMB(String speechText){
        stopSpeaking();
        sTts.speak(speechText.replace("_","").replace("-",""), TextToSpeech.QUEUE_FLUSH, map);
    }

    public void stopSpeaking(){
        sTts.stop();
        stopAudio();
    }

    public boolean isEngineSpeaking(){
        return sTts.isSpeaking();
    }

    public void setSpeechRate(float rate){
        sTts.setSpeechRate(rate);
    }

    public void setSpeechPitch(float pitch){
        sTts.setPitch(pitch);
    }

    public boolean isVoiceAvailableForLanguage(String language) {
        Locale locale = new Locale(language.split("-r")[0], language.split("-r")[1]);
        for (Voice voice : sTts.getVoices())
            if(voice.getLocale().equals(locale) && !voice.getFeatures().contains("notInstalled"))
                return true;
        return  false;
    }

    public void createUserProfileRecordingsUsingTTS() {
        final String path = getDir(UNIVERSAL_FOLDER, Context.MODE_PRIVATE).getAbsolutePath() + "/audio/";
        sTts.setLanguage(new Locale("hi", "IN"));
        try {
            String emailId = getSession().getEmailId().
                    replaceAll(".", "$0 ").replace(".", "dot");
            String contactNo = getSession().getCaregiverNumber().
                    replaceAll(".", "$0 ").replace("+", "plus");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                File name = new File(path+ "name.mp3");
                Log.i("File : ", String.valueOf(name.createNewFile()));
                File email = new File(path+ "email.mp3");
                Log.i("File : ", String.valueOf(email.createNewFile()));
                File contact = new File(path+ "contact.mp3");
                Log.i("File : ", String.valueOf(contact.createNewFile()));
                File caregiverName = new File(path+ "caregiverName.mp3");
                Log.i("File : ", String.valueOf(caregiverName.createNewFile()));
                File address = new File(path+ "address.mp3");
                Log.i("File : ", String.valueOf(address.createNewFile()));
                File bloodGroup = new File(path+ "bloodGroup.mp3");
                Log.i("File : ", String.valueOf(bloodGroup.createNewFile()));
                sTts.synthesizeToFile(getSession().getName(), null, name, getPackageName());
                sTts.synthesizeToFile(emailId, null, email, getPackageName());
                sTts.synthesizeToFile(contactNo, null, contact, getPackageName());
                sTts.synthesizeToFile(getSession().getCaregiverName(), null, caregiverName, getPackageName());
                sTts.synthesizeToFile(getSession().getAddress(), null, address, getPackageName());
                sTts.synthesizeToFile(getBloodGroup(getSession().getBlood()), null,
                        bloodGroup, getPackageName());
            }else {
                sTts.synthesizeToFile(getSession().getName(), null, path + "name.mp3");
                sTts.synthesizeToFile(emailId, null, path + "email.mp3");
                sTts.synthesizeToFile(contactNo, null, path + "contact.mp3");
                sTts.synthesizeToFile(getSession().getCaregiverName(), null, path + "caregiverName.mp3");
                sTts.synthesizeToFile(getSession().getAddress(), null, path + "address.mp3");
                sTts.synthesizeToFile(getBloodGroup(getSession().getBlood()), null,
                        path + "bloodGroup.mp3");
            }
            sTts.setVoice(getVoiceObject(getSession().getAppVoice().split(",")[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void playAudio(String audioPath) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(audioPath);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopMediaAudio(){
        try {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playInQueue(final String speechTextInQueue) {
        stopAudio();
        try {
            final int[] count = {0};
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(speechTextInQueue.split(",")[0]);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (count[0] < 2){
                        mMediaPlayer.release();
                        mMediaPlayer = null;mMediaPlayer = new MediaPlayer();
                        try {
                            mMediaPlayer.setDataSource(speechTextInQueue.split(",")[1]);
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mMediaPlayer.start();
                        count[0]++;
                    }
                }
            });
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            count[0]++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopAudio() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void speakInQueue(String speechData){
        speechData = getAudioPath(this)+ speechData;
        if (speechData.contains("GGL")){
            speechData = speechData+","+ getAudioPath(this)+ "name.mp3";
        }else if (speechData.contains("GGY")){
            speechData = speechData+","+ getAudioPath(this)+ "email.mp3";
        }else if (speechData.contains("GGM")){
            speechData = speechData+","+  getAudioPath(this)+ "contact.mp3";
        }else if (speechData.contains("GGD")){
            speechData = speechData+","+  getAudioPath(this)+ "caregiverName.mp3";
        }else if (speechData.contains("GGN")){
            speechData = speechData+","+  getAudioPath(this)+ "address.mp3";
        }else {
            speechData = speechData+","+  getAudioPath(this)+ "bloodGroup.mp3";
        }
        playInQueue(speechData);
    }

    public boolean isNoTTSLanguage(){
        return SessionManager.NoTTSLang.contains(getSession().getLanguage());
    }

    public void initiateSpeechEngineWithLanguage(String voice, String language){
        if(voice == null || voice.isEmpty()){
           voice = getAvailableVoicesForLanguage(getSession().getLanguage()).split(",")[0];
        }
        setupSpeechEngine(voice, language);
    }

    static HashMap<String, String> voiceGender = new HashMap<String, String>(){
        {
            put("bn-bd-x-ban-local", " (M)");
//            put("bn-in-x-bin-local", " (M)");
//            put("bn-in-x-bnf-local", " (F)");
            put("bn-in-x-bnd-local", " (M)");
            put("bn-in-x-bnc-local", " (F)");
            put("de-de-x-deb-local", " (M)");
            put("de-de-x-deg-local", " (M)");
            put("de-de-x-nfh-local", " (F)");
            put("de-de-x-dea-local", " (F)");
            put("en-au-x-afh-local", " (F)");
            put("en-au-x-aua-local", " (F)");
            put("en-au-x-aub-local", " (M)");
            put("en-au-x-auc-local", " (F)");
            put("en-au-x-aud-local", " (M)");
            put("en-gb-x-fis-local", " (F)");
            put("en-gb-x-gba-local", " (F)");
            put("en-gb-x-gbb-local", " (M)");
            put("en-gb-x-gbc-local", " (F)");
            put("en-gb-x-gbd-local", " (M)");
            put("en-gb-x-gbg-local", " (F)");
            put("en-gb-x-rjs-local", " (M)");
            put("en-in-x-ahp-local", " (F)");
            put("en-in-x-cxx-local", " (F)");
            put("en-in-x-ena-local", " (F)");
            put("en-in-x-enc-local", " (F)");
            put("en-in-x-end-local", " (M)");
            put("en-in-x-ene-local", " (M)");
            put("en-ng-x-tfn-local", " (F)");
            put("en-us-x-iob-local", " (F)");
            put("en-us-x-iog-local", " (F)");
            put("en-us-x-iol-local", " (M)");
            put("en-us-x-iom-local", " (M)");
            put("en-us-x-sfg-local", " (F)");
            put("en-us-x-tpc-local", " (F)");
            put("en-us-x-tpd-local", " (M)");
            put("en-us-x-tpf-local", " (F)");
            put("es-es-x-eea-local", " (F)");
            put("es-es-x-eec-local", " (F)");
            put("es-es-x-eed-local", " (M)");
            put("es-es-x-eee-local", " (F)");
            put("es-es-x-eef-local", " (M)");
            put("fr-fr-x-fra-local", " (F)");
            put("fr-fr-x-frb-local", " (M)");
            put("fr-fr-x-frc-local", " (F)");
            put("fr-fr-x-frd-local", " (M)");
            put("fr-fr-x-vlf-local", " (F)");
            put("gu-in-x-guf-local", " (F)");
            put("gu-in-x-gum-local", " (M)");
            put("hi-in-x-hia-local", " (F)");
            put("hi-in-x-hic-local", " (F)");
            put("hi-in-x-hid-local", " (M)");
            put("hi-in-x-hie-local", " (M)");
            put("mr-in-x-mrf-local", " (F)");
            put("ta-in-x-taf-local", " (F)");
            put("ta-in-x-tag-local", " (M)");
            put("te-in-x-tef-local", " (F)");
            put("te-in-x-tem-local", " (M)");
            put("pa-in-x-pac-local", " (F)");
            put("pa-in-x-pad-local", " (M)");
            put("pa-in-x-pae-local", " (F)");
            put("pa-in-x-pag-local", " (M)");
            put("kn-in-x-knf-local", " (F)");
            put("kn-in-x-knm-local", " (M)");
            put("ml-in-x-mlf-local", " (F)");
            put("ml-in-x-mlm-local", " (M)");
            put("or-in-x-end-local", " (M)");
            put("ur-in-x-urb-local", " (M)");
            put("ur-in-x-urc-local", " (F)");
            put("sr-rs-x-standard-local", " (M)");
            put("sr-rs-x-wavenet-local", " (F)");
            put("mai-in-x-end-local", " (M)");
        }
    };
}