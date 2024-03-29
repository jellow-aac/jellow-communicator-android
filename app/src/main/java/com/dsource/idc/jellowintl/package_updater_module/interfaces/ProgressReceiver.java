package com.dsource.idc.jellowintl.package_updater_module.interfaces;

public interface ProgressReceiver {
    void onDownloadProgress(int completedDownloads,int totalDownloads);

    void onIconDownloadTaskCompleted(boolean success);

    void updateStatusText(String message);

    void showUpdateInfo(String message);

    void iconsModified(boolean modified);
}
