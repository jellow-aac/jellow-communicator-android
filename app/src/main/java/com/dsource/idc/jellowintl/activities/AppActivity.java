package com.dsource.idc.jellowintl.activities;

import android.os.Bundle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.fragments.AboutJellowFragment;
import com.dsource.idc.jellowintl.fragments.AccessibilitySettingsFragment;
import com.dsource.idc.jellowintl.fragments.FeedbackFragment;
import com.dsource.idc.jellowintl.fragments.FeedbackTalkBackFragment;
import com.dsource.idc.jellowintl.fragments.IntroFragment;
import com.dsource.idc.jellowintl.fragments.LanguageDownloadFragment;
import com.dsource.idc.jellowintl.fragments.LanguageSelectFragment;
import com.dsource.idc.jellowintl.fragments.MainFragment;
import com.dsource.idc.jellowintl.fragments.ProfileFormFragment;
import com.dsource.idc.jellowintl.fragments.ResetPreferencesFragment;
import com.dsource.idc.jellowintl.fragments.SettingFragment;
import com.dsource.idc.jellowintl.fragments.SplashFragment;
import com.dsource.idc.jellowintl.fragments.TutorialFragment;
import com.dsource.idc.jellowintl.fragments.UserRegistrationFragment;

public class AppActivity extends LevelBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        setupParent();
        
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            androidx.navigation.NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.nav_graph);
            
            String destination = getIntent().getStringExtra("destination");
            if (destination != null) {
                int destId = getDestId(destination);

                if (destination.equals(LanguageDownloadFragment.class.getSimpleName())) {
                    if (destId != -1) navGraph.setStartDestination(destId);
                    Bundle args = new Bundle();
                    args.putString("LCODE", getIntent().getStringExtra("LCODE"));
                    args.putBoolean("CLOSE", getIntent().getBooleanExtra("CLOSE", false));
                    args.putBoolean("TUTORIAL", getIntent().getBooleanExtra("TUTORIAL", false));
                    args.putBoolean("SPLASH", getIntent().getBooleanExtra("SPLASH", true));
                    navController.setGraph(navGraph, args);
                } else if (destination.equals(UserRegistrationFragment.class.getSimpleName()) ||
                        destination.equals(SplashFragment.class.getSimpleName()) ||
                        destination.equals(IntroFragment.class.getSimpleName())) {
                    if (destId != -1) navGraph.setStartDestination(destId);
                    navController.setGraph(navGraph);
                } else {
                    navGraph.setStartDestination(R.id.mainFragment);
                    navController.setGraph(navGraph);
                    if (destId != -1 && destId != R.id.mainFragment) {
                        Bundle args = new Bundle();
                        if (getIntent().getExtras() != null) {
                            args.putAll(getIntent().getExtras());
                        }
                        navController.navigate(destId, args);
                    }
                }
            } else {
                // Default start destination (UserRegistrationFragment)
                navController.setGraph(navGraph);
            }
        }
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra("destination")) {
            String destination = intent.getStringExtra("destination");
            int destId = getDestId(destination);
            if (destId != -1) {
                Bundle args = new Bundle();
                if (intent.getExtras() != null) {
                    args.putAll(intent.getExtras());
                }
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
                if (navHostFragment != null) {
                    navHostFragment.getNavController().navigate(destId, args);
                }
            }
        }
    }

    private static int getDestId(String destination) {
        int destId = -1;
        if (destination.equals(TutorialFragment.class.getSimpleName())) {
            destId = R.id.tutorialFragment;
        } else if (destination.equals(LanguageDownloadFragment.class.getSimpleName())) {
            destId = R.id.languageDownloadFragment;
        } else if (destination.equals(ProfileFormFragment.class.getSimpleName())) {
            destId = R.id.profileFormFragment;
        } else if (destination.equals(AboutJellowFragment.class.getSimpleName())) {
            destId = R.id.aboutJellowFragment;
        } else if (destination.equals(LanguageSelectFragment.class.getSimpleName())) {
            destId = R.id.languageSelectFragment;
        } else if (destination.equals(SettingFragment.class.getSimpleName())) {
            destId = R.id.settingFragment;
        } else if (destination.equals(AccessibilitySettingsFragment.class.getSimpleName())) {
            destId = R.id.accessibilitySettingsFragment;
        } else if (destination.equals(ResetPreferencesFragment.class.getSimpleName())) {
            destId = R.id.resetPreferencesFragment;
        } else if (destination.equals(FeedbackFragment.class.getSimpleName())) {
            destId = R.id.feedbackFragment;
        } else if (destination.equals(FeedbackTalkBackFragment.class.getSimpleName())) {
            destId = R.id.feedbackTalkBackFragment;
        } else if (destination.equals(MainFragment.class.getSimpleName())) {
            destId = R.id.mainFragment;
        } else if (destination.equals(com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardListFragment.class.getSimpleName())) {
            destId = R.id.boardListFragment;
        } else if (destination.equals(com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardTrashFragment.class.getSimpleName())) {
            destId = R.id.boardTrashFragment;
        } else if (destination.equals(com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardHomeFragment.class.getSimpleName())) {
            destId = R.id.boardHomeFragment;
        } else if (destination.equals(com.dsource.idc.jellowintl.make_my_board_module.fragments.AddEditBoardFragment.class.getSimpleName())) {
            destId = R.id.addEditBoardFragment;
        } else if (destination.equals(com.dsource.idc.jellowintl.make_my_board_module.fragments.IconSelectFragment.class.getSimpleName())) {
            destId = R.id.iconSelectFragment;
        }
        return destId;
    }
}