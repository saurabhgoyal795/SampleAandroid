package com.zonetech.online.mypackage.onlineClass;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.player.PlayerActivity;
import com.zonetech.online.player.VimeoPlayer;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.DownloadContent;
import com.zonetech.online.server.DownloadVimeoContent;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import at.huber.youtubeExtractor.ZTYouTubeExtractor;

public class ClassVideoListActivity extends ZTAppCompatActivity{
    public JSONArray videoList;
    public JSONArray videoListDownload;
    BottomNavigationView navView;
    TabLayout tabs;
    VideoPagerAdapter videoPagerAdapter;
    public static final String DOWNLOAD_REQUEST = "zonetech.download.listener";
    private BroadcastReceiver event = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            Log.i("DownloadTesting", "type ="+type);
            if("start".equalsIgnoreCase(type)){
                downloadVideoId = intent.getStringExtra("videoId");
                downloadText = "Downloading...";
                setList();
            }else if("progress".equalsIgnoreCase(type)){
                downloadVideoId = intent.getStringExtra("videoId");
                int progress = intent.getIntExtra("progress", 0);
                int max = intent.getIntExtra("max", 100);
                Log.i("DownloadTesting", "downloadvide0Id ="+downloadVideoId);
                Log.i("DownloadTesting", "progress ="+progress);
                Log.i("DownloadTesting", "max ="+max);
                if(!("Downloading("+progress+" MB / "+max+" MB)").equalsIgnoreCase(downloadText)) {
                    downloadText = "Downloading(" + progress + " MB / " + max + " MB)";
                    setList();
                }
            }else if("finish".equalsIgnoreCase(type)){
                Utils.showToast(getApplicationContext(), "Video downloaded");
                setList();
            }else if("error".equalsIgnoreCase(type)){
                Utils.showToast(getApplicationContext(), intent.getStringExtra("error"));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_package_class_video_list);
        tabs = findViewById(R.id.tabs);
        try {
            Bundle bundle = getIntent().getExtras();
            videoList = new JSONArray(bundle.getString("videoList"));
            String title = bundle.getString("title");
            setTitle(title);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        setList();
        navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(1).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    Utils.openHome(ClassVideoListActivity.this);
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(ClassVideoListActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(ClassVideoListActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(ClassVideoListActivity.this);
                }
                return false;
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(event, new IntentFilter(DOWNLOAD_REQUEST));
    }

    private void setList(){
        try {
            findViewById(R.id.downloadProgress).setVisibility(View.GONE);
            videoListDownload = new JSONArray();
            for (int i = 0; i < videoList.length(); i++) {
                try {
                    String videoUrl = videoList.optJSONObject(i).optString("VideoURL");
                    String downloadURL = videoList.optJSONObject(i).optString("DownloadURL");
                    if(Utils.isValidString(downloadURL) && !downloadURL.contains("youtube.com")){
                        videoUrl = downloadURL;
                    }
                    if (videoUrl.contains("youtube.com")) {
                        if (videoUrl.contains("https://www.youtube.com/watch?v=")) {
                            videoUrl = videoUrl.replace("https://www.youtube.com/watch?v=", "");
                        } else if (videoUrl.contains("https://www.youtube.com/embed/")) {
                            videoUrl = videoUrl.replace("https://www.youtube.com/embed/", "");
                        }
                        int index = videoUrl.indexOf("&");
                        if (index > 0) {
                            videoUrl = videoUrl.substring(0, index);
                        }
                    }
                    File file = new File(getFilesDir() + "/Videos/" + videoUrl + ".mp4");
                    if(!file.exists() && (DownloadContent.isDownloading || DownloadVimeoContent.isVimeoDownloading) && videoUrl.equalsIgnoreCase(downloadVideoId)){
                        videoList.optJSONObject(i).put("downloadingText", downloadText);
                        videoList.optJSONObject(i).put("isDownloading", true);
                    }else{
                        videoList.optJSONObject(i).put("isDownloading", false);
                    }
                    videoList.optJSONObject(i).put("isFileDownload", file.exists());
                    if (file.exists()) {
                        videoListDownload.put(videoList.optJSONObject(i));
                    }
                } catch (Exception e) {
                    if (Utils.isDebugModeOn) {
                        e.printStackTrace();
                    }
                }
            }
            if (videoPagerAdapter == null) {
                videoPagerAdapter = new VideoPagerAdapter(this, getSupportFragmentManager());
                ViewPager viewPager = findViewById(R.id.view_pager);
                tabs.setupWithViewPager(viewPager);
                viewPager.setAdapter(videoPagerAdapter);
            } else {
                videoPagerAdapter.refreshList();
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(videoList != null && videoList.length() > 0) {
            setList();
        }
    }

    public void cancelDownload(){
        DownloadContent.cancelDownload(this);
        downloadVideoId = "";
        setList();
    }

    public void cancelVimeoDownload(){
        DownloadVimeoContent.cancelDownload(this);
        downloadVideoId = "";
        setList();
    }

    public void downloadFiles(String videoId, String videoJson){
        if(DownloadContent.isDownloading){
            Utils.showToast(getApplicationContext(), "Video already downloading, Please wait to finish download");
            return;
        }
        if(!Utils.isConnectedToInternet(getApplicationContext())){
            Utils.showToast(getApplicationContext(), getString(R.string.network_error_1));
            return;
        }
//        findViewById(R.id.downloadProgress).setVisibility(View.VISIBLE);
        String youtubeLink = "https://www.youtube.com/watch?v="+ videoId;
        if(Preferences.get(getApplicationContext(), Preferences.KEY_IS_CUSTOM_EXTRACTOR_ENABLED, true)){
            customExtracotr(youtubeLink, videoId, videoJson);
        }else{
            libExtractor(youtubeLink, videoId, videoJson);
        }
    }
    public void downloadVimeoFiles(String videoId, String channelName,String videoJson){
        if(DownloadVimeoContent.isVimeoDownloading){
            Utils.showToast(getApplicationContext(), "Video already downloading, Please wait to finish download");
            return;
        }
        if(!Utils.isConnectedToInternet(getApplicationContext())){
            Utils.showToast(getApplicationContext(), getString(R.string.network_error_1));
            return;
        }
        VimeoPlayer.getVideo(this, videoId, channelName, new VimeoPlayer.FetchListener() {
            @Override
            public void videoData(HashMap<String, Object> fetchedData) {
                if (Utils.isActivityDestroyed(ClassVideoListActivity.this)) {
                    return;
                }
                if(fetchedData != null){
                    setVimeoPlayerValues(fetchedData, videoId, videoJson, channelName);
                }
            }

            @Override
            public void error(String error) {
            }
        });
    }

    private void customExtracotr(String youtubeLink, String videoId, String videoJson){
        new ZTYouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles == null) {
                    if(progressDialog != null){
                        progressDialog.dismiss();
                    }
                    return;
                }
                setPlayerValues(getVideoData(ytFiles), videoId,videoJson);
            }
        }.extract(youtubeLink, true, true);
    }

