package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.SessionManager.BE_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_BD;
import static com.dsource.idc.jellowintl.utility.SessionManager.BN_IN;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class KeyboardInputFragment extends Fragment {
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
        return inflater.inflate(R.layout.activity_keyboard_input, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(KeyboardInputFragment.class.getSimpleName());
        getAppActivity().setupToolbarMenu();
        getAppActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.getKeyboardControl));
        getAppActivity().applyMonochromeColor();

        view.findViewById(R.id.abc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseCrashlytics.getInstance().log("KeyboardAct SerialABC");
                startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });

        view.findViewById(R.id.qwerty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseCrashlytics.getInstance().log("KeyboardAct Qwerty");
                startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
            }
        });

        view.findViewById(R.id.default_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseCrashlytics.getInstance().log("KeyboardAct Save");
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();
                requireActivity().onBackPressed();
            }
        });

        int boldTxtLen = 7;
        SpannableString spannedStr = new SpannableString(getString(R.string.step1));
        if(getSession().getLanguage().equals(BN_IN) ||
            getSession().getLanguage().equals(BE_IN) ||
                getSession().getLanguage().equals(BN_BD)) boldTxtLen = 11;
        spannedStr.setSpan(new StyleSpan(Typeface.BOLD),0, boldTxtLen,0);
        ((TextView)view.findViewById(R.id.t2)).setText(spannedStr);
        spannedStr = new SpannableString(getString(R.string.step2));
        if(getSession().getLanguage().equals(BN_IN) ||
            getSession().getLanguage().equals(BE_IN) ||
                getSession().getLanguage().equals(BN_BD)) boldTxtLen = 13;
        spannedStr.setSpan(new StyleSpan(Typeface.BOLD),0, boldTxtLen,0);
        ((TextView)view.findViewById(R.id.t3)).setText(spannedStr);
        
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
}
