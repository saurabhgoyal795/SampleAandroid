package com.zonetech.online;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.home.MainActivity;
import com.zonetech.online.login.LoginActivity;
import com.zonetech.online.notification.NotificationScheduledWorker;
import com.zonetech.online.server.DeepLinkActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

import java.util.Calendar;

public class SplashActivity extends ZTAppCompatActivity {
    String link = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        link = checkForNotification();
        setContentView(R.layout.activity_splash);
        BaseApplication.isShownAlert = false;
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                launchLoginScreen();
            }
        }, 5000);
    }

    private void launchLoginScreen(){
        if(Utils.isValidString(link) && !"https://zonetech.in".equalsIgnoreCase(link)){
            Intent intent = new Intent(SplashActivity.this, DeepLinkActivity.class);
            intent.putExtra("url", link);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, Utils.isLoginCompleted(this) ? MainActivity.class : LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private String checkForNotification(){
        Bundle bundle = getIntent().getExtras();
        if(Utils.isLoginCompleted(this) && bundle != null && bundle.containsKey(NotificationScheduledWorker.NOTIFICATION_LINK)){
            try{
                return bundle.getString(NotificationScheduledWorker.NOTIFICATION_LINK);
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
