package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.Presentor.PreferencesHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class ResetPreferencesFragment extends Fragment {

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
        return inflater.inflate(R.layout.activity_reset_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(ResetPreferencesFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuResetPref));
        getAppActivity().setupToolbarMenu();
        getAppActivity().setupBottomBar();
        getAppActivity().applyMonochromeColor();

        view.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        final String strIconsResetMsg = getString(R.string.iconsHasBeenReset);
        view.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireContext(), strIconsResetMsg, Toast.LENGTH_SHORT).show();
                PreferencesHelper.clearPreferences(getAppActivity().getAppDatabase());
                startActivity(new Intent(requireContext(), AppActivity.class));
                FirebaseCrashlytics.getInstance().log("ResetPref Yes");
                requireActivity().finishAffinity();
            }
        });
        
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
