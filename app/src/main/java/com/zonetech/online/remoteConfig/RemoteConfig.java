package com.zonetech.online.remoteConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.zonetech.online.R;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RemoteConfig {
    public static final String TAG = "RemoteConfig";
    public static void fetchRemoteConfig(Activity context){
        try {
            FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(Utils.isDebugModeOn ? 0 : 3600)
                    .build();
            mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
            mFirebaseRemoteConfig.fetchAndActivate()
                    .addOnCompleteListener(context, new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            try {
                                if (task.isSuccessful()) {
                                    if (Utils.isActivityDestroyed(context)) {
                                        return;
                                    }
                                    boolean isUpdated = task.getResult();
                                    Log.i(TAG, "isUpdated = " + isUpdated);
                                    if (isUpdated) {
                                        fetchTaggedInformation(mFirebaseRemoteConfig, context.getApplicationContext());
                                    }
                                }
                            }catch (Exception e){
                                if(Utils.isDebugModeOn){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
    private static void fetchTaggedInformation(FirebaseRemoteConfig mFirebaseRemoteConfig, Context context){
        Preferences.put(context, Preferences.KEY_INAPP_UPDATE_PRIORITY, Integer.parseInt(mFirebaseRemoteConfig.getString("releasePriority")));
        Preferences.put(context, Preferences.KEY_IS_INAPPUPDATE_ENABLED, Integer.parseInt(mFirebaseRemoteConfig.getString("inAppUpdate")));
        Preferences.put(context, Preferences.KEY_IS_CUSTOM_EXTRACTOR_ENABLED, "true".equalsIgnoreCase(mFirebaseRemoteConfig.getString("customExtractorEnabled")));
        Preferences.put(context, Preferences.KEY_CUSTOM_EXTRACTOR_URL, mFirebaseRemoteConfig.getString("customExtractorBaseUrl"));
        if(!"".equalsIgnoreCase(mFirebaseRemoteConfig.getString("webPlayerEnabled"))){
            Preferences.put(context, Preferences.KEY_IS_WEB_PLAYER_ENABLED, "true".equalsIgnoreCase(mFirebaseRemoteConfig.getString("webPlayerEnabled")));
        }
        Preferences.put(context, Preferences.KEY_FORCE_UPDATE, mFirebaseRemoteConfig.getString("forceUpdate"));
        Preferences.put(context, Preferences.KEY_VIMEO_TOKENS, mFirebaseRemoteConfig.getString("vimeoTokens"));
        Log.i(TAG, "webPlayerEnabled = "+mFirebaseRemoteConfig.getString("webPlayerEnabled"));
        Log.i(TAG, "releasePriority = "+mFirebaseRemoteConfig.getString("releasePriority"));
        Log.i(TAG, "inAppUpdate = "+mFirebaseRemoteConfig.getString("inAppUpdate"));
        Log.i(TAG, "customExtractorEnabled = "+mFirebaseRemoteConfig.getString("customExtractorEnabled"));
        Log.i(TAG, "customExtractorBaseUrl = "+mFirebaseRemoteConfig.getString("customExtractorBaseUrl"));
        Log.i(TAG, "forceUpdate = "+mFirebaseRemoteConfig.getString("forceUpdate"));
        Log.i(TAG, "vimeotokens"+mFirebaseRemoteConfig.getString("vimeoTokens"));
    }
}
