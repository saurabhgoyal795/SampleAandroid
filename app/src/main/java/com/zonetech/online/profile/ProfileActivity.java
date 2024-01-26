package com.zonetech.online.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.news.NewsActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class ProfileActivity extends ZTAppCompatActivity {
    JSONObject profileJson = new JSONObject();
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
         viewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
            params.put("StudentId",  Utils.getStudentId(getApplicationContext()));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(getApplicationContext(), ServerApi.TESTING_BASE_URL,"studentprofile", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if(Utils.isActivityDestroyed(ProfileActivity.this)){
                    return;
                }
                Utils.saveObject(getApplicationContext(), response.toString(), "profile");
                setProfileJSON(response);
                SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(ProfileActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if(Utils.isActivityDestroyed(ProfileActivity.this)){
                    return;
                }
            }
        });
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.getMenu().getItem( 3).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    onBackPressed();
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(ProfileActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(ProfileActivity.this, 1);
                }
                return false;
            }
        });
    }

    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getApplicationContext(), "profile");
            if(Utils.isValidString(value)){
                JSONObject response = new JSONObject(value);
                setProfileJSON(response);
                SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(ProfileActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Exception e) {}
    }

    public void setProfileJSON(JSONObject json){
        profileJson = json;
        if (additionalProfileFragment != null){
            additionalProfileFragment.profileJSON = profileJson.optJSONObject("Body").optJSONObject("profile");
        }
    }

    public JSONObject getProfileJSON() {
        return profileJson;
    }

    ProfileFragment profileFragment;
    AdditionalProfileFragment additionalProfileFragment;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        @StringRes
        private  final int[] TAB_TITLES = new int[]{R.string.tab_personal_details, R.string.tab_additional_details};
        private final Context mContext;


        public SectionsPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0 ){
                profileFragment = new ProfileFragment();
                Bundle args = new Bundle();
                args.putString("response", getProfileJSON().toString());
                profileFragment.setArguments(args);
                return profileFragment;
            } else {
                additionalProfileFragment = new AdditionalProfileFragment();
                Bundle args = new Bundle();
                args.putString("response", getProfileJSON().toString());
                additionalProfileFragment.setArguments(args);
                return additionalProfileFragment;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getResources().getString(TAB_TITLES[position]);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}