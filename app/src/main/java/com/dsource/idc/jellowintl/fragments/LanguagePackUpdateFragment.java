package com.dsource.idc.jellowintl.fragments;

import static com.dsource.idc.jellowintl.utility.Analytics.isAnalyticsActive;
import static com.dsource.idc.jellowintl.utility.Analytics.resetAnalytics;
import static com.dsource.idc.jellowintl.utility.Analytics.startMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.stopMeasuring;
import static com.dsource.idc.jellowintl.utility.Analytics.validatePushId;
import static android.content.Context.ACCESSIBILITY_SERVICE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.AppActivity;
import com.dsource.idc.jellowintl.models.GlobalConstants;
import com.dsource.idc.jellowintl.package_updater_module.ConnectionUtils;
import com.dsource.idc.jellowintl.package_updater_module.UpdateManager;
import com.dsource.idc.jellowintl.package_updater_module.interfaces.ProgressReceiver;
import com.dsource.idc.jellowintl.utility.SessionManager;

public class LanguagePackUpdateFragment extends Fragment implements ProgressReceiver{

    UpdateManager updateManager;
    RoundCornerProgressBar progressBar;
    TextView statusText;
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
        return inflater.inflate(R.layout.activity_language_pack_update_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAppActivity().setVisibleAct(LanguagePackUpdateFragment.class.getSimpleName());
        getAppActivity().applyMonochromeColor();
        updateManager = new UpdateManager(requireContext());
        statusText = view.findViewById(R.id.status);
        progressBar = view.findViewById(R.id.pg);
        progressBar.setMax(1);
        
        if (getAppActivity().isAccessibilityTalkBackOn((AccessibilityManager) requireContext().getSystemService(ACCESSIBILITY_SERVICE))) {
            view.findViewById(R.id.btnClose).setVisibility(View.VISIBLE);
        }
        
        view.findViewById(R.id.btnClose).setOnClickListener(v -> closeActivity());
        
        if(ConnectionUtils.isConnected(requireContext())){
            updateManager.startDownload();
        } else {
            statusText.setText(getString(R.string.checkConnectivity));
            statusText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
        }
        
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closeActivity();
            }
        });
    }

    @Override
    public void onDownloadProgress(int completedDownloads, int totalDownloads) {
        progressBar.setProgress((float)completedDownloads/totalDownloads);
    }

    @Override
    public void onIconDownloadTaskCompleted(boolean success) {}

    @Override
    public void updateStatusText(String message) {
        statusText.setText(message);
        statusText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
    }

    @Override
    public void showUpdateInfo(String message) {
        showToast(message);
    }

    @Override
    public void iconsModified(boolean modified) {
        if(modified){
            for (String language :
                    SessionManager.LangValueMap.keySet()) {
                getSession().setLanguageDataUpdateState(language,
                        GlobalConstants.LANGUAGE_STATE_CREATE_DB);
            }
            startActivity(new Intent(requireContext(), AppActivity.class));
            requireActivity().finishAffinity();
        }else{
            closeActivity();
        }
    }

    private void showToast(String message){
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(!isAnalyticsActive()){
            resetAnalytics(requireContext(), getSession().getCaregiverNumber().substring(1));
        }
        startMeasuring();
    }

    @Override
    public void onPause() {
        super.onPause();
        long sessionTime = validatePushId(getSession().getSessionCreatedAt());
        getSession().setSessionCreatedAt(sessionTime);
        stopMeasuring("LanguagePackUpdateActivity");
    }

    public void closeActivity(){
        if (!NavHostFragment.findNavController(LanguagePackUpdateFragment.this).popBackStack()) {
            NavHostFragment.findNavController(LanguagePackUpdateFragment.this).navigate(R.id.levelOneFragment);
        }
    }
}
