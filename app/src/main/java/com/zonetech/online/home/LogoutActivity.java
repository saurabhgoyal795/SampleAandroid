package com.zonetech.online.home;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.login.LoginActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class LogoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        TextView title = findViewById(R.id.title);
        title.setText("LogOut");
        TextView okButton = findViewById(R.id.ok);
        ((TextView)findViewById(R.id.message)).setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        okButton.setBackgroundResource(R.drawable.button_red_rounded_4dp);
        findViewById(R.id.cancel).setVisibility(View.VISIBLE);
        okButton.setText("Logout");
        ((TextView)findViewById(R.id.message)).setText(getString(R.string.logout_msg));
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        findViewById(R.id.rootView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.popupView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        Preferences.put(getApplicationContext(), Preferences.KEY_IS_WELCOME_MSG_SHOWN, true);
    }
    private void logout(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        String deviceType = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        ServerApi.logoutDevice(this, Utils.getStudentId(this), deviceType, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                resetValues();
            }

            @Override
            public void error(String error) {

            }
        });
    }

    public void resetValues(){
        Preferences.remove(this, Preferences.KEY_STUDENT_ID);
        Preferences.remove(this, Preferences.KEY_SPEC_ID);
        Preferences.remove(this, Preferences.KEY_COURSE_ID);
        Preferences.remove(this, Preferences.KEY_STUDENT_CODE);
        Preferences.remove(this, Preferences.KEY_STUDENT_NAME);
        Preferences.remove(this, Preferences.KEY_STUDENT_EMAIL);
        Preferences.remove(this, Preferences.KEY_STUDENT_PHONE);
        Preferences.remove(this, Preferences.KEY_COURSE_NAME);
        Preferences.remove(this, Preferences.KEY_SPEC_NAME);
        Preferences.remove(this, Preferences.KEY_STUDENT_ROLLNO);
        Preferences.remove(this, Preferences.KEY_STUDENT_PROFILE_PIC);
        Preferences.remove(this, Preferences.KEY_IS_LOGIN_COMPLTED);
        Preferences.remove(this, Preferences.KEY_CART_COUNT);
        Preferences.remove(this, Preferences.KEY_IS_WELCOME_MSG_SHOWN);
        Preferences.remove(this, Preferences.KEY_IS_NEW_USER);

        Utils.deleteObject(this, "homenews");
        Utils.deleteObject(this, "packages_0");
        Utils.deleteObject(this, "OnlineClassPackages_0");
        Utils.deleteObject(this, "homeAchivements");
        Utils.deleteObject(this, "notifications");
        Utils.deleteObject(this, "videoImages");
        Utils.deleteObject(this, "profile");
        Utils.deleteObject(this, "books");
        Utils.deleteObject(this, "seriesDetails");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(LogoutActivity.this).clearDiskCache();
            }
        }).start();
        Glide.get(LogoutActivity.this).clearMemory();
        Toast.makeText(getApplicationContext(), "You have successfully logged out!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
