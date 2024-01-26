/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zonetech.online.notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;


import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ZTFirebaseMessagingService extends FirebaseMessagingService {
    public static String TAG = "ZTFMS";
    public static final String channel_id = "default";
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage != null){
            Map<String, String> data = remoteMessage.getData();
            scheduleJobs(data);
        }
    }

    private void scheduleJobs(Map<String, String> data){
        if(data != null && data.size() > 0){
            try {
                Log.i(TAG, "data = " + data);
                Data.Builder inputBuilder = new Data.Builder();
                Set set = data.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry mEntry = (Map.Entry) iterator.next();
                    inputBuilder.putString((String) mEntry.getKey(), (String) mEntry.getValue());
                }
                Data input = inputBuilder.build();
                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificationScheduledWorker.class)
                                .setInputData(input)
                                .build();
                WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                workManager.enqueue(request).getResult().get();
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendRegistrationToServer(String token) {
        try {
            Log.i(TAG, "token = "+token);
            String oldToken = Preferences.get(getApplicationContext(), Preferences.KEY_USER_TOKEN, "");
            if(!token.equalsIgnoreCase(oldToken) && Utils.isValidString(token)){
                Preferences.put(getApplicationContext(), Preferences.KEY_USER_TOKEN, token);
                Preferences.put(getApplicationContext(), Preferences.KEY_USER_TOKEN_SAVED, false);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    public static void saveToken(Context context){
        String token = Preferences.get(context, Preferences.KEY_USER_TOKEN, "");
        if(!(Utils.isValidString(token) && !Preferences.get(context, Preferences.KEY_USER_TOKEN_SAVED, false))){
            return;
        }
        JSONObject params = new JSONObject();
        try{
            params.put("DeviceID", token);
            params.put("DeviceType", "Android");
            params.put("StudentID", Utils.getStudentId(context));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL,"SaveDeviceDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                Log.i(TAG, "response = "+response);
                Preferences.put(context, Preferences.KEY_USER_TOKEN_SAVED, true);
            }

            @Override
            public void error(String error) {
                Log.i(TAG, "error = "+error);
            }
        });
    }
}