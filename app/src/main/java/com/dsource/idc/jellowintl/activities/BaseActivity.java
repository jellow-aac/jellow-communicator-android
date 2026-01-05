package com.dsource.idc.jellowintl.activities;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static com.dsource.idc.jellowintl.make_my_board_module.utility.BoardConstants.GRID_SIZE;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_PHONE;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_SEVEN_INCH_TAB;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB;
import static com.dsource.idc.jellowintl.utility.Analytics.setCrashlyticsCustomKey;
import static com.dsource.idc.jellowintl.utility.Analytics.setUserProperty;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.make_my_board_module.activity.BoardListActivity;
import com.dsource.idc.jellowintl.make_my_board_module.activity.BoardSearchActivity;
import com.dsource.idc.jellowintl.make_my_board_module.activity.BoardTrashActivity;
import com.dsource.idc.jellowintl.make_my_board_module.activity.HomeActivity;
import com.dsource.idc.jellowintl.make_my_board_module.custom_dialogs.DialogNoOfIconPerScreen;
import com.dsource.idc.jellowintl.make_my_board_module.interfaces.GridSelectListener;
import com.dsource.idc.jellowintl.models.AppDatabase;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.utility.DefaultExceptionHandler;
import com.dsource.idc.jellowintl.utility.LanguageHelper;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity{
    final private String APP_DB_NAME = "jellow_app_database";
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
       if(s.getCurrentBoardLanguage()==null||s.getCurrentBoardLanguage().equals(""))
           super.attachBaseContext((LanguageHelper.onAttach(newBase)));
       else super.attachBaseContext(LanguageHelper.onAttach(newBase,s.getCurrentBoardLanguage()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if(getBoardClass().contains(getVisibleAct()) || getNonMenuClass().contains(getVisibleAct()))
            return false;
        super.onCreateOptionsMenu(menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        try {
            Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            method.setAccessible(true);
            method.invoke(menu, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!getLevelClass().contains(getVisibleAct())
                && !getVisibleAct().contains(getBoardListClass())
                    && !getVisibleAct().contains(getBoardTrashClass())){
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.my_boards_icon).setVisible(false);
            menu.findItem(R.id.number_of_icons).setVisible(false);
        }else if(getVisibleAct().contains(getBoardListClass())){
            setMenu(menu);
            menu.findItem(R.id.enable_edit).setVisible(true);
            menu.findItem(R.id.enable_delete).setVisible(true);
            menu.findItem(R.id.my_boards_icon).setVisible(false);
            menu.findItem(R.id.number_of_icons).setVisible(false);
            menu.findItem(R.id.search).setTitle(R.string.search_board_in_jellow);
        }else if(getVisibleAct().contains(getBoardTrashClass())){
            setMenu(menu);
            menu.findItem(R.id.enable_delete).setVisible(true);
            menu.findItem(R.id.enable_edit).setVisible(false);
            menu.findItem(R.id.my_boards_icon).setVisible(false);
            menu.findItem(R.id.number_of_icons).setVisible(false);
            menu.findItem(R.id.search).setTitle(R.string.search_board_in_jellow);
        }
        if (isAccessibilityTalkBackOn((AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))) {
            menu.findItem(R.id.closePopup).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.search) {
            if (getLevelClass().contains(getVisibleAct())) {
                startActivity(new Intent(this, SearchActivity.class));
            } else {
                Intent searchIntent = new Intent(this, BoardSearchActivity.class);
                searchIntent.putExtra(BoardSearchActivity.SEARCH_MODE, BoardSearchActivity.SEARCH_FOR_BOARD);
                startActivityForResult(searchIntent, Integer.parseInt(getString(R.string.search_board)));
            }
        } else if (itemId == R.id.my_boards_icon || itemId == R.id.my_boards) {
            if (getVisibleAct().equals(BoardListActivity.class.getSimpleName())) {
                // Do nothing, already on the correct screen.
            } else {
                startActivity(new Intent(this, BoardListActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.my_boards_trash) {
            if (getVisibleAct().equals(BoardTrashActivity.class.getSimpleName())) {
                // Do nothing, already on the correct screen.
            } else {
                startActivity(new Intent(this, BoardTrashActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.number_of_icons) {
            showGridDialog(new GridSelectListener() {
                @Override
                public void onGridSelectListener(int size) {
                    getSession().setGridSize(size);
                    setGridSize();
                    startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                    finish();
                }
            }, getSession().getGridSize());
        } else if (itemId == R.id.profile) {
            if (!getVisibleAct().equals(ProfileFormActivity.class.getSimpleName())) {
                startActivity(new Intent(this, ProfileFormActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.aboutJellow) {
            if (!getVisibleAct().equals(AboutJellowActivity.class.getSimpleName())) {
                startActivity(new Intent(this, AboutJellowActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.tutorial) {
            if (!getVisibleAct().equals(TutorialActivity.class.getSimpleName())) {
                startActivity(new Intent(this, TutorialActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } /* else if (itemId == R.id.keyboardInput) { // Uncomment if you bring this feature back
        if (!getVisibleAct().equals(KeyboardInputActivity.class.getSimpleName())) {
            startActivity(new Intent(this, KeyboardInputActivity.class));
            if (!getLevelClass().contains(getVisibleAct())) {
                finish();
            }
        }
    } */ else if (itemId == R.id.languageSelect) {
            if (!getVisibleAct().equals(LanguageSelectActivity.class.getSimpleName())) {
                startActivity(new Intent(this, LanguageSelectActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.settings) {
            if (!getVisibleAct().equals(SettingActivity.class.getSimpleName())) {
                startActivity(new Intent(this, SettingActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.accessibilitySetting) {
            if (!getVisibleAct().equals(AccessibilitySettingsActivity.class.getSimpleName())) {
                startActivity(new Intent(this, AccessibilitySettingsActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.resetPreferences) {
            if (!getVisibleAct().equals(ResetPreferencesActivity.class.getSimpleName())) {
                startActivity(new Intent(this, ResetPreferencesActivity.class));
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == R.id.feedback) {
            if (!getVisibleAct().equals(FeedbackActivity.class.getSimpleName()) &&
                    !getVisibleAct().equals(FeedbackActivityTalkBack.class.getSimpleName())) {
                if (isAccessibilityTalkBackOn((AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE))) {
                    startActivity(new Intent(this, FeedbackActivityTalkBack.class));
                } else {
                    startActivity(new Intent(this, FeedbackActivity.class));
                }
                if (!getLevelClass().contains(getVisibleAct())) {
                    finish();
                }
            }
        } else if (itemId == android.R.id.home) {
            finish();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public SessionManager getSession(){
        return sSession;
    }

    protected AppDatabase getAppDatabase(){
        return sAppDatabase;
    }

    public boolean isConnectedToNetwork(ConnectivityManager connMgr){
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * <p>This function check whether user device is not wifi only and
     * has sim card inserted into SIM slot and user can make a call.
     * @return true if device can able to make phone calls.</p>
     * */
    public boolean isDeviceReadyToCall(TelephonyManager tm){
        return tm != null
            && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE
                && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    /**
     * <p>This function check whether does Accessibility Talkback feature turned of or not.
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


    public void setupActionBarTitle(String title) {
        ((TextView) findViewById(R.id.tvActionbarTitle)).setText(title);
    }

    public void setupActionBarTitle(int isBackVisible, String title){
        findViewById(R.id.iv_action_bar_back).setVisibility(isBackVisible);
        if (title.contains("("))
            ((TextView)findViewById(R.id.tvActionbarTitle)).setText(title.substring(0,title.indexOf("(")));
        else
            ((TextView)findViewById(R.id.tvActionbarTitle)).setText(title);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            AppBarLayout appBarLayout = findViewById(R.id.app_bar);
            if (appBarLayout != null) {
                ViewCompat.setOnApplyWindowInsetsListener(appBarLayout, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    // Apply the insets as a margin to the view. This solution sets only the
                    // bottom, left, and right dimensions, but you can apply whichever insets are
                    // appropriate to your layout. You can also update the view padding if that's
                    // more appropriate.
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                    mlp.leftMargin = insets.left;
                    mlp.bottomMargin = insets.bottom;
                    mlp.rightMargin = insets.right;
                    mlp.topMargin = 0;
                    v.setLayoutParams(mlp);
                    // Return CONSUMED if you don't want the window insets to keep passing
                    // down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
                appBarLayout.setPadding(32,0,0,0);
            }
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
                    int right = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout()).left;
                    v.post(() -> {
                        v.setPadding(right, 0, 0, 0);
                        v.requestLayout();
                    });
                    // Return CONSUMED if you don't want the window insets to keep passing
                    // down to descendant views.
                    return WindowInsetsCompat.CONSUMED;
                });
            }
        } else {
            findViewById(R.id.dummyStatusBar).setVisibility(View.GONE);
        }
    }

    public void enableNavigationBack(){
        Toolbar toolbar = findViewById(R.id.toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
    }

    public void setNavigationUiConditionally() {
        try {
            int resourceId = getResources().getIdentifier("config_navBarInteractionMode", "integer", "android");
            if (getResources().getInteger(resourceId) == 2) {
                View view = findViewById(R.id.parent);
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
                lp.topMargin = 68;
                getWindow().setNavigationBarColor(getResources().getColor(R.color.transparent));
            } else {
                getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public void finishCurrentActivity(View view) {
        finish();
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
        return MainActivity.class.getSimpleName() + "," +
            LevelTwoActivity.class.getSimpleName() + "," +
            LevelThreeActivity.class.getSimpleName() + "," +
            SequenceActivity.class.getSimpleName();
    }

    private String getBoardClass() {
        return BoardSearchActivity.class.getSimpleName() + ","+
                HomeActivity.class.getSimpleName();
    }

    private String getBoardListClass(){
        return BoardListActivity.class.getSimpleName();
    }

    private String getBoardTrashClass(){
        return BoardTrashActivity.class.getSimpleName();
    }

    private String getNonMenuClass() {
        return UserRegistrationActivity.class.getSimpleName();
    }

    public String getVisibleAct() {
        return sVisibleAct;
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
        Intent gridDialog = new Intent(this, DialogNoOfIconPerScreen.class);
        gridDialog.putExtra(GRID_SIZE, gridSize);
        DialogNoOfIconPerScreen.mGridSelectionListener =mGridSizeSelectListener;
        startActivity(gridDialog);
    }

    public boolean hasCameraHardware(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
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