package com.zonetech.online.player;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoSize;
import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;


public class ExoVideoPlayer {
    private static final String TAG = "CAExoplayer";
    private PlayerView playerView;
    private SimpleExoPlayer videoPlayer;
    private boolean isAutoPlay;
    private Context context;
    public long startTime;
    public long endTime;

    public interface PlayerControl{
        void start();
        void complete();
        void buffering();
        void pause();
        void resume();
        void videoSize(int height, int width);
    }

    private PlayerControl playerControl;
    public ExoVideoPlayer(Context context, PlayerView playerView, boolean isController, boolean isAutoPlay){
        this.context = context;
        this.isAutoPlay = isAutoPlay;
        this.playerView = playerView;
        this.playerView.setUseController(isController);
        this.playerView.setControlDispatcher(new DefaultControlDispatcher(){
            @Override
            public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
                if(playerControl != null){
                    if(playWhenReady){
                        playerControl.resume();
                    }else{
                        playerControl.pause();
                    }
                }
                return super.dispatchSetPlayWhenReady(player, playWhenReady);
            }
        });
    }

    public void setController(boolean isController){
        playerView.setUseController(isController);
    }

    public void setPlayer(String path){
        Uri uri = Uri.parse(path);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        if(endTime > 0){
            mediaItem = new MediaItem.Builder()
                    .setUri(uri)
                    .setClipStartPositionMs(startTime)
                    .setClipEndPositionMs(endTime)
                    .build();
        }
        setPlayerEvents();
        videoPlayer.setMediaItem(mediaItem);
        videoPlayer.prepare();
        videoPlayer.setPlayWhenReady(isAutoPlay);
    }

    public void setPlayer(String videoPath, String audioPath){
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context);
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        MediaSource videoSource =  new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(videoPath)));
        MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(audioPath)));
        concatenatedSource.addMediaSource(new MergingMediaSource(videoSource, audioSource));
        setPlayerEvents();
        videoPlayer.setMediaSource(concatenatedSource);
        videoPlayer.prepare();
        videoPlayer.setPlayWhenReady(isAutoPlay);
    }

    private void setPlayerEvents(){
        videoPlayer = new SimpleExoPlayer.Builder(context).build();
        playerView.setPlayer(videoPlayer);
        videoPlayer.addVideoListener(new Player.Listener() {
            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                if(playerControl!= null){
                    playerControl.videoSize(videoSize.height, videoSize.width);
                }
            }
        });

        videoPlayer.addListener(new Player.Listener() {

            @Override
            public void onPlayerError(PlaybackException error) {
                Bundle bundle = new Bundle();
                bundle.putString("error", error.getMessage());
                Utils.firebaseEvent(context, "ExoPlayerError", bundle);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        if(playerControl != null){
                            playerControl.buffering();
                        }
                        break;
                    case Player.STATE_ENDED:
                        if(playerControl != null){
                            playerControl.complete();
                        }
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        if(playerControl != null){
                            playerControl.start();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void setResizeMode(int mode){
        playerView.setResizeMode(mode);
    }

    public void setPlayerControl(PlayerControl playerControl){
        this.playerControl = playerControl;
    }

    public void pauseVideo(){
        if(videoPlayer != null) {
            videoPlayer.setPlayWhenReady(false);
        }
    }
    public void startVideo(){
        if(videoPlayer != null && !isPlaying()) {
            videoPlayer.setPlayWhenReady(true);
        }
    }
    public void releasePlayer(){
        if(videoPlayer != null) {
            videoPlayer.release();
        }
    }

    public long getCurrentPosition(){
        if(videoPlayer == null){
            return 0;
        }
        return videoPlayer.getCurrentPosition();
    }

    public long getDuration(){
        if(videoPlayer == null){
            return 0;
        }
        return videoPlayer.getDuration();
    }

    public long getBufferedPosition(){
        if(videoPlayer == null){
            return 0;
        }
        return videoPlayer.getBufferedPosition();
    }

    public boolean isPlaying(){
        if(videoPlayer == null){
            return false;
        }
        return videoPlayer.getPlayWhenReady();
    }

    public void seekTo(long value){
        if(videoPlayer != null) {
            videoPlayer.seekTo(value);
        }
    }

    public static synchronized Cache getDownloadCache(Context context) {
        return VideoCache.getInstance(context);
    }

    public void setPlaybackSpeed(float value){
        if(videoPlayer != null) {
            PlaybackParameters param = new PlaybackParameters(value);
            videoPlayer.setPlaybackParameters(param);
        }
    }
}
