package com.zonetech.online.mypackage;

import android.os.Build;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.remoteConfig.RemoteConfig;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.AnimationListener;

public class MyPackageActivity extends ZTAppCompatActivity {
    BottomNavigationView navView;
    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_my_package);
        navView = findViewById(R.id.navigation);
        TabLayout tabs = findViewById(R.id.tabs);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        tabs.setupWithViewPager(viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);
        if(bundle != null){
            position = bundle.getInt("position");
            viewPager.setCurrentItem(position);
        }
        if(Utils.isLollipop()) {
            findViewById(R.id.contentView).setTransitionName("my_packages");
        }
        navView.getMenu().getItem(position + 1).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.navigation_test:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openDownloadsNewTask(MyPackageActivity.this);
                        break;
                }
                return false;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                navView.getMenu().getItem(position + 1).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        for(int i=0; i < tabs.getTabCount(); i++) {
            View tab = ((ViewGroup) tabs.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(0, 0, 8, 0);
            tab.requestLayout();
        }
    }

    @Override
    public void finishAfterTransition() {
        if(Utils.isLollipop()) {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setDuration(500);
            fadeOut.setAnimationListener(new AnimationListener(){
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    findViewById(R.id.tabs).clearAnimation();
                    findViewById(R.id.tabs).setVisibility(View.INVISIBLE);
                    findViewById(R.id.view_pager).setVisibility(View.INVISIBLE);
                }
            });
            findViewById(R.id.tabs).startAnimation(fadeOut);
            findViewById(R.id.view_pager).startAnimation(fadeOut);
        }
        super.finishAfterTransition();

    }

    @Override
    protected void onStart() {
        super.onStart();
        RemoteConfig.fetchRemoteConfig(this);
    }
}