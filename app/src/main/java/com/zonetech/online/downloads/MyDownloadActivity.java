package com.zonetech.online.downloads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.freecourses.FreeClassPackageDetailsActivity;
import com.zonetech.online.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MyDownloadActivity extends ZTAppCompatActivity {
    public static final String REFRESH_DOWNLOAD_LIST = "download.file.refresh";
    private DownloadFolderAdapter downloadFolderAdapter;
    private String[] folders = {"SubjectPdf"};
    private ArrayList<HashMap<String, Object>> folderList;
    private BroadcastReceiver event = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getDownloadFiles();
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_downloads);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    onBackPressed();
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(MyDownloadActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(MyDownloadActivity.this, 1);
                }
                return false;
            }
        });
        getDownloadFiles();
        LocalBroadcastManager.getInstance(this).registerReceiver(event, new IntentFilter(REFRESH_DOWNLOAD_LIST));
    }

    public void getDownloadFiles(){
        folderList = new ArrayList<>();
        for (int i = 0 ; i < folders.length ; i++){
            String basePath = getFilesDir() + "/"+folders[i]+"/";
            File file = new File(basePath);
            if(file.exists()){
                File[] files = file.listFiles();
                if(files != null && files.length > 0){
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("folderName", folders[i]);
                    if("SubjectPdf".equalsIgnoreCase(folders[i])){
                        item.put("title", "PDF Notes");
                    }else{
                        item.put("title", "Video lectures");
                    }
                    item.put("size", files.length);
                    folderList.add(item);
                }
            }
        }
        if(folderList.size() > 0){
            if(downloadFolderAdapter == null){
                downloadFolderAdapter = new DownloadFolderAdapter(this, folderList);
                RecyclerView downloadList = findViewById(R.id.downloadList);
                downloadList.setLayoutManager(new LinearLayoutManager(this));
                downloadList.setAdapter(downloadFolderAdapter);
            }else{
                downloadFolderAdapter.refreshValues(folderList);
            }
        }else{
            findViewById(R.id.noDownloads).setVisibility(View.VISIBLE);
            findViewById(R.id.downloadList).setVisibility(View.GONE);
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(event);
    }
}
