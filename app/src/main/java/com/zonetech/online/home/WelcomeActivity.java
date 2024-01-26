package com.zonetech.online.home;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zonetech.online.R;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        String message = MainActivity.msg;
        if(message == null){
            finish();
            return;
        }
        Bundle bundle = getIntent().getExtras();
        String titleText = "";
        if(bundle != null){
            titleText = bundle.getString("title", "");
        }
        boolean isNewUser = Preferences.get(getApplicationContext(), Preferences.KEY_IS_NEW_USER, false);
        TextView title = findViewById(R.id.title);
        title.setVisibility(View.GONE);
        findViewById(R.id.message).setVisibility(View.GONE);
        if(Utils.isValidString(titleText)){
            title.setText(titleText);
        }else{
            if(isNewUser) {
                title.setText(String.format(getString(R.string.welcome_text), Utils.getName(this)));
            }else{
                title.setText(String.format(getString(R.string.welcome_back), Utils.getName(this)));
            }
        }
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.rootView).setOnClickListener(new View.OnClickListener() {
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
        ZTWebView webviewMsg = findViewById(R.id.webviewMsg);
        webviewMsg.setVisibility(View.VISIBLE);
        message = message.replaceAll("<img ", "<img style=\"max-width:100%\"");
        String head1 = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
        String newHtml = head1 + message + "</body></html>";
        webviewMsg.loadDataWithBaseURL(null, newHtml, "text/html", "UTF-8", null);
        Preferences.put(getApplicationContext(), Preferences.KEY_IS_WELCOME_MSG_SHOWN, true);
    }
}
