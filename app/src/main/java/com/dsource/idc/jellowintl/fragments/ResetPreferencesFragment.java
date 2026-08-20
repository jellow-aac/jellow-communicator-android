package com.dsource.idc.jellowintl.fragments;

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
import androidx.navigation.fragment.NavHostFragment;

import com.dsource.idc.jellowintl.Presentor.PreferencesHelper;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.AppActivity;
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
        getAppActivity().setupActionBarTitle(view, View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuResetPref));
        getAppActivity().setupToolbarMenu(view);
        getAppActivity().setupBottomBar();
        getAppActivity().applyMonochromeColor();

        view.findViewById(R.id.no).setOnClickListener(v -> NavHostFragment.findNavController(ResetPreferencesFragment.this).popBackStack());

        final String strIconsResetMsg = getString(R.string.iconsHasBeenReset);
        view.findViewById(R.id.yes).setOnClickListener(v -> {
            Toast.makeText(requireContext(), strIconsResetMsg, Toast.LENGTH_SHORT).show();
            PreferencesHelper.clearPreferences(getAppActivity().getAppDatabase());
            Intent intent = new Intent(requireContext(), AppActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            FirebaseCrashlytics.getInstance().log("ResetPref Yes");
        });
        
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!NavHostFragment.findNavController(ResetPreferencesFragment.this).popBackStack()) {
                    NavHostFragment.findNavController(ResetPreferencesFragment.this).navigate(R.id.levelOneFragment);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getAppActivity().setVisibleAct(ResetPreferencesFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(getView(), View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuResetPref));
        getAppActivity().setupToolbarMenu(getView());
        if(!isAnalyticsActive()) {
            resetAnalytics(requireContext(), getSession().getUserId());
        }
    }
}