    private void libExtractor(String youtubeLink, String videoId, String videoJson){
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles == null) {
                    if(progressDialog != null){
                        progressDialog.dismiss();
                    }
                    return;
                }
                setPlayerValues(getVideoData(ytFiles), videoId,videoJson);
            }
        }.extract(youtubeLink, true, true);
    }

    private HashMap<String, Object> getVideoData(SparseArray<YtFile> ytFiles){
        HashMap<Integer, HashMap<String, Object>> qualityMaps =new HashMap<>();
        HashMap<String, Object> videoItem = new HashMap<>();
        ArrayList<Integer> qualityList = new ArrayList<>();
        try{
            for(int i = 0 ; i < ytFiles.size() ; i++){
                int key = ytFiles.keyAt(i);
                YtFile file = ytFiles.get(key);
                if(file.getFormat().getHeight() == -1 || file.getFormat().getAudioBitrate() <= 0 || qualityMaps.containsKey(file.getFormat().getHeight())){
                    continue;
                }
                int height = file.getFormat().getHeight();
                HashMap<String, Object> item = new HashMap<>();
                item.put("url", file.getUrl());
                qualityMaps.put(height, item);
                qualityList.add(height);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Collections.sort(qualityList, Collections.reverseOrder());
        videoItem.put("qualityList", qualityList);
        videoItem.put("qualityMaps", qualityMaps);
        return videoItem;
    }

    private void setPlayerValues(HashMap<String, Object> fetchedData, String videoId, String videoJson){
        HashMap<Integer, HashMap<String, Object>> qualityMaps = (HashMap<Integer, HashMap<String, Object>>) fetchedData.get("qualityMaps");
        ArrayList<Integer> qualityList = (ArrayList<Integer>)fetchedData.get("qualityList");
        int position = qualityList.indexOf(360);
        if(position < 0){
            position = qualityList.indexOf(480);
            if(position < 0){
                position = qualityList.indexOf(540);
                if(position < 0){
                    position = qualityList.indexOf(720);
                }else{
                    position = qualityList.size() - 1;
                }
            }
        }
        String videoUrl = (String)qualityMaps.get(qualityList.get(position)).get("url");
        DownloadContent.downloadMedia(getApplicationContext(), videoUrl, videoId, videoJson);
    }
    private void setVimeoPlayerValues(HashMap<String, Object> fetchedData, String videoId, String videoJson, String channelName){
        HashMap<Integer, HashMap<String, Object>> qualityMaps = (HashMap<Integer, HashMap<String, Object>>) fetchedData.get("qualityMaps");
        ArrayList<Integer> qualityList = (ArrayList<Integer>)fetchedData.get("qualityList");
        int position = qualityList.indexOf(360);
        if(position < 0){
            position = qualityList.indexOf(480);
            if(position < 0){
                position = qualityList.indexOf(540);
                if(position < 0){
                    position = qualityList.indexOf(720);
                }else{
                    position = qualityList.size() - 1;
                }
            }
        }
        String videoUrl = (String)qualityMaps.get(qualityList.get(position)).get("url");
        DownloadVimeoContent.downloadMedia(getApplicationContext(), videoUrl, videoId, videoJson);
    }
    DownloadMediaTask downloadVideoTask;
    String downloadVideoId;
    String downloadText;

    public void deleteVideo(String videoId, boolean isCancel){
        findViewById(R.id.downloadProgress).setVisibility(View.VISIBLE);
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
                        setList();
                    }
                });
            }
        }).start();
    }

    ProgressDialog progressDialog;
    private void showDownloadDialog(int progress, int max, String videoId){
        try {
            findViewById(R.id.downloadProgress).setVisibility(View.GONE);
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Downloading...");
                progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(downloadVideoTask != null){
                            downloadVideoTask.cancel(true);
                        }
                        deleteVideo(videoId, true);
                        progressDialog.dismiss();
                    }
                });
            }
            progressDialog.setProgress(progress);
            progressDialog.setMax(max);
            progressDialog.setProgressNumberFormat(progress+" MB / "+max+" MB");
            if(!Utils.isActivityDestroyed(this)){
                progressDialog.show();
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(event);
    }


    class DownloadMediaTask extends AsyncTask<String, Integer, Boolean>{
        File file;
        String videoId, videoJson;
        int max;
        @Override
        protected Boolean doInBackground(String... strings) {
            BufferedInputStream ios = null;
            FileOutputStream fos = null;
            String downloadPath = strings[0];
            String savePath = strings[1];
            videoId = strings[2];
            videoJson = strings[3];
            file = new File(savePath);
            try {
                URL url = new URL(downloadPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                ios = new BufferedInputStream(connection.getInputStream());
                file.getParentFile().mkdirs();
                fos = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int len = 0;
                int downloadLength = 0;
                max = Math.round(connection.getContentLength()/(1024 * 1024));
                while ((len = ios.read(buffer)) != -1) {
                    if(isCancelled()){
                        deleteTitle(videoId);
                        return false;
                    }
                    downloadLength = downloadLength + len;
                    fos.write(buffer, 0, len);
                    publishProgress(downloadLength);
                }
                fos.flush();
                fos.close();
                ios.close();
                if(isCancelled()){
                    deleteTitle(videoId);
                    return false;
                }
                saveTitle(videoJson, videoId);
                return true;
            } catch (Exception e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
            deleteTitle(videoId);
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(isCancelled()){
                return;
            }
            showDownloadDialog(values[0]/(1024*1024), max, videoId);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            try {
                if (aBoolean) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    setList();
                } else {
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveTitle(String videoJson, String videoId){
        HashMap<String, HashMap<String, Object>> videoData = null;
        try{
            videoData = (HashMap<String, HashMap<String, Object>>)Utils.getObject(getApplicationContext(), "videoDownloadData");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(videoData == null){
            videoData = new HashMap<>();
        }
        HashMap<String, Object> item = new HashMap<>();
        item.put("videoJson", videoJson);
        videoData.put(videoId, item);
        Utils.saveObject(getApplicationContext(), videoData, "videoDownloadData");
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

    AlertDialog dialog;
    public void showDialog(String url) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.alert_dialog, null);
            TextView message = promptsView.findViewById(R.id.message);
            message.setText("Do you want to delete this video?");
            TextView delete = promptsView.findViewById(R.id.ok);
            delete.setText("Yes");
            delete.setBackgroundResource(R.drawable.button_red_rounded_4dp);
            promptsView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    deleteVideo(url, false);
                }
            });
            promptsView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            builder.setView(promptsView);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }
}
