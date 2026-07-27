package com.dsource.idc.jellowintl.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavDirections;
import androidx.navigation.NavController;

import com.dsource.idc.jellowintl.BuildConfig;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.TalkBack.TalkbackHints_DropDownMenu;
import com.dsource.idc.jellowintl.factories.LanguageFactory;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.dsource.idc.jellowintl.utility.async.InternetTest;
import com.dsource.idc.jellowintl.utility.interfaces.CheckNetworkStatus;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.models.GlobalConstants.SCREEN_SIZE_SEVEN_INCH_TAB;
import static com.dsource.idc.jellowintl.utility.Analytics.bundleEvent;
import static com.dsource.idc.jellowintl.utility.Analytics.getAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.setCrashlyticsCustomKey;
import static com.dsource.idc.jellowintl.utility.Analytics.setUserProperty;
import static com.dsource.idc.jellowintl.utility.SessionManager.LangMap;
import static com.dsource.idc.jellowintl.utility.SessionManager.MR_IN;
import static com.dsource.idc.jellowintl.utility.SessionManager.UNIVERSAL_PACKAGE;

public class UserRegistrationFragment extends BaseFragment implements CheckNetworkStatus {
    private Button bRegister;
    private EditText etName, etEmergencyContact, etAddress;
    private DatabaseReference mRef;
    private CountryCodePicker mCcp;
    private String[] languagesCodes = new String[LangMap.size()];
    private String selectedLanguage;
    private InternetTest internetTest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseMessaging.getInstance().subscribeToTopic("jellow_aac");
        getSession().changePreferencesFile(requireContext());
        //Reset Board Language
        getSession().setCurrentBoardLanguage("");
        getSession().setBoardVoice("");

        if (!getSession().getUserId().equals("")) {
            getAnalytics(requireContext(), getSession().getUserId());
            getSession().setSessionCreatedAt(new Date().getTime());
            FirebaseCrashlytics.getInstance().setUserId(getSession().getUserId());
        }

        NavController navController = NavHostFragment.findNavController(this);

