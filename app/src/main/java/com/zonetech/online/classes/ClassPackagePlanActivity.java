package com.zonetech.online.classes;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.PopupMenu;

import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.MyPackageActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.AnimationListener;

import org.json.JSONObject;

import java.util.HashMap;

public class ClassPackagePlanActivity extends ZTAppCompatActivity {
    BottomNavigationView navView;
    ClassPackagePlanFragment homeFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_series);
        navView = findViewById(R.id.navigation);
        homeFragment = new ClassPackagePlanFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragment).commitAllowingStateLoss();
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(ClassPackagePlanActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(ClassPackagePlanActivity.this,1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openDownloadsNewTask(ClassPackagePlanActivity.this);
                        break;
                }
                return false;
            }
        });
        if(Utils.filterOptions == null || Utils.filterOptions.size() == 0) {
            Utils.getCourseSpec(this, new CompleteListener() {
                @Override
                public void success(JSONObject response) {
                }

                @Override
                public void error(String error) {

                }
            });

        }
    }


    public void setFilterOptions(){
        if(defaultMenu != null){
            defaultMenu.getItem(0).setVisible(true);
        }
    }
    Menu defaultMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_menu, menu);
        defaultMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                showMenuOptions(findViewById(R.id.filter));
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }
    private void showMenuOptions(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.filter_option_menu, popup.getMenu());
        int index = 1;
        for (int i = 0 ; i < Utils.filterOptions.size() ; i++){
            HashMap<String, Object> item = Utils.filterOptions.get(i);
            if(homeFragment.courseIds.contains((Integer) item.get("courseId"))){
                popup.getMenu().add(0, (Integer) item.get("courseId"), index, (String)item.get("courseName"));
                index++;
            }
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.all){
                    homeFragment.setFliter(-1);
                }else{
                    homeFragment.setFliter(item.getItemId());
                }
                return true;
            }
        });

        if (!Utils.isActivityDestroyed(this)) {
            popup.show();
        }
    }
}