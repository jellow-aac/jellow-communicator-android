package com.dsource.idc.jellowintl.activities;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_PHONE;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_SEVEN_INCH_TAB;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB;
import static com.dsource.idc.jellowintl.utility.Analytics.setCrashlyticsCustomKey;
import static com.dsource.idc.jellowintl.utility.Analytics.setUserProperty;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.fragments.AboutJellowFragment;
import com.dsource.idc.jellowintl.fragments.AccessibilitySettingsFragment;
import com.dsource.idc.jellowintl.fragments.FeedbackFragment;
import com.dsource.idc.jellowintl.fragments.FeedbackTalkBackFragment;
import com.dsource.idc.jellowintl.fragments.IntroFragment;
import com.dsource.idc.jellowintl.fragments.LanguageDownloadFragment;
import com.dsource.idc.jellowintl.fragments.LanguageSelectFragment;
import com.dsource.idc.jellowintl.fragments.LevelThreeFragment;
import com.dsource.idc.jellowintl.fragments.LevelTwoFragment;
import com.dsource.idc.jellowintl.fragments.MainFragment;
import com.dsource.idc.jellowintl.fragments.ProfileFormFragment;
import com.dsource.idc.jellowintl.fragments.ResetPreferencesFragment;
import com.dsource.idc.jellowintl.fragments.SearchDialogFragment;
import com.dsource.idc.jellowintl.fragments.SequenceFragment;
import com.dsource.idc.jellowintl.fragments.SettingFragment;
import com.dsource.idc.jellowintl.fragments.SplashFragment;
import com.dsource.idc.jellowintl.fragments.TutorialFragment;
import com.dsource.idc.jellowintl.fragments.UserRegistrationFragment;
import com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardSearchActivity;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogNoOfIconPerScreen;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.GridSelectListener;
import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.utility.DefaultExceptionHandler;
import com.dsource.idc.jellowintl.utility.LanguageHelper;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity{
    private static SessionManager sSession;
    private static String sVisibleAct ="";
    private static AppDatabase sAppDatabase;
    private Menu menu;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `BoardModel` (`board_id` TEXT NOT NULL, `board_name` TEXT, `board_icon_list` TEXT, `setup_status` INTEGER NOT NULL, `grid_sized` INTEGER NOT NULL, `language_code` TEXT, `timestamp` INTEGER NOT NULL, `custom_icons` TEXT, PRIMARY KEY(`board_id`))");
            try{
                database.execSQL("ALTER TABLE `VerbiageModel` ADD COLUMN `Search_Tag` TEXT");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            try{
                database.execSQL("ALTER TABLE `BoardModel` ADD COLUMN `board_voice` TEXT");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            try{
                database.execSQL("CREATE TABLE IF NOT EXISTS `CustomIconsModel` (`iconId` TEXT NOT NULL, `iconLanguage` TEXT NOT NULL, `iconLocation` TEXT NOT NULL, `isCategory` INTEGER NOT NULL, `iconVerbiage` TEXT NOT NULL, PRIMARY KEY(`iconId`))");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            try{
                database.execSQL("ALTER TABLE `BoardModel` ADD COLUMN `is_deleted` INTEGER NOT NULL DEFAULT 0");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    @Override
    protected void attachBaseContext(Context newBase) {
       SessionManager s = new SessionManager(newBase);
       if(s.getCurrentBoardLanguage()==null||s.getCurrentBoardLanguage().isEmpty())
           super.attachBaseContext((LanguageHelper.onAttach(newBase)));
       else super.attachBaseContext(LanguageHelper.onAttach(newBase,s.getCurrentBoardLanguage()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modern edge-to-edge support for API 35+
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            // On API 35+, system bars are forced transparent. 
            // We use the root background + padding to show brand colors.
            getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

            // Ensure system bar icons are white (for the red background)
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(false);
                controller.setAppearanceLightNavigationBars(false);
            }
        } else if (isGestureNavigationEnabled()) {
            // Legacy gesture mode support
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
            getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getWindow().setNavigationBarContrastEnforced(false);
            }
        } else {
            // Restore default stable behavior for 2/3 button mode on legacy devices
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        final String APP_DB_NAME = "jellow_app_database";
        // Initialize default exception handler for this activity.
        // If any exception occurs during this activity usage,
        // handle it using default exception handler.
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));
        if (sSession == null)
            sSession = new SessionManager(this);
        if(sAppDatabase == null)
            sAppDatabase = Room.databaseBuilder(this, AppDatabase.class, APP_DB_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .build();
    }

    @Override
    protected void onDestroy() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.search) {
            if (getLevelClass().contains(getVisibleAct())) {
                SearchDialogFragment.newInstance().show(getSupportFragmentManager(), SearchDialogFragment.class.getSimpleName());
            } else {
                Bundle args = new Bundle();
                args.putString(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.SEARCH_FOR_BOARD);
                BoardSearchActivity searchDialog =
                        BoardSearchActivity.newInstance(args, (icon, resultString) -> {
                            androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                            if (navHostFragment != null) {
                                androidx.fragment.app.Fragment currentFrag = navHostFragment.getChildFragmentManager().getFragments().isEmpty() ? null : navHostFragment.getChildFragmentManager().getFragments().get(0);
                                if (currentFrag instanceof com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardListFragment) {
                                    ((com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardListFragment) currentFrag).highlightSearchedBoard(resultString);
                                } else if (currentFrag instanceof com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardTrashFragment) {
                                    ((com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardTrashFragment) currentFrag).highlightSearchedBoard(resultString);
                                }
                            }
                        });
                searchDialog.show(getSupportFragmentManager(), "BoardSearchActivity");
            }
        } else if (itemId == R.id.my_boards_icon || itemId == R.id.my_boards) {
            navigateToDestination(com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardListFragment.class.getSimpleName(), R.id.boardListFragment);
        } else if (itemId == R.id.my_boards_trash) {
            navigateToDestination(com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardTrashFragment.class.getSimpleName(), R.id.boardTrashFragment);
        } else if (itemId == R.id.number_of_icons) {
            showGridDialog((GridSelectListener) size -> {
                getSession().setGridSize(size);
                setGridSize();
                if (this instanceof AppActivity) {
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                    if (navController.getCurrentDestination() != null) {
                        int currentDest = navController.getCurrentDestination().getId();
                        navController.navigate(currentDest, null, new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(currentDest, true).build());
                    }
                } else {
                    startActivity(new Intent(getApplicationContext(), AppActivity.class));
                    finish();
                }
            }, getSession().getGridSize());
        } else if (itemId == R.id.profile) {
            navigateToDestination(ProfileFormFragment.class.getSimpleName(), R.id.profileFormFragment);
        } else if (itemId == R.id.aboutJellow) {
            navigateToDestination(AboutJellowFragment.class.getSimpleName(), R.id.aboutJellowFragment);
        } else if (itemId == R.id.tutorial) {
            navigateToDestination(TutorialFragment.class.getSimpleName(), R.id.tutorialFragment);
        } else if (itemId == R.id.languageSelect) {
            navigateToDestination(LanguageSelectFragment.class.getSimpleName(), R.id.languageSelectFragment);
        } else if (itemId == R.id.settings) {
            navigateToDestination(SettingFragment.class.getSimpleName(), R.id.settingFragment);
        } else if (itemId == R.id.accessibilitySetting) {
            navigateToDestination(AccessibilitySettingsFragment.class.getSimpleName(), R.id.accessibilitySettingsFragment);
        } else if (itemId == R.id.resetPreferences) {
            navigateToDestination(ResetPreferencesFragment.class.getSimpleName(), R.id.resetPreferencesFragment);
        } else if (itemId == R.id.feedback) {
            if (!getVisibleAct().equals(FeedbackFragment.class.getSimpleName()) &&
                    !getVisibleAct().equals(FeedbackTalkBackFragment.class.getSimpleName())) {
                int destId = isAccessibilityTalkBackOn((android.view.accessibility.AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE)) ?
                        R.id.feedbackTalkBackFragment : R.id.feedbackFragment;
                String destName = destId == R.id.feedbackTalkBackFragment ? FeedbackTalkBackFragment.class.getSimpleName() : FeedbackFragment.class.getSimpleName();
                navigateToDestination(destName, destId);
            }
        } else if (itemId == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                androidx.fragment.app.Fragment primary = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                if (primary != null && primary.isVisible() && primary.onOptionsItemSelected(item)) {
                    return true;
                }
                for (androidx.fragment.app.Fragment f : navHostFragment.getChildFragmentManager().getFragments()) {
                    if (f != null && f.isVisible() && f.onOptionsItemSelected(item)) {
                        return true;
                    }
                }
            }
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void navigateToDestination(String fragmentName, int destinationId) {
        if (!getVisibleAct().equals(fragmentName)) {
            if (this instanceof AppActivity) {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                androidx.navigation.NavOptions.Builder builder = new androidx.navigation.NavOptions.Builder();
                if (!getVisibleAct().isEmpty() && !getLevelClass().contains(getVisibleAct()) &&
                        !getNonMenuClass().contains(getVisibleAct())) {
                    if (navController.getCurrentDestination() != null) {
                        builder.setPopUpTo(navController.getCurrentDestination().getId(), true);
                    }
                }
                navController.navigate(destinationId, null, builder.build());
            } else {
                Intent intent = new Intent(this, AppActivity.class);
                intent.putExtra("destination", fragmentName);
                startActivity(intent);
                if (!getLevelClass().contains(getVisibleAct())) finish();
            }
        }
    }

    public SessionManager getSession(){
        return sSession;
    }

    public AppDatabase getAppDatabase(){
        return sAppDatabase;
    }

    public boolean isConnectedToNetwork(ConnectivityManager connMgr){
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * <p>This function check whether user device is not Wi-Fi only and
     * has sim card inserted into SIM slot and user can make a call.
     * @return true if device can make phone calls.</p>
     * */
    public boolean isDeviceReadyToCall(TelephonyManager tm){
        return tm != null
            && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE
                && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    /**
     * <p>This function check whether, does Accessibility Talkback feature turned off or not.
     * @return true if Accessibility Talkback feature is on.</p>
     * */
    public boolean isAccessibilityTalkBackOn(AccessibilityManager am) {
        return am != null && am.isEnabled() && am.isTouchExplorationEnabled();
    }

    /**
     * <p>This function gives screen aspect ratio.
     * @return aspect ratio value in float.</p>
     * */
    public boolean isNotchDevice(){
        float aspectRatio = (float)this.getResources().getDisplayMetrics().widthPixels /
                ((float)this.getResources().getDisplayMetrics().heightPixels);
        return (aspectRatio >= 2.0 && aspectRatio <= 2.15);
    }

    public int getScreenSize(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth > 720) {
            //Device is a 10" tablet
            return SCREEN_SIZE_TEN_INCH_TAB;
        }else if (smallestWidth > 600) {
            //Device is a 7" tablet
            return SCREEN_SIZE_SEVEN_INCH_TAB;
        }else
            return SCREEN_SIZE_PHONE;
    }

    public View getRootViewForCurrentDestination() {
        Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHost != null) {
            Fragment primary = navHost.getChildFragmentManager().getPrimaryNavigationFragment();
            if (primary != null && primary.getView() != null) {
                return primary.getView();
            }
            for (Fragment f : navHost.getChildFragmentManager().getFragments()) {
                if (f != null && f.isVisible() && f.getView() != null) {
                    return f.getView();
                }
            }
        }
        return getWindow() != null && getWindow().getDecorView() != null ? getWindow().getDecorView() : null;
    }

    public void setupActionBarTitle(String title) {
        setupActionBarTitle((View) null, title);
    }

    public void setupActionBarTitle(int isBackVisible, String title) {
        setupActionBarTitle((View) null, isBackVisible, title);
    }

    public void setupActionBarTitle(View view, String title) {
        View target = (view != null) ? view : getRootViewForCurrentDestination();
        if (target != null) {
            TextView tvTitle = target.findViewById(R.id.tvActionbarTitle);
            if (tvTitle != null) {
                if (title != null && title.contains("("))
                    tvTitle.setText(title.substring(0, title.indexOf("(")));
                else
                    tvTitle.setText(title != null ? title : "");
            }
        }
        setupToolbarMenu(view);
    }

    public void setupActionBarTitle(View view, int isBackVisible, String title) {
        View target = (view != null) ? view : getRootViewForCurrentDestination();
        if (target != null) {
            View ivBack = target.findViewById(R.id.iv_action_bar_back);
            if (ivBack != null) {
                ivBack.setVisibility(isBackVisible);
                ivBack.setOnClickListener(this::finishCurrentActivity);
            }
            TextView tvTitle = target.findViewById(R.id.tvActionbarTitle);
            if (tvTitle != null) {
                if (title != null && title.contains("("))
                    tvTitle.setText(title.substring(0, title.indexOf("(")));
                else
                    tvTitle.setText(title != null ? title : "");
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            
            setupToolbarMenu(target);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                int dummyId = getResources().getIdentifier("dummyStatusBar", "id", getPackageName());
                if (dummyId != 0 && target.findViewById(dummyId) != null) {
                    target.findViewById(dummyId).setVisibility(View.GONE);
                }
            }
            MaterialToolbar toolbar = target.findViewById(R.id.toolbar);
            adjustToolbarForTablet(toolbar);
        }
    }

    public void adjustToolbarForTablet(MaterialToolbar toolbar) {
        if (toolbar == null) return;
        // Setting up toolbar height for 10' & 7' device
        if (getScreenSize() == GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB ||
                getScreenSize() == SCREEN_SIZE_SEVEN_INCH_TAB) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int height = 62;
            int heightInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    height,
                    displayMetrics
            );
            ViewGroup.LayoutParams toolbarParams = toolbar.getLayoutParams();
            if (toolbarParams != null && toolbarParams.height != heightInPx) {
                toolbarParams.height = heightInPx;
                int startPadding = 32;
                int startPaddingInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        startPadding,
                        displayMetrics
                );
                toolbar.setPadding(
                        startPaddingInPx,
                        toolbar.getPaddingTop(),
                        toolbar.getPaddingRight(),
                        toolbar.getPaddingBottom()
                );
                toolbar.setLayoutParams(toolbarParams);
            }
        }
    }

    public void setupToolbarMenu() {
        setupToolbarMenu(null);
    }

    public void setupToolbarMenu(View view) {
        View target = (view != null) ? view : getRootViewForCurrentDestination();
        if (target == null) {
            return;
        }
        MaterialToolbar toolbar = target.findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        
        adjustToolbarForTablet(toolbar);
        
        String visible = getVisibleAct();
        toolbar.getMenu().clear();
        if (!visible.isEmpty() && getIconSelectActivityClass().contains(visible)) {
            toolbar.inflateMenu(R.menu.my_board_select_icon_menu);
        } else if (!visible.isEmpty() && (getBoardAddEditActivityClass().contains(visible) ||
                getBoardHomeActivityClass().contains(visible))) {
            toolbar.inflateMenu(R.menu.board_home_menu);
        } else {
            toolbar.inflateMenu(R.menu.menu_main);
        }

        Menu menu = toolbar.getMenu();

        // 2. Apply your existing logic to the menu instance
        if (!visible.isEmpty() && (getBoardSearchClass().contains(visible) || getNonMenuClass().contains(visible))) {
            menu.clear(); // Hide menu if it's a non-menu activity
            return;
        }

        MenuCompat.setGroupDividerEnabled(menu, true);

        // Reflection to show icons in the overflow menu
        try {
            Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            method.setAccessible(true);
            method.invoke(menu, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // --- Apply your visibility logic ---
        applyMenuVisibilityLogic(menu);

        // 3. Set the Click Listener (Replacing onOptionsItemSelected)
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
    }

    public void setupParent(){
        if (findViewById(R.id.parent) != null) {
            View vParent = findViewById(R.id.parent);
            // On API 35+, we set the parent background to red to achieve solid red status/nav bars
            if (Build.VERSION.SDK_INT >= 35) {
                vParent.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary));
            }
            ViewCompat.setOnApplyWindowInsetsListener(vParent, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                // On API 35+, edge-to-edge is forced. We use padding to expose the red root background.
                int bottomInset = insets.bottom;
                if (Build.VERSION.SDK_INT >= 35) {
                    if (isGestureNavigationEnabled()) {
                        // In gesture mode, we want the content to bleed through (transparent nav bar)
                        bottomInset = 0;
                    }
                } else if (isGestureNavigationEnabled()) {
                    // Legacy logic
                    bottomInset = 0;
                }
                
                v.setPadding(insets.left, insets.top, insets.right, bottomInset);
                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    public void setupBottomBar(){
        LinearLayout llBottom = findViewById(R.id.llBottom);
        if (llBottom != null) {
            // For API 35+, ensure the bottom bar container itself handles red padding if needed
            if (Build.VERSION.SDK_INT >= 35) {
                View vParent = findViewById(R.id.parent);
                if (vParent != null) vParent.setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary));
            }
            ViewCompat.setOnApplyWindowInsetsListener(llBottom, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                
                int bottomInset = insets.bottom;
                if (Build.VERSION.SDK_INT >= 35) {
                    if (isGestureNavigationEnabled()) {
                        bottomInset = 0;
                    }
                } else if (isGestureNavigationEnabled()) {
                    bottomInset = 0;
                }

                v.setPadding(insets.left, 0, insets.right, bottomInset);
                return WindowInsetsCompat.CONSUMED;
            });
            if (getScreenSize() == SCREEN_SIZE_PHONE) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int padding = 8;
                int paddingInPx = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        padding,
                        displayMetrics
                );
                llBottom.setPadding(paddingInPx,0,0,0);
            }
        }
    }

    private boolean isGestureNavigationEnabled() {
        int mode = 0;
        try {
            int resourceId = getResources().getIdentifier("config_navBarInteractionMode", "integer", "android");
            if (resourceId > 0) {
                mode = getResources().getInteger(resourceId);
            }
        } catch (Exception ignored) {}
        return mode == 2;
    }

    private void applyMenuVisibilityLogic(Menu menu) {
        String visible = getVisibleAct();
        if (visible.isEmpty()) {
            return;
        }
        if (!getLevelClass().contains(visible)
                && !getBoardListClass().contains(visible)
                && !getBoardTrashClass().contains(visible)
                && !getBoardAddEditActivityClass().contains(visible)
                && !getBoardHomeActivityClass().contains(visible)
                && !getIconSelectActivityClass().contains(visible)){
            if (menu.findItem(R.id.search) != null) menu.findItem(R.id.search).setVisible(false);
            if (menu.findItem(R.id.my_boards_icon) != null) menu.findItem(R.id.my_boards_icon).setVisible(false);
            if (menu.findItem(R.id.number_of_icons) != null) menu.findItem(R.id.number_of_icons).setVisible(false);
        }else if(getBoardListClass().contains(visible)) {
            setMenu(menu);
            if (menu.findItem(R.id.enable_edit) != null) menu.findItem(R.id.enable_edit).setVisible(true);
            if (menu.findItem(R.id.enable_delete) != null) menu.findItem(R.id.enable_delete).setVisible(true);
            if (menu.findItem(R.id.my_boards_icon) != null) menu.findItem(R.id.my_boards_icon).setVisible(false);
            if (menu.findItem(R.id.number_of_icons) != null) menu.findItem(R.id.number_of_icons).setVisible(false);
            if (menu.findItem(R.id.search) != null) menu.findItem(R.id.search).setTitle(R.string.search_board_in_jellow);
        }else if(getBoardTrashClass().contains(visible)){
            setMenu(menu);
            if (menu.findItem(R.id.enable_delete) != null) menu.findItem(R.id.enable_delete).setVisible(true);
            if (menu.findItem(R.id.enable_edit) != null) menu.findItem(R.id.enable_edit).setVisible(false);
            if (menu.findItem(R.id.my_boards_icon) != null) menu.findItem(R.id.my_boards_icon).setVisible(false);
            if (menu.findItem(R.id.number_of_icons) != null) menu.findItem(R.id.number_of_icons).setVisible(false);
            if (menu.findItem(R.id.search) != null) menu.findItem(R.id.search).setTitle(R.string.search_board_in_jellow);
        }else if(getBoardAddEditActivityClass().contains(visible)){
            if (menu.findItem(R.id.reposition_lock) != null) menu.findItem(R.id.reposition_lock).setVisible(false);
            if (menu.findItem(R.id.action_home_app) != null) menu.findItem(R.id.action_home_app).setVisible(false);
        } else if (getBoardHomeActivityClass().contains(visible)) {
            setMenu(menu);
        }
        if (isAccessibilityTalkBackOn((AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))) {
            if (menu.findItem(R.id.closePopup) != null) menu.findItem(R.id.closePopup).setVisible(true);
        }
    }

    public void finishCurrentActivity(View view) {
        if (this instanceof AppActivity) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.mainFragment) {
                finish();
            } else {
                getOnBackPressedDispatcher().onBackPressed();
            }
        } else {
            finish();
        }
    }

    public void openPrivacyPolicyPage(View view){
        try{
         startActivity(new Intent("android.intent.action.VIEW",
                Uri.parse(getString(R.string.privacy_link))));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * <p>This function will find and return the blood group of user
     * @return blood group of user.</p>
     * */
    public String getBloodGroup(int bloodGroup) {
        switch(bloodGroup){
            case  1: return getString(R.string.aPos);
            case  2: return getString(R.string.aNeg);
            case  3: return getString(R.string.bPos);
            case  4: return getString(R.string.bNeg);
            case  5: return getString(R.string.abPos);
            case  6: return getString(R.string.abNeg);
            case  7: return getString(R.string.oPos);
            case  8: return getString(R.string.oNeg);
            default: return "";
        }
    }

    private String getLevelClass() {
        return MainFragment.class.getSimpleName() + "," +
            LevelTwoFragment.class.getSimpleName() + "," +
            LevelThreeFragment.class.getSimpleName() + "," +
            SequenceFragment.class.getSimpleName();
    }

    private String getBoardSearchClass() {
        return BoardSearchActivity.class.getSimpleName() ;
    }

    private String getBoardListClass(){
        return com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardListFragment.class.getSimpleName();
    }

    private String getIconSelectActivityClass(){
        return com.dsource.idc.jellowintl.make_my_board_module.fragments.IconSelectFragment.class.getSimpleName();
    }

    private String getBoardAddEditActivityClass(){
        return com.dsource.idc.jellowintl.make_my_board_module.fragments.AddEditBoardFragment.class.getSimpleName();
    }

    private String getBoardHomeActivityClass(){
        return com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardHomeFragment.class.getSimpleName();
    }

    private String getBoardTrashClass(){
        return com.dsource.idc.jellowintl.make_my_board_module.fragments.BoardTrashFragment.class.getSimpleName();
    }

    private String getNonMenuClass() {
        return UserRegistrationFragment.class.getSimpleName() + "," +
               SplashFragment.class.getSimpleName() + "," +
               IntroFragment.class.getSimpleName() + "," +
               LanguageDownloadFragment.class.getSimpleName();
    }

    public String getVisibleAct() {
        if (sVisibleAct != null && !sVisibleAct.isEmpty()) {
            return sVisibleAct;
        }
        try {
            androidx.fragment.app.Fragment navHost = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHost != null) {
                androidx.fragment.app.Fragment primary = navHost.getChildFragmentManager().getPrimaryNavigationFragment();
                if (primary != null) {
                    return primary.getClass().getSimpleName();
                }
                for (androidx.fragment.app.Fragment f : navHost.getChildFragmentManager().getFragments()) {
                    if (f != null && f.isVisible()) {
                        return f.getClass().getSimpleName();
                    }
                }
            }
        } catch (Exception ignored) {}
        return sVisibleAct != null ? sVisibleAct : "";
    }

    public void setVisibleAct(String visibleAct) {
        sVisibleAct = visibleAct;
    }

    public void setGridSize(){
        if(getSession().getGridSize() == GlobalConstants.NINE_ICONS_PER_SCREEN){
            setUserProperty("GridSize", "9");
            setCrashlyticsCustomKey("GridSize", "9");
        }else if(getSession().getGridSize() == GlobalConstants.FOUR_ICONS_PER_SCREEN){
            setUserProperty("GridSize", "4");
            setCrashlyticsCustomKey("GridSize", "4");
        }else if(getSession().getGridSize() == GlobalConstants.THREE_ICONS_PER_SCREEN){
            setUserProperty("GridSize", "3");
            setCrashlyticsCustomKey("GridSize", "3");
        }else if(getSession().getGridSize() == GlobalConstants.TWO_ICONS_PER_SCREEN){
            setUserProperty("GridSize", "2");
            setCrashlyticsCustomKey("GridSize", "2");
        }else if(getSession().getGridSize() == GlobalConstants.ONE_ICON_PER_SCREEN){
            setUserProperty("GridSize", "1");
            setCrashlyticsCustomKey("GridSize", "1");
        }
    }

    public boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void showGridDialog(GridSelectListener mGridSizeSelectListener, int gridSize) {
        DialogNoOfIconPerScreen dialog = DialogNoOfIconPerScreen.newInstance(gridSize, mGridSizeSelectListener);
        dialog.show(getSupportFragmentManager(), "DialogNoOfIconPerScreen");
    }

    public String getRomanNumber(int num){
        int[] values = {100,90,50,40,10,9,5,4,1};
        String[] romanLiterals = {"C","XC","L","XL","X","IX","V","IV","I"};
        StringBuilder romanNumber = new StringBuilder();
        for(int i=0;i<values.length;i++) {
            while(num >= values[i]) {
                num -= values[i];
                romanNumber.append(romanLiterals[i]);
            }
        }
        return romanNumber.toString();
    }

    public String getGender(String voice) {
        return SpeechEngineBaseActivity.voiceGender.get(voice);
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public void applyMonochromeColor(){
        if(getSession().getMonochromeDisplayState()) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            Paint greyscalePaint = new Paint();
            greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    View statusBar = decor.findViewById(android.R.id.statusBarBackground);
                    statusBar.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
                    return true;
                }
            });
            findViewById(R.id.parent).setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
            try {
                ((View) getSupportActionBar().getCustomView().getParent().getParent()).setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    public void applyMonochromeColor(View view){
        if(getSession().getMonochromeDisplayState()) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            Paint greyscalePaint = new Paint();
            greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
            view.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
        }
    }
}