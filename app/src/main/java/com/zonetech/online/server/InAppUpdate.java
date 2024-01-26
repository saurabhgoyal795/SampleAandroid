package com.zonetech.online.server;

import android.app.Activity;
import android.util.Log;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.Utils;

public class InAppUpdate {
    public static final int MY_REQUEST_CODE = 4;
    public interface AppListener {
        void updateDownloading(int max, int progress);
        void updateCompleted();
        void updateNotAvailable();
    }
    public static void checkForAppUpdate(final Activity context, AppListener appListener) {
        try {
            if(0 == Preferences.get(context, Preferences.KEY_IS_INAPPUPDATE_ENABLED, 1)){
                return;
            }
            int priority = Preferences.get(context, Preferences.KEY_INAPP_UPDATE_PRIORITY, AppUpdateType.IMMEDIATE);
            AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
            InstallStateUpdatedListener listener = new InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState installState) {
                    try {
                        Log.i("InAppUpdateTesting", "checkForAppUpdate installState.installStatus() = " + installState.installStatus());
                        if (installState.installStatus() == InstallStatus.DOWNLOADING || installState.installStatus() == InstallStatus.PENDING) {
                            long bytesDownloaded = installState.bytesDownloaded();
                            long totalBytesToDownload = installState.totalBytesToDownload();
                            if (appListener != null) {
                                appListener.updateDownloading((int) totalBytesToDownload, (int) bytesDownloaded);
                            }
                        } else if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                            Log.i("InAppUpdateTesting", "checkForAppUpdate InstallStatus.DOWNLOADED = " + InstallStatus.DOWNLOADED);
                            Utils.showToast(context, "Updated downloaded");
                            appUpdateManager.completeUpdate();
                            if (appListener != null) {
                                appListener.updateCompleted();
                            }
                        }
                    }catch (Exception e){
                        if(Utils.isDebugModeOn){
                            e.printStackTrace();
                        }
                    }
                }
            };

            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                @Override
                public void onSuccess(AppUpdateInfo result) {
                    try {
                        Log.i("InAppUpdateTesting", "checkForAppUpdate result = " + result);
                        Log.i("InAppUpdateTesting", "checkForAppUpdate updateAvailability = " + result.updateAvailability());
                        Log.i("InAppUpdateTesting", "checkForAppUpdate isUpdateTypeAllowed immediate = " + result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE));
                        Log.i("InAppUpdateTesting", "checkForAppUpdate isUpdateTypeAllowed flexible = " + result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE));
                        Log.i("InAppUpdateTesting", "checkForAppUpdate version = " + result.availableVersionCode());
                        if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                || result.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            if (priority == 1 && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                                appUpdateManager.startUpdateFlowForResult(
                                        result,
                                        AppUpdateType.IMMEDIATE,
                                        context,
                                        MY_REQUEST_CODE);
                            } else {
                                appUpdateManager.registerListener(listener);
                                appUpdateManager.startUpdateFlowForResult(
                                        result,
                                        AppUpdateType.FLEXIBLE,
                                        context,
                                        MY_REQUEST_CODE);
                            }
                        } else {
                            if (appListener != null) {
                                appListener.updateNotAvailable();
                            }
                        }
                    } catch (Exception e) {
                        if (Utils.isDebugModeOn) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }
}
