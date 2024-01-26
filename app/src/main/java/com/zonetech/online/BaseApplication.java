package com.zonetech.online;

import android.app.Application;
import android.util.Log;

import com.zonetech.online.payment.AppEnvironment;

/**
 * Created by Rahul Hooda on 14/7/17.
 */
public class BaseApplication extends Application {
    public static boolean isShownAlert = false;
    AppEnvironment appEnvironment;

    @Override
    public void onCreate() {
        super.onCreate();
        appEnvironment = AppEnvironment.PRODUCTION;
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }
}
