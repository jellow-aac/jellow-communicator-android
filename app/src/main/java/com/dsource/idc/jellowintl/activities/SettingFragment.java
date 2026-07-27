package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.setCrashlyticsCustomKey;
import static com.dsource.idc.jellowintl.utility.Analytics.setUserProperty;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;
import static android.content.Context.ACCESSIBILITY_SERVICE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class SettingFragment extends Fragment {
    private final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 0;
    private Spinner mSpinnerViewMode, mSpinnerGridSize;
    private TextView mTxtViewSpeechSpeed, mTxtViewVoicePitch;
    private SeekBar mSliderSpeed, mSliderPitch, mSliderVolume;
    private boolean mOpenSetting, mEnabledBasicCustomAdditionSwitch, mEnableMonochromeDisplay;
    private String  mCalPerMsg, mCalPerGranted, mCalPerRejected, mSettings, mDismiss;
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
        return inflater.inflate(R.layout.activity_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(SettingFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.action_settings));
        getAppActivity().setupToolbarMenu();
        getAppActivity().setupBottomBar();
        getAppActivity().applyMonochromeColor();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getAppActivity().setSpeechPitch((float) getSession().getPitch()/50f);
                getAppActivity().setSpeechRate((float) getSession().getSpeed()/50f);
                setEnabled(false);
                requireActivity().onBackPressed();
            }
        });

        mOpenSetting = false;
        mSpinnerViewMode = view.findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                (requireContext(), R.array.picture_view_mode, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerViewMode.setAdapter(adapter);
        mSpinnerGridSize = view.findViewById(R.id.spinner4);
        if(!getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            adapter = ArrayAdapter.createFromResource
                    (requireContext(), R.array.grid_size, R.layout.simple_spinner_item);
        }else {
            adapter = ArrayAdapter.createFromResource
                    (requireContext(), R.array.acc_grid_size, R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGridSize.setAdapter(adapter);
        mEnabledBasicCustomAdditionSwitch= getSession().getBasicCustomIconAddState();
        mEnableMonochromeDisplay=getSession().getMonochromeDisplayState();

        if(getAppActivity().isDeviceReadyToCall((TelephonyManager)requireContext().getSystemService(Context.TELEPHONY_SERVICE))) {
            ((SwitchCompat) view.findViewById(R.id.switchEnableCall)).setChecked(getSession().isCallingEnabled());
            ((SwitchCompat) view.findViewById(R.id.switchEnableCall)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        requestCallPermissionToUser();
                    else
                        getSession().setEnableCalling(false);
                }
            });
        }else{
            view.findViewById(R.id.divider).setVisibility(View.GONE);
            view.findViewById(R.id.txtOtherSetting).setVisibility(View.GONE);
            view.findViewById(R.id.rowOtherSetting).setVisibility(View.GONE);
        }

        ((SwitchCompat) view.findViewById(R.id.switchDisplaySpeechText)).setChecked(getSession().getTextBarVisibility());
        ((SwitchCompat) view.findViewById(R.id.switchDisplaySpeechText)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSession().setTextBarVisibility(isChecked);
            }
        });

        ((SwitchCompat) view.findViewById(R.id.switchEnableAnimation)).setChecked(getSession().getAnimationState());
        ((SwitchCompat) view.findViewById(R.id.switchEnableAnimation)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSession().setAnimationState(isChecked);
            }
        });

        ((SwitchCompat) view.findViewById(R.id.switchEnableIconAddition)).setChecked(getSession().getBasicCustomIconAddState());
        ((SwitchCompat) view.findViewById(R.id.switchEnableIconAddition)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mEnabledBasicCustomAdditionSwitch=isChecked;
                    }
                }
        );

        ((SwitchCompat) view.findViewById(R.id.switchEnablemonochromeDisplay)).setChecked(getSession().getMonochromeDisplayState());
        ((SwitchCompat) view.findViewById(R.id.switchEnablemonochromeDisplay)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mEnableMonochromeDisplay=isChecked;
                    }
                }
        );

        Button btnSave = view.findViewById(R.id.button4);
        final Button btnDemo = view.findViewById(R.id.demo);
        mSliderSpeed = view.findViewById(R.id.speed);
        mSliderPitch = view.findViewById(R.id.pitch);
        mSliderVolume = view.findViewById(R.id.volume);
        mTxtViewSpeechSpeed = view.findViewById(R.id.speechspeed);
        mTxtViewVoicePitch = view.findViewById(R.id.voicepitch);

        mSliderSpeed.setProgress(getSession().getSpeed());
        mSliderPitch.setProgress(getSession().getPitch());
        mSpinnerViewMode.setSelection(getSession().getPictureViewMode());
        mSpinnerGridSize.setSelection(getSession().getGridSize());

        final String strSpeechSpeed = getString(R.string.txtSpeechSpeed);
        final String strDemoSpeech = getString(R.string.demoTtsSpeech);
        final String strSpeechPitch = getString(R.string.txtVoiceSpeech);
        final String strSettingSaved = getString(R.string.savedSettingsMessage);
        
        btnDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAppActivity().speak(strDemoSpeech);
                FirebaseCrashlytics.getInstance().log("SettingAct Demo");
            }
        });

       mSliderSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
               getAppActivity().setSpeechRate((float) i / 50f);
               mTxtViewSpeechSpeed.setText(strSpeechSpeed.concat(": "+ i / 5));
           }

           @Override public void onStartTrackingTouch(SeekBar seekBar) {}
           @Override public void onStopTrackingTouch(SeekBar seekBar) {}
       });

        mSliderPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getAppActivity().setSpeechPitch((float) i / 50f);
                mTxtViewVoicePitch.setText(strSpeechPitch.concat(": "+ i / 5));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        if(getAppActivity().isNoTTSLanguage()){
            mSliderSpeed.setVisibility(View.GONE);
            mTxtViewSpeechSpeed.setVisibility(View.GONE);
            mSliderPitch.setVisibility(View.GONE);
            mTxtViewVoicePitch.setVisibility(View.GONE);
        }

        final AudioManager audio = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        mSliderVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b && audio != null)
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, i,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean recreate = false;
                if(getSession().getPictureViewMode() != mSpinnerViewMode.getSelectedItemPosition() ||
                    getSession().getGridSize() != mSpinnerGridSize.getSelectedItemPosition() ||
                        getSession().getBasicCustomIconAddState() != mEnabledBasicCustomAdditionSwitch ||
                            getSession().getMonochromeDisplayState() != mEnableMonochromeDisplay) {

                    if(getSession().getPictureViewMode() != mSpinnerViewMode.getSelectedItemPosition()) {
                        setUserProperty("PictureViewMode",
                                mSpinnerViewMode.getSelectedItemPosition() == 0 ? "PictureText": "PictureOnly");
                        setCrashlyticsCustomKey("PictureViewMode",
                                mSpinnerViewMode.getSelectedItemPosition() == 0 ? "PictureText": "PictureOnly");
                        getSession().setPictureViewMode(mSpinnerViewMode.getSelectedItemPosition());
                    }
                    if(getSession().getGridSize() != mSpinnerGridSize.getSelectedItemPosition()) {
                        switch(mSpinnerGridSize.getSelectedItemPosition()){
                            case 0: setUserProperty("GridSize", "1"); break;
                            case 1: setUserProperty("GridSize", "2"); break;
                            case 2: setUserProperty("GridSize", "3"); break;
                            case 3: setUserProperty("GridSize", "4"); break;
                            case 4: setUserProperty("GridSize", "9"); break;
                        }
                        getSession().setGridSize(mSpinnerGridSize.getSelectedItemPosition());
                    }
                    if(getSession().getBasicCustomIconAddState() != mEnabledBasicCustomAdditionSwitch)
                        getSession().setBasicCustomIconAddState(mEnabledBasicCustomAdditionSwitch);

                    if(getSession().getMonochromeDisplayState() !=mEnableMonochromeDisplay)
                        getSession().setMonochromeDisplayState(mEnableMonochromeDisplay);

                    recreate = true;
                }
                if(getSession().getSpeed() != mSliderSpeed.getProgress()) {
                    getAppActivity().setSpeechRate((float)mSliderSpeed.getProgress()/50f);
                    getSession().setSpeed(mSliderSpeed.getProgress());
                }
                if(getSession().getPitch() != mSliderPitch.getProgress()) {
                    getAppActivity().setSpeechPitch((float)mSliderPitch.getProgress()/ 50f);
                    getSession().setPitch(mSliderPitch.getProgress());
                }
                getSession().setToastMessage(strSettingSaved);
                FirebaseCrashlytics.getInstance().log("SettingAct Save");
                
                if (recreate) {
                    requireActivity().recreate();
                } else {
                    requireActivity().onBackPressed();
                }
            }
        });

        mCalPerMsg = getString(R.string.grant_permission_from_settings);
        mCalPerGranted = getString(R.string.granted_call_permission_req);
        mCalPerRejected = getString(R.string.rejected_call_permission_req);
        mSettings = getString(R.string.action_settings);
        mDismiss = getString(R.string.dismiss);
        
        addAccessibilityDelegateToSpinners(view);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] Permissions, @NonNull int[] grantResults){
        if (requestCode == MY_PERMISSIONS_REQUEST_CALL_PHONE){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getSession().setEnableCalling(true);
                if (getView() != null) {
                    ((SwitchCompat) getView().findViewById(R.id.switchEnableCall)).setChecked(true);
                }
                Toast.makeText(requireContext(), mCalPerGranted , Toast.LENGTH_SHORT).show();
            } else {
                if(!ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), android.Manifest.permission.CALL_PHONE)){
                    showSettingRequestDialog();
                }else{
                    getSession().setEnableCalling(false);
                    Toast.makeText(requireContext(), mCalPerRejected, Toast.LENGTH_SHORT).show();
                }
                if (getView() != null) {
                    ((SwitchCompat) getView().findViewById(R.id.switchEnableCall)).setChecked(false);
                }
            }
        }
    }

    private void requestCallPermissionToUser() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            getSession().setEnableCalling(true);
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED) {
            getSession().setEnableCalling(true);
        } else {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CALL_PHONE)) {
                requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            } else {
                requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        }
    }

    private void showSettingRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder
            .setPositiveButton(mSettings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mOpenSetting = true;
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));
                    startActivityForResult(intent, 99);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(mDismiss, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            })
            .setCancelable(true)
            .setMessage(mCalPerMsg);

        AlertDialog dialog = builder.create();
        dialog.show();
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positive.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
        Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
        getAppActivity().applyMonochromeColor(positive);
        getAppActivity().applyMonochromeColor(negative);
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring("SettingsActivity");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            getSession().setEnableCalling(true);
            if (getView() != null) {
                ((SwitchCompat) getView().findViewById(R.id.switchEnableCall)).setChecked(true);
            }
            mOpenSetting = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()){
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();

        if(mOpenSetting && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
            && ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            getSession().setEnableCalling(true);
            if (getView() != null) {
                ((SwitchCompat) getView().findViewById(R.id.switchEnableCall)).setChecked(true);
            }
            mOpenSetting = false;
        }
        AudioManager audio = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        if(audio != null && getView() != null)
            ((SeekBar) getView().findViewById(R.id.volume)).setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
    }

    @Override
    public void onDestroy() {
        getAppActivity().stopSpeaking();
        super.onDestroy();
    }

    private void addAccessibilityDelegateToSpinners(View view) {
        if (getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            mSpinnerViewMode.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onInitializeAccessibilityEvent(host, event);
                    if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                        view.findViewById(R.id.tv4).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    }
                }
            });

            mSpinnerGridSize.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onInitializeAccessibilityEvent(host, event);
                    if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                        view.findViewById(R.id.demo).sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    }
                }
            });
        }
    }
}
