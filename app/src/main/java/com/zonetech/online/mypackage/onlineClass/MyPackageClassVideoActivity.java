package com.zonetech.online.mypackage.onlineClass;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class MyPackageClassVideoActivity extends ZTAppCompatActivity {
    private JSONObject itemObj;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeigth;
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private String packageName;
    private MyPackageClassVideoItemAdapter adapter = null;
    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_package_class_video_detail);
        metrics = Utils.getMetrics(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                setTitle(itemObj.optString("SubjectName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        mRecyclerView = findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        setViews();
        navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(1).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        Utils.openHome(MyPackageClassVideoActivity.this);
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(MyPackageClassVideoActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(MyPackageClassVideoActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openDownloadsNewTask(MyPackageClassVideoActivity.this);
                        break;
                }
                return false;
            }
        });
    }

    private void setViews(){
        if(adapter == null) {
            adapter = new MyPackageClassVideoItemAdapter(itemObj.optJSONArray("SubjectTopicList"), MyPackageClassVideoActivity.this);
            mRecyclerView.setAdapter(adapter);
        }else{
            adapter.refreshAdapter(itemObj.optJSONArray("SubjectTopicList"));
        }
    }


}