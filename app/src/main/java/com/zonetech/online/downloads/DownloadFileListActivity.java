package com.zonetech.online.downloads;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineClass.FragmentVideoList;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class DownloadFileListActivity extends ZTAppCompatActivity {
    private DownloadAdapter downloadAdapter;
    private HashMap<String, HashMap<String, Object>> videoDownloadData = new HashMap();
    public JSONArray videoListDownload;
    public String folderName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_downloads);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(DownloadFileListActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(DownloadFileListActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                         onBackPressed();
                         break;
                }
                return false;
            }
        });
        Bundle bundle = getIntent().getExtras();
        folderName = bundle.getString("folderName");
        String title = bundle.getString("title");
        setTitle(title);
        if("Videos".equalsIgnoreCase(folderName)) {
            getVideoDownloadData(folderName);
        }else{
            getDownloadFiles(folderName);
        }
    }

    public void getDownloadFiles(String folderName){
        String basePath = getFilesDir() + "/"+folderName+"/";
        File file = new File(basePath);
        if(file.exists()){
            File[] files = file.listFiles();
            if(files == null || files.length == 0){
                findViewById(R.id.noDownloads).setVisibility(View.VISIBLE);
                findViewById(R.id.downloadList).setVisibility(View.GONE);
            }else{
                if(downloadAdapter == null){
                    downloadAdapter = new DownloadAdapter(this, files, folderName);
                    RecyclerView downloadList = findViewById(R.id.downloadList);
                    downloadList.setLayoutManager(new LinearLayoutManager(this));
                    downloadList.setAdapter(downloadAdapter);
                }else{
                    downloadAdapter.refreshValues(files);
                }
            }
        }else{
            findViewById(R.id.noDownloads).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }
    private void getVideoDownloadData(String folderName){
        try{
            videoDownloadData = (HashMap<String, HashMap<String, Object>>)Utils.getObject(getApplicationContext(), "videoDownloadData");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(videoDownloadData == null){
            videoDownloadData = new HashMap<>();
        }
        videoListDownload = new JSONArray();
        String basePath = getFilesDir() + "/"+folderName+"/";
        File file = new File(basePath);
        if(file.exists()){
            File[] files = file.listFiles();
            if(files != null){
                for (File videoFile : files){
                    String fileName = videoFile.getName();
                    String videoId = fileName.replace(".mp4", "");
                    JSONObject videoJson = getVideoJSON(videoId);
                    if(videoJson != null){
                        videoListDownload.put(videoJson);
                    }
                }
            }
        }
        if(videoListDownload == null || videoListDownload.length() == 0){
            findViewById(R.id.noDownloads).setVisibility(View.VISIBLE);
            findViewById(R.id.downloadList).setVisibility(View.GONE);
            findViewById(R.id.videoListContainer).setVisibility(View.GONE);
        }else{
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FragmentVideoList fragmentVideoList = new FragmentVideoList();
            Bundle bundle = new Bundle();
            bundle.putInt("type", 1);
            fragmentVideoList.setArguments(bundle);
            findViewById(R.id.videoListContainer).setVisibility(View.VISIBLE);
            fragmentTransaction.replace(R.id.videoListContainer, fragmentVideoList).commitAllowingStateLoss();
        }
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    public JSONObject getVideoJSON(String videoId){
        try {
            if (videoDownloadData != null && videoDownloadData.get(videoId) != null) {
                String videoJson = (String) videoDownloadData.get(videoId).get("videoJson");
                if (Utils.isValidString(videoJson)) {
                    return new JSONObject(videoJson);
                }
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deleteVideo(String videoId, boolean isCancel){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File videoFile = new File(getFilesDir() + "/Videos/" + videoId+".mp4");
                    videoFile.delete();
                    deleteTitle(videoId);
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), isCancel ? "Download cancelled":"Video file deleted", Toast.LENGTH_SHORT).show();
                        getVideoDownloadData(folderName);
                    }
                });
            }
        }).start();
    }
    private void deleteTitle(String videoId){
        try{
            HashMap<String, HashMap<String, Object>> videoData = (HashMap<String, HashMap<String, Object>>)Utils.getObject(getApplicationContext(), "videoDownloadData");
            videoData.remove(videoId);
            Utils.saveObject(getApplicationContext(), videoData, "videoDownloadData");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
}
