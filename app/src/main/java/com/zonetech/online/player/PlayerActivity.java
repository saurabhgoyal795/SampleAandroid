package com.zonetech.online.player;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.zonetech.online.BuildConfig;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import at.huber.youtubeExtractor.ZTYouTubeExtractor;

public class PlayerActivity extends ZTAppCompatActivity {

    private final static String TAG = "CAVideoPlayerActivity";
    String videoId = "",videoTitle, thumbPath, channelName;
    private PlayerView nativePlayer;
    private ProgressBar videoProgress;
    private ExoVideoPlayer videoPlayer;
    private String videoUrl;
    private HashMap<Integer, HashMap<String, Object>> qualityMaps;
    private ArrayList<Integer> qualityList;
    private DisplayMetrics metrics;
    private int videoWidth,videoHeight, selectedQuality = -1,selectedSpeed = 0, attemptCount;
    private long currentTime;
    private ImageView mFullScreenIcon, exoSetting;
    private ArrayList<String> speedOptions = new ArrayList<>();
    private Float[] speedValues = {1f, 1.25f, 1.5f, 1.75f, 2f, 2.5f, 3f};
    private boolean isYoutubeVideo, isOfflineAvailable;
    private WebView webView;
    private String[] threeQuality = {"High", "Medium", "Low"};
    private String[] twoQuality = {"High", "Low"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!Utils.isDebugModeOn) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_video_player_new);
        webView = findViewById(R.id.webView);
        nativePlayer = findViewById(R.id.nativePlayer);
        videoProgress = findViewById(R.id.videoProgress);
        PlayerControlView controlView = nativePlayer.findViewById(com.google.android.exoplayer2.R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        exoSetting = controlView.findViewById(R.id.exo_setting);
        metrics = Utils.getMetrics(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if(Utils.isLollipop()) {
                int position = bundle.getInt("position");
                findViewById(R.id.image).setTransitionName("video_"+position);
            }
            isYoutubeVideo = bundle.getBoolean("isYoutubeVideo");
            thumbPath = bundle.getString("imagePath");
            videoId = bundle.getString("videoUrl");
            videoTitle = bundle.getString("title");
            channelName = bundle.getString("channelName");
            setTitle(videoTitle);
        }
        videoWidth = metrics.widthPixels;
        videoHeight = (int)((videoWidth * 1f)/1.77f);
        findViewById(R.id.image).getLayoutParams().height = findViewById(R.id.nativePlayer).getLayoutParams().height= findViewById(R.id.webView).getLayoutParams().height = videoHeight;
        setPlayer();
        setSpeedOptions();
    }


    YouTubeExtractor youTubeExtractor;
    ZTYouTubeExtractor ztYouTubeExtractor;
    private void setPlayer(){
        String videoSavePath = getFilesDir() + "/Videos/" + videoId + ".mp4";
        isOfflineAvailable = new File(videoSavePath).exists();
        if(isOfflineAvailable){
            videoUrl = videoSavePath;
            playNativePlayer(false);
            findViewById(R.id.quality).setVisibility(View.GONE);
        }else{
            if(isYoutubeVideo){
                if(Preferences.get(getApplicationContext(), Preferences.KEY_IS_CUSTOM_EXTRACTOR_ENABLED, true)){
                    customExtractor();
                }else{
                    libExtractor();
                }
            }else{
                thumbPath = VimeoPlayer.getVideoImagePath(this, videoId);
                VimeoPlayer.getVideo(this, videoId, channelName, new VimeoPlayer.FetchListener() {
                    @Override
                    public void videoData(HashMap<String, Object> fetchedData) {
                        if (Utils.isActivityDestroyed(PlayerActivity.this)) {
                            return;
                        }
                        if(fetchedData != null){
                            setPlayerValues(fetchedData);
                        }else{
                            openWebPlayer();
                        }
                    }

                    @Override
                    public void error(String error) {
                        openWebPlayer();
                    }
                });
            }
        }

        if(Utils.isValidString(thumbPath)){
            setImage();
        }
    }
    private void libExtractor(){
        String youtubeLink = "https://www.youtube.com/watch?v="+ videoId;
        if(youTubeExtractor != null){
            youTubeExtractor.cancel(true);
        }
        youTubeExtractor = new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                try {
                    if (ytFiles == null) {
                        if(attemptCount >= 3) {
                            Bundle bundle = new Bundle();
                            bundle.putString("youtubeLink", youtubeLink);
                            Utils.firebaseEvent(getApplicationContext(), "ExtractorError1", bundle);
                            Intent intent = new Intent(PlayerActivity.this, YoutubePlayerActivity.class);
                            intent.putExtra("youtubeId", videoId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            finish();
                            return;
                        }else{
                            attemptCount++;
                            setPlayer();
                        }
                    }
                    setPlayerValues(getVideoData(ytFiles));
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("youtubeLink", youtubeLink);
                    Utils.firebaseEvent(getApplicationContext(), "ExtractorError2", bundle);
                    Intent intent = new Intent(PlayerActivity.this, YoutubePlayerActivity.class);
                    intent.putExtra("youtubeId", videoId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            }
        };
        youTubeExtractor.extract(youtubeLink, true, true);
    }
    private void customExtractor(){
        String youtubeLink = "https://www.youtube.com/watch?v="+ videoId;
        if(ztYouTubeExtractor != null){
            ztYouTubeExtractor.cancel(true);
        }
        ztYouTubeExtractor = new ZTYouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                try {
                    if (ytFiles == null) {
                        if(attemptCount >= 3) {
                            Bundle bundle = new Bundle();
                            bundle.putString("youtubeLink", youtubeLink);
                            Utils.firebaseEvent(getApplicationContext(), "ExtractorError1", bundle);
                            Intent intent = new Intent(PlayerActivity.this, YoutubePlayerActivity.class);
                            intent.putExtra("youtubeId", videoId);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            finish();
                            return;
                        }else{
                            attemptCount++;
                            setPlayer();
                        }
                    }
                    setPlayerValues(getVideoData(ytFiles));
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("youtubeLink", youtubeLink);
                    Utils.firebaseEvent(getApplicationContext(), "ExtractorError2", bundle);
                    Intent intent = new Intent(PlayerActivity.this, YoutubePlayerActivity.class);
                    intent.putExtra("youtubeId", videoId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                }
            }
        };
        ztYouTubeExtractor.extract(youtubeLink, true, true);
    }
    private void setSpeedOptions(){
        for (int i = 0 ; i < speedValues.length ; i++){
            speedOptions.add(speedValues[i]+"x");
        }
    }

    String audioUrl = "";
    private HashMap<String, Object> getVideoData(SparseArray<YtFile> ytFiles){
        HashMap<Integer, HashMap<String, Object>> qualityMaps =new HashMap<>();
        HashMap<String, Object> videoItem = new HashMap<>();
        ArrayList<Integer> qualityList = new ArrayList<>();
        try{
            for(int i = 0 ; i < ytFiles.size() ; i++){
                int key = ytFiles.keyAt(i);
                YtFile file = ytFiles.get(key);
                if(file.getFormat().getHeight() == -1){
                    if(audioUrl == ""){
                        audioUrl = file.getUrl();
                    }
                    continue;
                }
                boolean isExists = qualityMaps.containsKey(file.getFormat().getHeight());
                if(!isExists ||(isExists && (Integer)qualityMaps.get(file.getFormat().getHeight()).get("bitRate") <= 0 && file.getFormat().getAudioBitrate() > 0)){
                    int height = file.getFormat().getHeight();
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("url", file.getUrl());
                    item.put("width", (int)(height * 1.77f));
                    item.put("height", height);
                    item.put("bitRate", file.getFormat().getAudioBitrate());
                    item.put("format", file.getFormat().getHeight());
                    if(Utils.isWebPlayerEnabled(this) && isYoutubeVideo && (Integer)item.get("bitRate") <= 0){
                        continue;
                    }
                    qualityMaps.put(height, item);
                    qualityList.add(height);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Collections.sort(qualityList, Collections.reverseOrder());
        videoItem.put("qualityList", qualityList);
        videoItem.put("qualityMaps", qualityMaps);
        videoItem.put("thumbPath", thumbPath);
        return videoItem;
    }

    private void setPlayerValues(HashMap<String, Object> fetchedData){
        qualityMaps = (HashMap<Integer, HashMap<String, Object>>) fetchedData.get("qualityMaps");
        qualityList = (ArrayList<Integer>)fetchedData.get("qualityList");
        thumbPath = (String)fetchedData.get("thumbPath");
        setVideoLayout();
        setImage();
        int position = qualityList.indexOf(360);
        if(position < 0){
            position = qualityList.size() - 1;
        }
        setQuality(position);
    }

    private void openWebPlayer(){
        Intent intent = new Intent(PlayerActivity.this, WebVideoPlayer.class);
        intent.putExtra("videoUrl", videoUrl);
        intent.putExtra("title", videoTitle);
        startActivity(intent);
        finish();
    }

    int retryCount = 0;
    private void playWebVideo(){
        webView.setVisibility(View.VISIBLE);
        if(webView.getSettings() != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
        }
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(videoProgress.getVisibility() == View.VISIBLE){
                    findViewById(R.id.image).setVisibility(View.GONE);
                }
                videoProgress.setVisibility(View.GONE);
                if(!Utils.isLandscape(getApplicationContext())){
                    showQualityOptionLayout();
                }
                view.loadUrl("javascript:(function() { var videoTags = document.getElementsByTagName('video');for( var i = 0; i < videoTags.length; i++ ){ videoTags.item(i).style.width = '100%';  videoTags.item(i).playbackRate= 1; videoTags.item(i).setAttribute('controlslist','nodownload');}})()");
                retryCount = 0;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                retryCount++;
                if(retryCount <= 2){
                    playWebVideo();
                }
            }
        });
        webView.setWebChromeClient(new MyChrome());
        webView.loadUrl(videoUrl);
    }
    private void playNativePlayer(boolean isMixedStream){
        videoProgress.setVisibility(View.VISIBLE);
        if(!isOfflineAvailable && isYoutubeVideo) {
            findViewById(R.id.image).setVisibility(View.GONE);
            if(Utils.isValidString(videoUrl) && Utils.isWebPlayerEnabled(this) && isYoutubeVideo){
                retryCount = 0;
                playWebVideo();
                return;
            }
        }
        nativePlayer.setVisibility(View.VISIBLE);
        videoPlayer = new ExoVideoPlayer(this, nativePlayer, true, true);
        videoPlayer.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        videoPlayer.setPlayerControl(new ExoVideoPlayer.PlayerControl() {
            @Override
            public void start() {
                if(videoProgress.getVisibility() == View.VISIBLE){
                    videoPlayer.seekTo(currentTime);
                    findViewById(R.id.image).setVisibility(View.GONE);
                }
                videoProgress.setVisibility(View.GONE);
                videoPlayer.setPlaybackSpeed(speedValues[selectedSpeed]);
            }

            @Override
            public void complete() {
                videoPlayer.seekTo(0);
                videoPlayer.pauseVideo();
            }

            @Override
            public void buffering() {

            }

            @Override
            public void pause() {

            }

            @Override
            public void resume() {

            }

            @Override
            public void videoSize(int height, int width) {
            }
        });

        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Utils.isValidString(videoUrl)) {
                    if(isMixedStream){
                        videoPlayer.setPlayer(videoUrl, audioUrl);
                    }else {
                        videoPlayer.setPlayer(videoUrl);
                    }
                }
            }
        },1000);
        if (Utils.isLandscape(this)) {
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
        } else {
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
        }
        mFullScreenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isLandscape(getApplicationContext())) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
                }
            }
        });

        exoSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionLayout();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(videoPlayer != null && videoPlayer.isPlaying()){
            videoPlayer.pauseVideo();
        }
    }

    private Runnable decor_view_settings = new Runnable() {
        @Override
        public void run() {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            new Handler().post(decor_view_settings);
        }
    }

    private void hideSystemUI(){
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
//
//    private void showSystemUI() {
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(videoPlayer != null) {
            videoPlayer.releasePlayer();
        }
        if(youTubeExtractor != null){
            youTubeExtractor.cancel(true);
        }
        if(ztYouTubeExtractor != null){
            ztYouTubeExtractor.cancel(true);
        }
        destroyWebView();
    }

    AlertDialog dialog;
    private void showQualityDialog(){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.quality_layout, null);
            ListView optionList = promptsView.findViewById(R.id.optionList);
            OptionListAdapter optionListAdapter = new OptionListAdapter();
            optionList.setAdapter(optionListAdapter);
            optionList.setOnItemClickListener(optionListAdapter);
            builder.setView(promptsView);
            dialog = builder.create();
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }


    AlertDialog speedDialog;
    private void showSpeedDialog(){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.quality_layout, null);
            ListView optionList = promptsView.findViewById(R.id.optionList);
            SppedListAdapter sppedListAdapter = new SppedListAdapter();
            optionList.setAdapter(sppedListAdapter);
            optionList.setOnItemClickListener(sppedListAdapter);
            builder.setView(promptsView);
            speedDialog = builder.create();
            if (!Utils.isActivityDestroyed(this))
                speedDialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    private void setImage(){
        try {
            Glide.with(this)
                    .load(thumbPath)
                    .override(videoWidth, videoHeight)
                    .thumbnail(.10f)
                    .into((ImageView) findViewById(R.id.image));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private void setVideoLayout(){
        try {
            int position = selectedQuality;
            if (position == -1) {
                position++;
            }
            metrics = Utils.getMetrics(this);
            if (metrics == null) {
                return;
            }
            if (Utils.isLandscape(this)) {
                mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
                videoWidth = metrics.widthPixels;
                videoHeight = metrics.heightPixels;
                hideSystemUI();
            } else {
                mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
                if (isOfflineAvailable) {
                    videoWidth = metrics.widthPixels;
                    videoHeight = (int) ((videoWidth * 1f) / 1.77f);
                } else {
                    videoWidth = metrics.widthPixels;
                    int height = (int) qualityMaps.get(qualityList.get(position)).get("height");
                    int width = (int) qualityMaps.get(qualityList.get(position)).get("width");
                    videoHeight = (height * metrics.widthPixels) / width;
                }
            }
            findViewById(R.id.image).getLayoutParams().height = findViewById(R.id.nativePlayer).getLayoutParams().height= findViewById(R.id.webView).getLayoutParams().height = videoHeight;
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {}

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(Utils.isWebPlayerEnabled(getApplicationContext()) && isYoutubeVideo){
            if(Utils.isLandscape(getApplicationContext())){
                hideOptionLayout();
            }else{
                showQualityOptionLayout();
            }
        }
        setVideoLayout();
    }

    class OptionListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        @Override
        public int getCount() {
            return qualityList.size();
        }

        @Override
        public Integer getItem(int position) {
            return qualityList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return qualityList.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quality_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText(getQualityText(qualityList, position, getItem(position)));

            holder.radioButton.setOnCheckedChangeListener(null);
            if(selectedQuality == position){
                holder.radioButton.setChecked(true);
            }else{
                holder.radioButton.setChecked(false);
            }
            holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        setQuality(position);
                        dialog.dismiss();
                        if(!(Utils.isWebPlayerEnabled(getApplicationContext()) && isYoutubeVideo)) {
                            hideOptionLayout();
                        }
                    }
                }
            });
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setQuality(position);
            dialog.dismiss();
            if(!(Utils.isWebPlayerEnabled(getApplicationContext()) && isYoutubeVideo)) {
                hideOptionLayout();
            }
        }

        class ViewHolder{
            TextView title;
            MaterialRadioButton radioButton;
            public ViewHolder(View view){
                title = view.findViewById(R.id.title);
                radioButton = view.findViewById(R.id.radioButton);
            }
        }
    }
    class SppedListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        @Override
        public int getCount() {
            return speedOptions.size();
        }

        @Override
        public String getItem(int position) {
            return speedOptions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return speedOptions.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.quality_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText(getItem(position));
            holder.radioButton.setOnCheckedChangeListener(null);
            if(selectedSpeed == position){
                holder.radioButton.setChecked(true);
            }else{
                holder.radioButton.setChecked(false);
            }
            holder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        setPlayBackSpeed(position);
                        speedDialog.dismiss();
                        if(!(Utils.isWebPlayerEnabled(getApplicationContext()) && isYoutubeVideo)) {
                            hideOptionLayout();
                        }
                    }
                }
            });
            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setPlayBackSpeed(position);
            speedDialog.dismiss();
            if(!(Utils.isWebPlayerEnabled(getApplicationContext()) && isYoutubeVideo)) {
                hideOptionLayout();
            }
        }

        class ViewHolder{
            TextView title;
            MaterialRadioButton radioButton;
            public ViewHolder(View view){
                title = view.findViewById(R.id.title);
                radioButton = view.findViewById(R.id.radioButton);
            }
        }
    }

    private void setQuality(int position){
        if(selectedQuality == position){
            return;
        }
        selectedQuality = position;
        videoUrl = (String)qualityMaps.get(qualityList.get(selectedQuality)).get("url");
        boolean isMixedStream = false;
        if(isYoutubeVideo){
            int bitRate = (Integer) qualityMaps.get(qualityList.get(selectedQuality)).get("bitRate");
            isMixedStream = (bitRate <= 0);
        }
        if(videoPlayer != null) {
            currentTime = videoPlayer.getCurrentPosition();
            videoPlayer.releasePlayer();
        }
        playNativePlayer(isMixedStream);
    }
    private void setPlayBackSpeed(int position){
        if(selectedSpeed == position){
            return;
        }
        selectedSpeed = position;
        if(isYoutubeVideo && Utils.isWebPlayerEnabled(this)){
            try {
                String url = "javascript:(function() { var videoTags = document.getElementsByTagName('video');for( var i = 0; i < videoTags.length; i++ ){ videoTags.item(i).style.width = '100%';  videoTags.item(i).playbackRate=" + speedValues[position] + "; videoTags.item(i).setAttribute('controlslist','nodownload');}})()";
                webView.loadUrl(url);
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }else {
            if (videoPlayer != null) {
                videoPlayer.setPlaybackSpeed(speedValues[position]);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.optionLayout).getVisibility() == View.VISIBLE){
            hideOptionLayout();
            if(!(Utils.isWebPlayerEnabled(getApplicationContext()) && isYoutubeVideo)) {
                return;
            }
        }
        if(Utils.isLollipop()){
            if(videoPlayer != null){
                videoPlayer.pauseVideo();
            }
            findViewById(R.id.image).setVisibility(View.VISIBLE);
        }
        super.onBackPressed();
    }

    private void showOptionLayout(){
        if(findViewById(R.id.videoOptionLayout).getVisibility() == View.VISIBLE){
            return;
        }
        findViewById(R.id.optionLayout).setVisibility(View.VISIBLE);
        Animation animationBottomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_in_200ms);
        animationBottomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.videoOptionLayout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.optionShadow).setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.optionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideOptionLayout();
            }
        });

        findViewById(R.id.videoOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        findViewById(R.id.videoOptionLayout).startAnimation(animationBottomIn);
        findViewById(R.id.quality).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQualityDialog();
            }
        });

        findViewById(R.id.speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpeedDialog();
            }
        });
    }

    private void showQualityOptionLayout(){
        if(findViewById(R.id.videoOptionLayout).getVisibility() == View.VISIBLE){
            return;
        }
        findViewById(R.id.optionLayout).setVisibility(View.VISIBLE);
        Animation animationBottomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_in_200ms);
        animationBottomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.videoOptionLayout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.optionShadow).setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        findViewById(R.id.videoOptionLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        findViewById(R.id.videoOptionLayout).startAnimation(animationBottomIn);
        findViewById(R.id.quality).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQualityDialog();
            }
        });
        findViewById(R.id.speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSpeedDialog();
            }
        });
    }

    private void hideOptionLayout(){
        if(findViewById(R.id.optionLayout).getVisibility() == View.GONE){
            return;
        }
        Animation animationBottomout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_out_200ms);
        animationBottomout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.optionShadow).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.videoOptionLayout).setVisibility(View.GONE);
                findViewById(R.id.optionLayout).setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.videoOptionLayout).startAnimation(animationBottomout);
    }

    private void destroyWebView(){
        try {
            webView.clearHistory();
            webView.clearCache(true);
            webView.destroy();
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private String getQualityText(ArrayList<Integer>qualityList, int position, int quality){
        if(isYoutubeVideo){
            return quality + "P";
        }
        if(qualityList.size() == 3){
            return threeQuality[position];
        }else if(qualityList.size() == 2){
            return twoQuality[position];
        }else{
            return quality + "P";
        }
    }
}
