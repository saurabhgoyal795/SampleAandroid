package com.zonetech.online.server;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.zonetech.online.mypackage.onlineClass.ClassVideoListActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DownloadContent {
    private DownloadContent(){}
    private static DownloadMediaTask downloadVideoTask;
    public static boolean isDownloading = false;
    public static String downloadVideoId = "";

    public static void downloadMedia(Context context, String videoUrl, String videoId, String videoJson){
        Log.i("DownloadTesting", "downloadMedia download videoUrl = "+videoUrl);
        if(isDownloading){
            Intent intent = new Intent(ClassVideoListActivity.DOWNLOAD_REQUEST);
            intent.putExtra("type", "error");
            intent.putExtra("error", "Video already downloading, Please wait to finish download");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }else{
            downloadVideoId = videoId;
            Intent intent = new Intent(ClassVideoListActivity.DOWNLOAD_REQUEST);
            intent.putExtra("type", "start");
            intent.putExtra("videoId", videoId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            if(downloadVideoTask != null){
                downloadVideoTask.cancel(true);
            }
            downloadVideoTask = new DownloadMediaTask(context);
            downloadVideoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, videoUrl, videoId, videoJson);
        }
    }

    static class DownloadMediaTask extends AsyncTask<String, Integer, Boolean> {
        File file;
        String videoId, videoJson;
        int max;

        Context context;

        public DownloadMediaTask(Context context){
            this.context = context;
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            isDownloading = true;
            DataInputStream ios = null;
            FileOutputStream fos = null;
            String downloadPath = strings[0];
            String inputPath = context.getFilesDir() + "/TempVideos/";
            String outputPath = context.getFilesDir() + "/Videos/";
            String inputFile = videoId + ".mp4";
            String savePath = inputPath + inputFile;
            videoId = strings[1];
            videoJson = strings[2];
            file = new File(savePath);
            try {
                JSONObject object = new JSONObject();
                object.put("videoUrl", downloadPath);
                object.put("videoId", videoId);
                object.put("videoJson", videoJson);
                Preferences.put(context, Preferences.KEY_LAST_DOWNLOAD_DATA, object.toString());
                URL url = new URL(downloadPath);
                URLConnection connection = url.openConnection();
                file.getParentFile().mkdirs();
                int downloaded = 0;
                if(file.exists()){
                    downloaded = (int)file.length();
                    connection.setRequestProperty("Range", "bytes=" + (int) file.length() + "-");
                }
                if(downloaded > 0){
                    fos = new FileOutputStream(file, true);
                }else {
                    fos = new FileOutputStream(file);
                }
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.connect();
                ios = new DataInputStream(url.openStream());
                byte[] buffer = new byte[4096];
                int len = 0;
                int downloadLength = downloaded;
                max = Math.round((connection.getContentLength()+(int)file.length())/(1024 * 1024));
                while ((len = ios.read(buffer)) != -1) {
                    if(isCancelled()){
                        file.delete();
                        fos.flush();
                        fos.close();
                        ios.close();
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
                    file.delete();
                    return false;
                }
                Utils.moveFile(inputPath, inputFile, outputPath);
                saveTitle(videoJson, videoId);
                return true;
            } catch (Exception e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
                Log.i("DownloadTesting", "error = "+e.getMessage());
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(isCancelled()){
                return;
            }
            Intent intent = new Intent(ClassVideoListActivity.DOWNLOAD_REQUEST);
            intent.putExtra("type", "progress");
            intent.putExtra("progress", values[0]/(1024*1024));
            intent.putExtra("max", max);
            intent.putExtra("videoId", videoId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            isDownloading = false;
            try {
                if (aBoolean) {
                    Intent intent = new Intent(ClassVideoListActivity.DOWNLOAD_REQUEST);
                    intent.putExtra("type", "finish");
                    intent.putExtra("videoId", videoId);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                } else {
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }
                Preferences.remove(context, Preferences.KEY_LAST_DOWNLOAD_DATA);
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        private void saveTitle(String videoJson, String videoId){
            HashMap<String, HashMap<String, Object>> videoData = null;
            try{
                videoData = (HashMap<String, HashMap<String, Object>>)Utils.getObject(context, "videoDownloadData");
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
            Utils.saveObject(context, videoData, "videoDownloadData");
        }
    }

    public static void resumeDownload(Context context){
        if(!isDownloading){
            try{
                String value = Preferences.get(context, Preferences.KEY_LAST_DOWNLOAD_DATA, "");
                Log.i("DownloadTesting", "resume download value = "+value);
                if(Utils.isValidString(value)){
                    JSONObject object = new JSONObject(value);
                    String videoUrl = object.optString("videoUrl");
                    String videoId = object.optString("videoId");
                    String videoJson = object.optString("videoJson");
                    downloadMedia(context, videoUrl, videoId, videoJson);
                }else{
                    Utils.deleteRecursive(new File(context.getFilesDir() + "/TempVideos/"));
                }
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void cancelDownload(Context context){
        if(downloadVideoTask != null){
            downloadVideoTask.cancel(true);
        }
        isDownloading = false;
        Preferences.remove(context, Preferences.KEY_LAST_DOWNLOAD_DATA);
    }
}
