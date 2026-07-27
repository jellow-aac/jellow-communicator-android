package com.dsource.idc.jellowintl.activities;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;
import static android.content.Context.ACCESSIBILITY_SERVICE;
import static com.dsource.idc.jellowintl.utility.SessionManager.MR_IN;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsource.idc.jellowintl.BuildConfig;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.utility.SessionManager;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.hbb20.CountryCodePicker;

public class ProfileFormFragment extends Fragment {
    private Button btnSave;
    private EditText etChildName, etCaregiverContact, etCaregiverName, etAddress, etEmailId;
    private String mDetailSaved;
    private CountryCodePicker mCcp;
    private Spinner mBloodGroup;
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
        return inflater.inflate(R.layout.activity_profile_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(ProfileFormFragment.class.getSimpleName());
        getAppActivity().setupActionBarTitle(View.VISIBLE, getString(R.string.home)+"/ "+getString(R.string.menuProfile));
        getAppActivity().setupToolbarMenu();
        getAppActivity().setupBottomBar();
        getAppActivity().applyMonochromeColor();
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        etChildName = view.findViewById(R.id.etName);
        etCaregiverName = view.findViewById(R.id.etFathername);
        etCaregiverContact = view.findViewById(R.id.etFathercontact);
        etAddress = view.findViewById(R.id.etAddress);
        etEmailId = view.findViewById(R.id.etEmailAddress);
        mBloodGroup = view.findViewById(R.id.bloodgroup);
        ((TextView)view.findViewById(R.id.view_privacy_policy)).setText(Html.fromHtml(getString(R.string.txt_view_privacy_policy)));
        
        if (getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))){
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                    R.array.bloodgroup_talkback, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBloodGroup.setAdapter(adapter);
        }else{
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireContext(), R.array.bloodgroup, R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBloodGroup.setAdapter(adapter);
        }
        btnSave = view.findViewById(R.id.bSave);

        etChildName.setText(getSession().getName());

        mCcp = view.findViewById(R.id.ccp);
        mCcp.setCountryForPhoneCode(Integer.valueOf(getSession().getUserCountryCode()));
        mCcp.registerCarrierNumberEditText(etCaregiverContact);
        String contact = getSession().getCaregiverNumber().replace(
                "+".concat(getSession().getUserCountryCode()),"");
        etCaregiverContact.setText(contact);

        etCaregiverName.setText(getSession().getCaregiverName());
        etAddress.setText(getSession().getAddress());
        etEmailId.setText(getSession().getEmailId());

        if(getSession().getBlood() != -1)
            mBloodGroup.setSelection(getSession().getBlood());

        final String strEnterName = getString(R.string.enterTheName);
        final String strInvalidEmail = getString(R.string.invalid_emailId);
        final String strInvalidAddress = getString(R.string.invalid_address);
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseCrashlytics.getInstance().log("Profile Save");
                btnSave.setEnabled(false);
                if (etChildName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(requireContext(),
                            strEnterName, Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }
                if(!etCaregiverContact.getText().toString().matches("[0-9]+")){
                    Toast.makeText(requireContext(),
                            getString(R.string.enternonemptycontact), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }
                if (!etEmailId.getText().toString().trim().isEmpty() &&
                        !getAppActivity().isValidEmail(etEmailId.getText().toString().trim())) {
                    Toast.makeText(requireContext(),
                            strInvalidEmail, Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    return;
                }
                savedProfileDetails();
            }
        });

        mDetailSaved = getString(R.string.detailSaved);
        if(getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            view.findViewById(R.id.tvName).setFocusableInTouchMode(true);
            view.findViewById(R.id.tvName).setFocusable(true);
            mCcp.setCountryPreference(null);
        }
        
        addAccessibilityDelegateToSpinners(view);
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring("ProfileFormActivity");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()){
            resetAnalytics(requireContext(), getSession().getUserId());
        }
        startMeasuring();
    }

    private void addAccessibilityDelegateToSpinners(View view) {
        if (getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            mBloodGroup.setAccessibilityDelegate(new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                    super.onInitializeAccessibilityEvent(host, event);
                    if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                        btnSave.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
                    }
                }
            });
        }
    }

    private void savedProfileDetails() {
        getSession().setCaregiverName(etCaregiverName.getText().toString());
        getSession().setAddress(etAddress.getText().toString());
        getSession().setName(etChildName.getText().toString());
        getSession().setCaregiverNumber(mCcp.getFullNumberWithPlus());
        getSession().setUserCountryCode(mCcp.getSelectedCountryCode());
        getSession().setEmailId(etEmailId.getText().toString().trim());
        if(mBloodGroup.getSelectedItemPosition() > 0)
            getSession().setBlood(mBloodGroup.getSelectedItemPosition());
        else
            getSession().setBlood(-1);
        getSession().setToastMessage(mDetailSaved);
        if(getSession().getLanguage().endsWith(MR_IN)) {
            getAppActivity().createUserProfileRecordingsUsingTTS();
        }
        FirebaseDatabase mDB = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mDB.getReference(BuildConfig.DB_TYPE+"/users/"+getSession().getUserId());
        mRef.child("updatedOn").setValue(ServerValue.TIMESTAMP);
        mRef.child("versionCode").setValue(BuildConfig.VERSION_CODE);
        requireActivity().onBackPressed();
    }
}
