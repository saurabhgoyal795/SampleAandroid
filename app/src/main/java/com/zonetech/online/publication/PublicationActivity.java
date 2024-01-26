package com.zonetech.online.publication;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;

import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineClass.ClassVideoListActivity;
import com.zonetech.online.offers.OfferActivity;
import com.zonetech.online.utils.Utils;

public class PublicationActivity extends ZTAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publication);
        PublicationFragment homeFragment = new PublicationFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    onBackPressed();
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(PublicationActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(PublicationActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(PublicationActivity.this);
                }
                return false;
            }
        });
    }
}