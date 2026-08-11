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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.fragments.LanguageSelectFragment;
import com.dsource.idc.jellowintl.utility.Fish;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.utility.TextToSpeechErrorUtils;
import com.dsource.idc.jellowintl.utility.interfaces.TextToSpeechCallBacks;

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
        initiateSpeechEngineWithLanguage(getSession().getAppVoice().split(",")[0], getSession().getLanguage());
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
//            int fish = 0, dolphin = 1, whale = 2;
//            if (animationCounter % 25 == 0) {
//                showAnimation(whale);
//                animationCounter = 0;
//            } else if (animationCounter % 10 == 0)
//                showAnimation(dolphin);
//            else if (animationCounter % 5 == 0)
                showAnimation(2);
        }
    }

    private void showAnimation(int fishType) {
        final Fish fish;
        String tag = "small";
        View parentView = findViewById(R.id.parent);
        if (parentView != null && parentView.getTag() != null) {
            tag = parentView.getTag().toString().trim();
        } else {
            try {
                androidx.fragment.app.Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if (navHost != null) {
                    androidx.fragment.app.Fragment primary = navHost.getChildFragmentManager().getPrimaryNavigationFragment();
                    if (primary != null && primary.getView() != null && primary.getView().getTag() != null) {
                        tag = primary.getView().getTag().toString().trim();
                    }
                }
            } catch (Exception ignored) {}
        }

        switch(fishType){
            case 1: fish = Fish.Dolphin.get(tag); break;
            case 2: fish = Fish.Whale.get(tag); break;
            case 0:
            default: fish = Fish.JellowFish.get(tag); break;
        }

        try {
            final ImageView animView = findViewById(fish.animViewId);
            if (animView == null) return;
            
            // Set visible so Glide starts the request, then clear to reset state
            animView.setVisibility(View.VISIBLE);
            Glide.with(this).clear(animView);
            
            final MediaPlayer mp = new MediaPlayer();
            try {
                AssetFileDescriptor afd = getAssets().openFd(fish.animSound);
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mp.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Glide.with(this)
                    .asGif()
                    .load(fish.fishType)
                    .apply(new RequestOptions()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            runOnUiThread(() -> {
                                animView.setVisibility(View.GONE);
                                try { mp.release(); } catch (Exception ignored) {}
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(final GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            // Force the GIF to play exactly ONCE
                            resource.setLoopCount(1);
                            
                            // Use Native Callback to eliminate manual endTime delay
                            resource.registerAnimationCallback(new androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback() {
                                @Override
                                public void onAnimationEnd(android.graphics.drawable.Drawable drawable) {
                                    runOnUiThread(() -> {
                                        try {
                                            if (mp != null) mp.release();
                                        } catch (Exception ignored) {}
                                        animView.setVisibility(View.GONE);
                                        Glide.with(LevelBaseActivity.this).clear(animView);
                                    });
                                }
                            });
                            
                            resource.startFromFirstFrame();
                            
                            runOnUiThread(() -> {
                                int[] allViews = {R.id.animFish, R.id.animDolphin, R.id.animWhale};
                                for (int id : allViews) {
                                    if (id != fish.animViewId) {
                                        View v = findViewById(id);
                                        if (v != null) v.setVisibility(View.GONE);
                                    }
                                }
                            });

                            // Schedule splash sound relative to the actual animation start
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                try {
                                    mp.start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }, fish.soundTime);

                            return false;
                        }
                    })
                    .into(animView);
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
                                Intent intent = new Intent(LevelBaseActivity.this,
                                        AppActivity.class);
                                intent.putExtra("destination", LanguageSelectFragment.class.getSimpleName());
                                startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        })
                        .setNeutralButton(mSwitchLang, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getSession().setLanguage(SessionManager.ENG_US);
                                startActivity(new Intent(LevelBaseActivity.this,
                                        AppActivity.class));
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
