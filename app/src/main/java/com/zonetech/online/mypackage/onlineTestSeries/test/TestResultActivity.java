package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;

public class TestResultActivity extends ZTAppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);
        Bundle bundle = getIntent().getExtras();
        setTitle(bundle.getString("title"));
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentTestCompleted testCompletedFragment = new FragmentTestCompleted();
        testCompletedFragment.setArguments(getIntent().getExtras());
        fragmentTransaction.replace(R.id.container, testCompletedFragment).commitAllowingStateLoss();
    }
}
