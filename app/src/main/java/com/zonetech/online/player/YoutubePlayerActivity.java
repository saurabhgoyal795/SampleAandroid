package com.zonetech.online.player;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.zonetech.online.BuildConfig;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.utils.Utils;

import java.util.Map;

public class YoutubePlayerActivity extends ZTAppCompatActivity  implements YouTubePlayer.OnInitializedListener{
    private ZTYoutubeSupportFragment youTubePlayerSupportFragment;
    private YouTubePlayer player;
    private String youtube_id;
    private int currentTime;
    private boolean controlReset = true;
    private ImageView mFullScreenIcon;
    private SeekBar videoPlayerProgress;
    private Handler visiblityTimeHandler;
    private Runnable visiblityTimerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (visiblityTimeHandler == null || player == null)
                    return;
                findViewById(R.id.controlLayout).setVisibility(View.GONE);
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
    };

    private Handler timeHandler;
    private long startTime;
    long playerCurrentTime = 0;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (timeHandler == null || player == null)
                    return;
                long currentTime = player.getCurrentTimeMillis();
                videoPlayerProgress.setProgress((int) currentTime);
                videoPlayerProgress.setMax(player.getDurationMillis());
                ((TextView)findViewById(R.id.videoDuration)).setText(Utils.timeFormatHrMinSec(player.getDurationMillis()));
                ((TextView) findViewById(R.id.videoCurrentTime)).setText(Utils.timeFormatHrMinSec(currentTime));
                if (timeHandler != null) {
                    timeHandler.postDelayed(timerRunnable, 1000);
                }
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
    };
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!Utils.isDebugModeOn) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
        setContentView(R.layout.activity_youtube_player);
        Bundle bundle = getIntent().getExtras();
        youtube_id = bundle.getString("youtubeId");
        videoPlayerProgress = findViewById(R.id.videoPlayerProgress);
        mFullScreenIcon = findViewById(R.id.fullscreen_icon);
        findViewById(R.id.pauseLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player != null){
                    player.play();
                }
                findViewById(R.id.pauseLayout).setVisibility(View.GONE);
            }
        });
        findViewById(R.id.errorLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isConnectedToInternet(getApplicationContext())) {
                    findViewById(R.id.errorLayout).setVisibility(View.GONE);
                    setYoutubePlayer();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.network_error_1), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        setLayout();
        setYoutubePlayer();
    }

    private void setLayout(){
        if(Utils.isLandscape(this)){
            findViewById(R.id.playerLayout).getLayoutParams().height = Utils.getMetrics(this).heightPixels;
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_exit_white_18dp);
        }else{
            findViewById(R.id.playerLayout).getLayoutParams().height = (int)(240 * Utils.getMetrics(this).density);
            mFullScreenIcon.setImageResource(R.drawable.fullscreen_white_18dp);
        }
    }

    private void setYoutubePlayer(){
        youTubePlayerSupportFragment = new ZTYoutubeSupportFragment(this, new ZTYoutubeSupportFragment.ToggleListener() {
            @Override
            public void toggleControls() {
                if(player != null) {
                    stopControlsVisiblityTimer();
                    if(findViewById(R.id.controlLayout).getVisibility() == View.VISIBLE){
                        findViewById(R.id.controlLayout).setVisibility(View.GONE);
                    }else{
                        findViewById(R.id.controlLayout).setVisibility(View.VISIBLE);
                        startControlsVisiblityTimer();
                    }
                }
            }
        });

        youTubePlayerSupportFragment.initialize(BuildConfig.YT_DEV_KEY, this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.youtube_view, youTubePlayerSupportFragment);
        fragmentTransaction.commitAllowingStateLoss();

    }
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        youTubePlayer.setManageAudioFocus(true);
        youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                |YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                |YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        try{
            if (!b) {
                youTubePlayer.loadVideo(youtube_id);
            }
            else {
                youTubePlayer.play();
            }
            player = youTubePlayer;


        }catch (Throwable e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(String s) {
                try {
                    if (player != null) {
                        if(currentTime != 0){
                            player.seekToMillis(currentTime);
                            player.pause();
                        } else {
                            if (player != null) {
                                player.pause();
                            }
                        }
                    }

                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
                if (player != null) {
                    int currentPosition = player.getCurrentTimeMillis();
                    if (currentPosition == 0) {
                        player.play();
                    } else {
                        player.pause();
                    }
                }
            }

            @Override
            public void onAdStarted() {
            }

            @Override
            public void onVideoStarted() {
            }

            @Override
            public void onVideoEnded() {
                controlReset = true;
                player.seekToMillis(0);
                videoPlayerProgress.setProgress(0);
                ((TextView) findViewById(R.id.videoCurrentTime)).setText("00:00:00");
                player.pause();
                stopControlsVisiblityTimer();
                findViewById(R.id.controlLayout).setVisibility(View.VISIBLE);
                stopTimerHandler();
                findViewById(R.id.pauseLayout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {
                if(errorReason != null && YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY.toString().equalsIgnoreCase(errorReason.name())){
                    ((ImageView)findViewById(R.id.playButton)).setImageResource(R.drawable.ic_media_play);
                    findViewById(R.id.pauseLayout).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.errorLayout).setVisibility(View.VISIBLE);
                }
                controlReset = true;
            }
        });
        youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
                findViewById(R.id.controlLayout).setVisibility(View.VISIBLE);
                videoPlayerProgress.setMax(player.getDurationMillis());
                findViewById(R.id.pauseLayout).setVisibility(View.GONE);
                ((ImageView)findViewById(R.id.playButton)).setImageResource(R.drawable.ic_media_pause);
                if(controlReset){
                    controlReset = false;
                    ((TextView)findViewById(R.id.videoDuration)).setText(Utils.timeFormatHrMinSec(player.getDurationMillis()));
                    videoPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(fromUser){
                                player.seekToMillis(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            stopControlsVisiblityTimer();
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            startControlsVisiblityTimer();
                        }
                    });
                    findViewById(R.id.playButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(player != null) {
                                if (player.isPlaying()) {
                                    player.pause();
                                } else {
                                    player.play();
                                }
                            }
                        }
                    });
                    startTimeHandler();
                    startControlsVisiblityTimer();
                }
            }

            @Override
            public void onPaused() {
                ((ImageView)findViewById(R.id.playButton)).setImageResource(R.drawable.ic_media_play);
                findViewById(R.id.pauseLayout).setVisibility(View.VISIBLE);
                stopTimerHandler();
                controlReset = true;
            }

            @Override
            public void onStopped() {
                controlReset = true;
            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        });
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private void startControlsVisiblityTimer(){
        stopControlsVisiblityTimer();
        visiblityTimeHandler = new Handler();
        visiblityTimeHandler.postDelayed(visiblityTimerRunnable, 4000);
    }

    private void stopControlsVisiblityTimer(){
        if(visiblityTimeHandler != null){
            visiblityTimeHandler.removeCallbacks(visiblityTimerRunnable);
        }
        visiblityTimeHandler = null;
    }
    private void startTimeHandler() {
        timeHandler = new Handler(Looper.getMainLooper());
        timeHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimerHandler(){
        if(timeHandler != null){
            timeHandler.removeCallbacks(timerRunnable);
            timeHandler = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyYoutube();
    }

    private void destroyYoutube(){
        try {
            stopControlsVisiblityTimer();
            stopTimerHandler();
            if (player != null) {
                playerCurrentTime = player.getCurrentTimeMillis();
                player.release();
                player = null;
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
        setLayout();
    }
}
