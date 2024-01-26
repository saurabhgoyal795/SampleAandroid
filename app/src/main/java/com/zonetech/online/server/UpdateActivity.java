package com.zonetech.online.server;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.utils.Utils;

public class UpdateActivity extends ZTAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_force_update_activity);

        findViewById(R.id.updateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://"
                            + "details?id=" + getPackageName())));
                } catch (ActivityNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://"
                                + "play.google.com/store/apps/details?id=" + getPackageName())));
                    }catch (ActivityNotFoundException e2){
                        if(Utils.isDebugModeOn){
                            e2.printStackTrace();
                        }
                        Toast.makeText(getApplicationContext(), "play store not exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if("soft".equalsIgnoreCase(Utils.updateType(this))){
            super.onBackPressed();
        }else {
            return;
        }
    }
}
