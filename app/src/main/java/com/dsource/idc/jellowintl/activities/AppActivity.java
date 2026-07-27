package com.dsource.idc.jellowintl.activities;

import android.os.Bundle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.dsource.idc.jellowintl.R;

public class AppActivity extends SpeechEngineBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        setupParent();
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            String destination = getIntent().getStringExtra("destination");
            if ("TutorialFragment".equals(destination)) {
                // Remove the start destination from back stack so back button exits or goes where it needs
                navController.popBackStack(R.id.splashFragment, true);
                navController.navigate(R.id.tutorialFragment);
            } else if ("LanguageDownloadFragment".equals(destination)) {
                Bundle args = new Bundle();
                args.putString("LCODE", getIntent().getStringExtra("LCODE"));
                args.putBoolean("CLOSE", getIntent().getBooleanExtra("CLOSE", false));
                navController.navigate(R.id.languageDownloadFragment, args);
            } else if (destination != null) {
                int destId = -1;
                switch (destination) {
                    case "ProfileFormFragment": destId = R.id.profileFormFragment; break;
                    case "AboutJellowFragment": destId = R.id.aboutJellowFragment; break;
                    case "LanguageSelectFragment": destId = R.id.languageSelectFragment; break;
                    case "SettingFragment": destId = R.id.settingFragment; break;
                    case "AccessibilitySettingsFragment": destId = R.id.accessibilitySettingsFragment; break;
                    case "ResetPreferencesFragment": destId = R.id.resetPreferencesFragment; break;
                    case "FeedbackFragment": destId = R.id.feedbackFragment; break;
                    case "FeedbackTalkBackFragment": destId = R.id.feedbackTalkBackFragment; break;
                }
                if (destId != -1) navController.navigate(destId);
            }
        }
    }
}
