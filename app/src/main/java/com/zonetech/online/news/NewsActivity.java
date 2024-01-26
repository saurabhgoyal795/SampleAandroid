package com.zonetech.online.news;

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
import com.zonetech.online.mypackage.onlineTestSeries.TestSeriesDetailsActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.utils.Utils;

public class NewsActivity extends ZTAppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        NewsFragment homeFragment = new NewsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(NewsActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(NewsActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openDownloadsNewTask(NewsActivity.this);
                        break;
                }
                return false;
            }
        });
    }
}