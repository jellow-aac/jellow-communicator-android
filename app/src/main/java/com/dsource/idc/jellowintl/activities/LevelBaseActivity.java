package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_SEVEN_INCH_TAB;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.Fish;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.utility.TextToSpeechErrorUtils;
import com.dsource.idc.jellowintl.utility.interfaces.TextToSpeechCallBacks;

import java.util.Timer;
import java.util.TimerTask;

import pl.droidsonroids.gif.GifDrawable;

public class LevelBaseActivity extends SpeechEngineBaseActivity implements TextToSpeechCallBacks{
    private String mErrorMessage, mDialogTitle, mLanguageSetting, mSwitchLang;
    private Toast toast;
    private CountDownTimer timer;

    /*animation variable*/
    private static int animationCounter=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerSpeechEngineErrorHandle(this);


        mErrorMessage = getString(R.string.langauge_correction_message);
        String lang = SessionManager.LangValueMap.get(getSession().getLanguage()) != null ?
                SessionManager.LangValueMap.get(getSession().getLanguage()): "";
        mErrorMessage = mErrorMessage.replace("-", lang);
        mErrorMessage = mErrorMessage.replace("_", getString(R.string.Language));
        mErrorMessage = mErrorMessage.replace("$", getString(R.string.dialog_default_language_option));
        mDialogTitle = getString(R.string.changeLanguage);
        mLanguageSetting = getString(R.string.Language);
        mSwitchLang = getString(R.string.dialog_default_language_option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initiateSpeechEngineWithLanguage(getSession().getAppVoice().split(",")[0]);
    }

    public void animateIfEnabled(){
        /*If animation is enabled and
          user in not adding custom icons (or custom icon addition at level screen is disabled) and
          monochrome display is disable (monochrome display slows down the animation.) then
          show the animation.
        */
        if (getSession().getAnimationState() &&
                !getSession().getBasicCustomIconAddState() &&
                   !getSession().getMonochromeDisplayState()
        ) {
            animationCounter++;
            int fish = 0, dolphin = 1, whale = 2;
            if (animationCounter % 25 == 0) {
                showAnimation(whale);
                animationCounter = 0;
            } else if (animationCounter % 10 == 0)
                showAnimation(dolphin);
            else if (animationCounter % 5 == 0)
                showAnimation(fish);
        }
    }

    private void showAnimation(int fishType) {
        final Fish fish;
        switch(fishType){
            case 1:
                fish = Fish.getDolphin(findViewById(R.id.parent).getTag().toString().trim());
                break;
            case 2:
                fish = Fish.getWhale(findViewById(R.id.parent).getTag().toString().trim());
                break;
            case 0:
            default:
                fish = Fish.getFish(findViewById(R.id.parent).getTag().toString().trim());
        }

        try {
            final GifDrawable gifFromResource = new GifDrawable(getResources(), fish.getFishType());
            gifFromResource.setSpeed(.5f);
            findViewById(fish.getView()[0]).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(fish.getView()[0])).setImageDrawable(gifFromResource);

            findViewById(fish.getView()[1]).setVisibility(View.GONE);
            findViewById(fish.getView()[2]).setVisibility(View.GONE);
            final MediaPlayer mp = new MediaPlayer();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        AssetFileDescriptor afd = getAssets().openFd(fish.getAnimSound());
                        mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        mp.prepare();
                        mp.start();
                        this.cancel();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mp.release();
                                    findViewById(fish.getView()[0]).setVisibility(View.GONE);
                                    gifFromResource.stop();
                                }
                            });
                            this.cancel();
                        }
                    }, fish.getEndTime());
                }

                @Override
                public long scheduledExecutionTime() {
                    return super.scheduledExecutionTime();
                }
            }, fish.getSoundTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void speakAndShowTextBar_(String text){
        speak(text);
        final String txt = text
                .replace(",", "")
                .replace("plus", "+");
        if(getSession().getTextBarVisibility()){
            if(toast!=null) toast.cancel();
            if(timer!= null) timer.cancel();
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        View layout = getLayoutInflater().inflate(R.layout.toast_layout, null);
                        TextView textView = layout.findViewById(R.id.text);
                        textView.setText(txt);
                        toast = new Toast(getApplicationContext());
                        toast.setDuration(Toast.LENGTH_SHORT);
                        int yOff;
                        switch (getScreenSize()){
                            case SCREEN_SIZE_SEVEN_INCH_TAB:
                            case SCREEN_SIZE_TEN_INCH_TAB:
                                yOff  = 62;
                                break;
                            default:
                                yOff  = 2;
                                break;
                        }
                        toast.setGravity(Gravity.BOTTOM, 0, yOff);
                        toast.setView(layout);

                        timer = new CountDownTimer(5000, 75) {
                            @Override
                            public void onFinish() {
                                toast.cancel();
                            }

                            @Override
                            public void onTick(long millisUntilFinished) {
                                toast.show();
                            }
                        };
                        timer.start();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**Text-To-Speech Engine error callbacks implementations are following**/
    @Override
    public void sendSpeechEngineLanguageNotSetCorrectlyError() {
        LevelBaseActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LevelBaseActivity.this);
                builder.setMessage(mErrorMessage)
                        .setTitle(mDialogTitle)
                        .setPositiveButton(mLanguageSetting, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(LevelBaseActivity.this,
                                        LanguageSelectActivity.class));
                                dialogInterface.dismiss();
                            }
                        })
                        .setNeutralButton(mSwitchLang, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getSession().setLanguage(SessionManager.ENG_US);
                                startActivity(new Intent(LevelBaseActivity.this,
                                        SplashActivity.class));
                                finishAffinity();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
                Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positive.setTextColor(LevelBaseActivity.this.getResources().getColor(R.color.colorAccent));
                Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                negative.setTextColor(LevelBaseActivity.this.getResources().getColor(R.color.colorAccent));
                Button neutral = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                neutral.setTextColor(LevelBaseActivity.this.getResources().getColor(R.color.colorAccent));
                applyMonochromeColor(positive);
                applyMonochromeColor(negative);
                applyMonochromeColor(neutral);
            }
        });
    }

    @Override
    public void speechEngineNotFoundError() {
        LevelBaseActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new TextToSpeechErrorUtils(LevelBaseActivity.this).showErrorDialog();
            }
        });
    }

    @Override
    public void speechSynthesisCompleted() {}
    /*-------------*/
}