        if (getSession().isUserLoggedIn()) {
            if (LanguageFactory.isLanguageDataAvailable(requireContext()) && getSession().isCompletedIntro()) {
                navController.navigate(R.id.action_userRegistrationFragment_to_splashFragment);
            } else if (!LanguageFactory.isLanguageDataAvailable(requireContext())) {
                Bundle args = new Bundle();
                args.putString("LCODE", UNIVERSAL_PACKAGE);
                args.putBoolean("TUTORIAL", true);
                navController.navigate(R.id.action_userRegistrationFragment_to_languageDownloadFragment, args);
                
                /* 0 represents old value of one by three config*/
                if (getSession().getGridSize() == 0)
                    getSession().setGridSize(GlobalConstants.THREE_ICONS_PER_SCREEN);
                else
                    getSession().setGridSize(GlobalConstants.NINE_ICONS_PER_SCREEN);
            } else if (getSession().getLanguage().equals(MR_IN) && !LanguageFactory.isMarathiPackageAvailable(requireContext())) {
                Bundle args = new Bundle();
                args.putString("LCODE", MR_IN);
                args.putBoolean("TUTORIAL", true);
                navController.navigate(R.id.action_userRegistrationFragment_to_languageDownloadFragment, args);
            } else if (LanguageFactory.isLanguageDataAvailable(requireContext()) && !getSession().isCompletedIntro()) {
                navController.navigate(R.id.action_userRegistrationFragment_to_introFragment);
            }
        } else {
            getSession().setBlood(-1);
            initializeScreenViewsAndListeners(view);
        }
    }

    @Override
    public void onReceiveNetworkState(int state) {
        if (internetTest != null) {
            internetTest.unRegisterReceiver();
        }
        if(state == GlobalConstants.NETWORK_CONNECTED){
            Toast.makeText(requireContext(), getString(R.string.register_user), Toast.LENGTH_SHORT).show();
            autoLoginAndSetupUser();
        }else{
            bRegister.setEnabled(true);
            Toast.makeText(requireContext(), getString(R.string.checkConnectivity), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeScreenViewsAndListeners(View view) {
        getBaseActivity().setupActionBarTitle(View.GONE, getString(R.string.menuUserRegistration));
        
        // Setting up toolbar height for 10' & 7' device
        if (getBaseActivity().getScreenSize() == GlobalConstants.SCREEN_SIZE_TEN_INCH_TAB ||
                getBaseActivity().getScreenSize() == SCREEN_SIZE_SEVEN_INCH_TAB) {
            ScrollView scrollView = view.findViewById(R.id.scrollable);
            if (scrollView != null){
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int startPadding = 16;
                int paddingLeft = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        startPadding,
                        displayMetrics
                );
                scrollView.setPadding(
                        paddingLeft,
                        scrollView.getPaddingTop(),
                        scrollView.getPaddingRight(),
                        scrollView.getPaddingBottom()
                );
            }
        }

        languagesCodes = LanguageFactory.getAvailableLanguages();
        etName = view.findViewById(R.id.etName);
        ((TextView)view.findViewById(R.id.tv_pivacy_link)).setText(Html.fromHtml(getString(R.string.privacy_link_info)));
        etEmergencyContact = view.findViewById(R.id.etEmergencyContact);
        mCcp = view.findViewById(R.id.ccp);
        
        if (getBaseActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE)))
            mCcp.setCountryPreference(null);
        ViewCompat.setAccessibilityDelegate(mCcp, new TalkbackHints_DropDownMenu());
        mCcp.registerCarrierNumberEditText(etEmergencyContact);
        //This listener is useful only when TalkBack accessibility is "ON".
        mCcp.setDialogEventsListener(new CountryCodePicker.DialogEventsListener() {
            @Override public void onCcpDialogOpen(Dialog dialog) {}

            @Override
            public void onCcpDialogDismiss(DialogInterface dialogInterface) {
                etEmergencyContact.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
            }
            @Override public void onCcpDialogCancel(DialogInterface dialogInterface) {}
        });
        etAddress = view.findViewById(R.id.etAddress);
        bRegister = view.findViewById(R.id.bRegister);
        view.findViewById(R.id.childName).setFocusableInTouchMode(true);
        view.findViewById(R.id.childName).setFocusable(true);
        view.findViewById(R.id.cb_privacy_consent).setContentDescription(
                ((TextView)view.findViewById(R.id.tv_pivacy_link)).getText().toString());
        
        Button btnLangSelect = view.findViewById(R.id.btn_lang_select);
        btnLangSelect.setText(languagesCodes[1]);
        btnLangSelect.setOnClickListener(v -> showAvailableLanguageDialog(v));
        selectedLanguage = languagesCodes[1];

        bRegister.setOnClickListener(v -> {
            bRegister.setEnabled(false);

            if (etName.getText().toString().trim().isEmpty()) {
                bRegister.setEnabled(true);
                Toast.makeText(requireContext(),
                        getString(R.string.enterTheName), Toast.LENGTH_SHORT).show();
                return;
            }

            if(!etEmergencyContact.getText().toString().matches("[0-9]+")){
                bRegister.setEnabled(true);
                Toast.makeText(requireContext(),
                        getString(R.string.enternonemptycontact), Toast.LENGTH_SHORT).show();
                return;
            }

            CheckBox cb = view.findViewById(R.id.cb_privacy_consent);
            if (!cb.isChecked()){
                bRegister.setEnabled(true);
                Toast.makeText(requireContext(),
                        getString(R.string.consent_privacy), Toast.LENGTH_SHORT).show();
                return;
            }

            if(selectedLanguage == null)
                return;
            internetTest = new InternetTest();
            internetTest.registerReceiver(UserRegistrationFragment.this);
            internetTest.execute(requireContext());
        });

        if(!getSession().getName().isEmpty()){
            etName.setText(getSession().getName());
            etEmergencyContact.setText(getSession().getCaregiverNumber());
            etAddress.setText(getSession().getAddress());
        }
    }

    private void autoLoginAndSetupUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        mAuth.signInAnonymously()
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && isAdded()) {
                            FirebaseCrashlytics.getInstance().log("User logged in");
                            getSession().setName(etName.getText().toString().trim());
                            getSession().setCaregiverNumber(mCcp.getFullNumberWithPlus());
                            getSession().setUserCountryCode(mCcp.getSelectedCountryCode());
                            getSession().setAddress(etAddress.getText().toString().trim());
                            getSession().setUserId(UUID.randomUUID().toString());

                            getAnalytics(requireContext(), getSession().getUserId());
                            getSession().setSessionCreatedAt(new Date().getTime());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("firstLanguage", selectedLanguage);
                            map.put("versionCode", BuildConfig.VERSION_CODE);
                            map.put("joinedOn", ServerValue.TIMESTAMP);
                            FirebaseDatabase mDB = FirebaseDatabase.getInstance();
                            mRef = mDB.getReference(BuildConfig.DB_TYPE + "/users");
                            mRef.child(getSession().getUserId()).setValue(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful() && isAdded()) {
                                        getSession().setUserLoggedIn(true);
                                        getSession().setLanguage(LangMap.get(selectedLanguage));
                                        
                                        // The voice helper used to be in SpeechEngineBaseActivity, we can just use the base class or static method
                                        String voice = SpeechEngineBaseActivity.getAvailableVoicesForLanguage(
                                                SessionManager.LangMap.get(selectedLanguage));
                                        getSession().setAppVoice(voice.split(",")[0]
                                                +", Voice I"+getBaseActivity().getGender(voice.split(",")[0]));
                                                
                                        getSession().setGridSize(GlobalConstants.NINE_ICONS_PER_SCREEN);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("LanguageSet", "First time "+ LangMap.get(selectedLanguage));
                                        setUserProperty("UserId", getSession().getUserId());
                                        setUserProperty("UserLanguage", LangMap.get(selectedLanguage));
                                        setUserProperty("GridSize", "9");
                                        setUserProperty("PictureViewMode", "PictureText");
                                        bundleEvent("Language", bundle);
                                        FirebaseCrashlytics.getInstance().setUserId(getSession().getUserId());
                                        setCrashlyticsCustomKey("GridSize", "9");
                                        setCrashlyticsCustomKey("PictureViewMode", "PictureText");
                                        
                                        bundle.clear();
                                        bundle.putString("LCODE", UNIVERSAL_PACKAGE);
                                        bundle.putBoolean("TUTORIAL", true);
                                        NavHostFragment.findNavController(UserRegistrationFragment.this).navigate(R.id.action_userRegistrationFragment_to_languageDownloadFragment, bundle);
                                        requireActivity().recreate();
                                    } else {
                                        if (isAdded()) {
                                            bRegister.setEnabled(true);
                                            FirebaseCrashlytics.getInstance().log("User data not added.");
                                            Toast.makeText(requireContext(),
                                                    getString(R.string.checkConnectivity), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (isAdded()) {
                                        bRegister.setEnabled(true);
                                        FirebaseCrashlytics.getInstance().log("User data not added.");
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        Toast.makeText(requireContext(),
                                                getString(R.string.error_in_registration), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else {
                            if (isAdded()) {
                                Toast.makeText(requireContext(),
                                        getString(R.string.error_in_registration), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    bRegister.setEnabled(true);
                }
            }
        });
    }

    public void showAvailableLanguageDialog(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setSingleChoiceItems(LanguageFactory.getAvailableLanguages(), 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedLanguage = languagesCodes[which];
                Button btnLangSelect = getView().findViewById(R.id.btn_lang_select);
                if (btnLangSelect != null) {
                    btnLangSelect.setText(languagesCodes[which]);
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (internetTest != null) {
            internetTest.unRegisterReceiver();
            internetTest.cancel(true);
            internetTest = null;
        }
    }
}
