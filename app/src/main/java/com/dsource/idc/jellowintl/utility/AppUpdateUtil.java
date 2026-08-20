package com.dsource.idc.jellowintl.utility;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import android.content.IntentSender;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.dsource.idc.jellowintl.R;
import com.dsource.idc.jellowintl.activities.BaseActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class AppUpdateUtil{
    public final static int UPDATE_REQUEST_CODE = 99;
    private AppUpdateManager appUpdateManager;
    private AppUpdateInfo appUpdateInfo;

    public interface AppUpdateCallback {
        void continueLoadingTheApp();
    }

    public enum UpdateStatus {
        INIT, SHOW_RATIONALE, START, RUNNING, CANCELED, FAILED, NOT_AVAILABLE
    }

    public void executeUpdateFlow(UpdateStatus status, BaseActivity context, AppUpdateCallback callback){
            Log.i("JellowApp","Created appUpdateManager");
        switch(status){
            case INIT:
                checkIfNewVersionAvailable(context, callback);
                break;
            case SHOW_RATIONALE:
                showUpdateRationaleToUser(context, callback);
                break;
            case START:
                startUpdateProcess(context, callback);
                break;
            case RUNNING:
                callUpdateUIToForegroundIfRunning(context, callback);
                break;
            case CANCELED:
            case FAILED:
            case NOT_AVAILABLE:
                callback.continueLoadingTheApp();
                break;
        }
    }

    public void checkIfNewVersionAvailable(final BaseActivity context, final AppUpdateCallback callback) {
        appUpdateManager = AppUpdateManagerFactory.create(context);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            AppUpdateUtil.this.appUpdateInfo = appUpdateInfo;
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
                executeUpdateFlow(UpdateStatus.SHOW_RATIONALE, context, callback);
            }else{
                executeUpdateFlow(UpdateStatus.FAILED, context, callback);
            }
        }).addOnFailureListener(e -> executeUpdateFlow(UpdateStatus.FAILED, context, callback));
    }

    private void showUpdateRationaleToUser(final BaseActivity context, final AppUpdateCallback callback) {
        String updateNow = context.getString(R.string.update_now);
        String updateLater = context.getString(R.string.update_later);
        String message = context.getString(R.string.app_update_message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Add the buttons
        builder
            .setPositiveButton(updateNow, (dialog, id) -> {
                executeUpdateFlow(UpdateStatus.START, context, callback);
                dialog.dismiss();
            })
            .setNegativeButton(updateLater, (dialog, i) -> {
                executeUpdateFlow(UpdateStatus.CANCELED, context, callback);
                dialog.dismiss();
            })
            // Set other dialog properties
            .setCancelable(true)
            .setMessage(message);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        // Show the AlertDialog
        dialog.show();
        dialog.setOnCancelListener(dialog1 -> {
            executeUpdateFlow(UpdateStatus.CANCELED, context, callback);
            dialog1.dismiss();
        });
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(context.getResources().getColor(R.color.colorAccent));
            positiveButton.setTextSize(18f);
            context.applyMonochromeColor(positiveButton);
        }
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(context.getResources().getColor(R.color.colorAccent));
            negativeButton.setTextSize(18f);
            context.applyMonochromeColor(negativeButton);
        }
    }

    public void startUpdateProcess(BaseActivity context, AppUpdateCallback callback){
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    context,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                     UPDATE_REQUEST_CODE);
        } catch (IntentSender.SendIntentException | NullPointerException e) {
            executeUpdateFlow(UpdateStatus.FAILED, context, callback);
            e.printStackTrace();
        }
    }

    public void callUpdateUIToForegroundIfRunning(final BaseActivity context, final AppUpdateCallback callback) {
        appUpdateManager = AppUpdateManagerFactory.create(context);
        appUpdateManager
            .getAppUpdateInfo()
            .addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability()
                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                context,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                                UPDATE_REQUEST_CODE
                        );
                    } catch (IntentSender.SendIntentException e) {
                        executeUpdateFlow(UpdateStatus.FAILED, context, callback);
                        e.printStackTrace();
                    }
                }
            });
    }
}