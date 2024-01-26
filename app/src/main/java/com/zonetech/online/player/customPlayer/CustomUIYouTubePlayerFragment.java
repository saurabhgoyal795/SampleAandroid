package com.zonetech.online.player.customPlayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.zonetech.online.R;
import com.zonetech.online.BuildConfig;
import com.zonetech.online.utils.Utils;

/**
 * Fragment for playing YouTube videos with a custom UI
 */
public class CustomUIYouTubePlayerFragment extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener, YouTubePlayer.PlayerStateChangeListener, YouTubePlayer.PlaybackEventListener {

    private static final String TAG = "CustomUIYTPlayerFrag";

    protected String youtubeId;
    protected String apiKey;
    protected boolean enableLogging;

    protected PlayerControlsPopupWindow playerControls;
    protected int lastPositionMillis;
    protected boolean wasPlaying;

    private YouTubePlayer youtubePlayer;
    private GestureDetectorCompat gestureDetector;
    private ContextThemeWrapper wrappedContext;
    private final LogWrapper logWrapper = new LogWrapper();

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final int HIDE_STATUS_BAR_FLAGS_IMMERSIVE = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private static final int HIDE_STATUS_BAR_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LOW_PROFILE;
    private static final int SEEK_BAR_UPDATE_INTERVAL = 1000;
    private static final int HIDE_STATUS_BAR_DELAY_MILLIS = 3000;

    private static final String STATE_LAST_POSITION_MILLIS = CustomUIYouTubePlayerFragment.class.getPackage().getName() + ".state_last_millis";
    private static final String STATE_WAS_PLAYING = CustomUIYouTubePlayerFragment.class.getPackage().getName() + ".state_was_playing";

    private static final String ARG_VIDEO_YOUTUBE_ID = CustomUIYouTubePlayerFragment.class.getPackage().getName() + ".arg_video_youtube_id";
    private static final String ARG_GOOGLE_API_KEY = CustomUIYouTubePlayerFragment.class.getPackage().getName() + ".arg_google_api_key";
    private static final String ARG_THEME_RESOURCE_ID = CustomUIYouTubePlayerFragment.class.getPackage().getName() + ".arg_theme_id";
    private static final String ARG_ENABLE_LOGGING = CustomUIYouTubePlayerFragment.class.getPackage().getName() + ".arg_enable_logging";

    private Handler seekBarPositionHandler = new Handler();
    private Runnable seekBarPositionRunnable = new Runnable() {
        @Override
        public void run() {
            if(youtubePlayer != null) {
                logWrapper.d(TAG, "In CustomUIYouTubePlayerFragment#seekBarPositionRunnable.run. Current time in millis is %d. Current length in millis is %d", youtubePlayer.getCurrentTimeMillis(), youtubePlayer.getDurationMillis());
                //Docs say that getDurationMillis() can change over time, so if things have changed, update SeekBarMax so we don't potentially get out of range
                if(playerControls.getSeekBarMax() != youtubePlayer.getDurationMillis()) {
                    logWrapper.d(TAG, "In CustomUIYouTubePlayerFragment#seekBarPositionRunnable.run. Updating SeekBarMax from %d to %d", playerControls.getSeekBarMax(), youtubePlayer.getDurationMillis());
                    playerControls.setSeekBarMax(youtubePlayer.getDurationMillis());
                }
                playerControls.setSeekBarPosition(youtubePlayer.getCurrentTimeMillis());
                scheduleSeekBarUpdate();
            }

        }
    };
    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            toggleControlsVisibility();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    /**
     * Create a new instance of CustomUiYouTubePlayerFragment
     *
     * @param youtubeId ID of YouTube video to play
     * @param apiKey Google API Key
     * @param themeResourceId Theme Resource ID to use (null if using Activity's theme)
     * @param enableDebugLogging True, to enable logging fragment events to LogCat even if not a
     *                           debug build
     * @return New instance of CustomUiYouTubePlayerFragment
     */
    public static CustomUIYouTubePlayerFragment newInstance(@NonNull String youtubeId,
                                                            @NonNull String apiKey,
                                                            @Nullable @StyleRes Integer themeResourceId,
                                                            boolean enableDebugLogging) {
        CustomUIYouTubePlayerFragment frag = new CustomUIYouTubePlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_YOUTUBE_ID, youtubeId);
        args.putString(ARG_GOOGLE_API_KEY, apiKey);
        args.putBoolean(ARG_ENABLE_LOGGING, enableDebugLogging);
        if(themeResourceId != null) {
            args.putInt(ARG_THEME_RESOURCE_ID, themeResourceId);
        }
        frag.setArguments(args);
        return frag;
    }

    /**
     * Create a new instance of CustomUiYouTubePlayerFragment
     *
     * @param youtubeId ID of YouTube video to play
     * @param apiKey Google API Key
     * @param themeResourceId Theme Resource ID to use (null if using Activity's theme)
     */
    public static CustomUIYouTubePlayerFragment newInstance(@NonNull String youtubeId,
                                                            @NonNull String apiKey,
                                                            @Nullable @StyleRes Integer themeResourceId) {
        return newInstance(youtubeId, apiKey, themeResourceId, false);
    }

    public CustomUIYouTubePlayerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        wrapContext(activity);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        wrapContext(context);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logWrapper.d(TAG, "In onCreate. Initialize controls");

        if(savedInstanceState == null) {
            //Init new instance
            lastPositionMillis = 0;
            wasPlaying = false;

        } else {
            //init saved instance
            lastPositionMillis = savedInstanceState.getInt(STATE_LAST_POSITION_MILLIS, 0);
            wasPlaying = savedInstanceState.getBoolean(STATE_WAS_PLAYING);
        }

        youtubeId = getArguments().getString(ARG_VIDEO_YOUTUBE_ID);
        apiKey = getArguments().getString(ARG_GOOGLE_API_KEY);
        enableLogging = getArguments().getBoolean(ARG_ENABLE_LOGGING);

        logWrapper.d(TAG, "youtubeId: %s", youtubeId);
        logWrapper.d(TAG, "lastPositionMillis: %s", lastPositionMillis);

        if(TextUtils.isEmpty(youtubeId)) {
            throw new IllegalArgumentException("youtubeId cannot be null/empty");
        }
        if(TextUtils.isEmpty(apiKey)) {
            throw new IllegalArgumentException("apiKey cannot bey null/empty");
        }

        gestureDetector = new GestureDetectorCompat(getActivity(), onGestureListener);

        playerControls = new PlayerControlsPopupWindow(wrappedContext);
        playerControls.setEnabled(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logWrapper.d(TAG, "In onCreateView. Wrap the YouTubePlayerView");
        // clone the inflater using the context wrapped with the desired theme
        final LayoutInflater localInflater = inflater.cloneInContext(wrappedContext);

        YouTubePlayerViewWrapper wrapper = new YouTubePlayerViewWrapper(wrappedContext);
        wrapper.addView(super.onCreateView(localInflater, container, savedInstanceState));

        return wrapper;
    }

    @Override
    public void onStart() {
        super.onStart();
        logWrapper.d(TAG, "In onStart. Ensure playerControls are dismissed, init YouTubePlayer");

        //Want to make sure we start in the state where things will be shown when we hit onResume
        if(playerControls.isShowing()) {
            playerControls.dismiss();
        }

        initializeYoutubePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        logWrapper.d(TAG, "In onResume. Post runnable to root view, that will initially show the player controls & choose SystemUI mode");
        //We have to wait till everything is running before we show the PopupWindow, otherwise you get an exception.
        //This runnable will run once the View is attached to the window
        if ( isYoutubePlayerViewReady() ) {
            //noinspection ConstantConditions
            getView().post(new Runnable() {
                @Override
                public void run() {
                    final Activity activity = getActivity();
                    if (activity != null && !activity.isFinishing()) {
                        final View decorView = activity.getWindow().getDecorView();
                        String systemUiMode;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            //Have to use Sticky Immersive mode on Lollipop devices, otherwise when the Status Bar is shown
                            //the YoutubePlayer will throw the YouTubePlayer.ErrorReason.UNAUTHORIZED_OVERLAY error and stop playback.
                            //This doesn't happen on KitKat but since KitKat has Immersive mode, we'll use it there as well
                            decorView.setSystemUiVisibility(HIDE_STATUS_BAR_FLAGS_IMMERSIVE);
                            systemUiMode = "IMMERSIVE";
                        } else {
                            //For Jelly Bean, things are a little different. Here we're faking Sticky Immersive mode. The user can swipe down
                            //from the top of the screen at any time and unhide the Status Bar, and it'll never go away. So detect
                            //when the fullscreen flag gets removed, and post a delayed runnable that will hide the system bar again
                            //just like Sticky Immersive mode
                            decorView.setSystemUiVisibility(HIDE_STATUS_BAR_FLAGS);
                            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                                @Override
                                public void onSystemUiVisibilityChange(int visibility) {
                                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                        decorView.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                decorView.setSystemUiVisibility(HIDE_STATUS_BAR_FLAGS);
                                            }
                                        }, HIDE_STATUS_BAR_DELAY_MILLIS);
                                    }
                                }
                            });
                            systemUiMode = "FAKED IMMERSIVE";
                        }
                        logWrapper.d(TAG, "SystemUIMode: %s", systemUiMode);
                        toggleControlsVisibility();
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        if(youtubePlayer != null) {
            wasPlaying = youtubePlayer.isPlaying();
            youtubePlayer.pause();
            lastPositionMillis = youtubePlayer.getCurrentTimeMillis();
            logWrapper.d(TAG, "in onPause. Recording lastPositionMillis: %d", lastPositionMillis);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        logWrapper.d(TAG, "In onStop. Hide controls, reset button state, release player");

        //If activity is restarted, player will be paused. So reset the button state on our way out
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
        playerControls.setEnabled(false);
        //Stops window leaked error
        playerControls.dismiss();
        //Doing this here guarantees that if the activity is not completely torn down,
        //we can re-initialize. Otherwise you'll get stuck in onPaused state for YouTubePlayer.
        if(youtubePlayer != null) {
            youtubePlayer.release();
        }
        youtubePlayer = null;
        super.onStop();
    }

    @Override
    public void onDetach() {
        wrappedContext = null;
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            initializeYoutubePlayer();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            int height = Utils.getMetrics(getActivity()).heightPixels;
            playerControls.rootPopupView.getLayoutParams().height = height;
        }else{
            float density = Utils.getMetrics(getActivity()).density;
            playerControls.rootPopupView.getLayoutParams().height = (int)(240*density);
        }
        if(playerControls.isShowing()) {
            playerControls.update();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        logWrapper.d(TAG, "in onSaveInstanceState. lastPositionMillis: %d", lastPositionMillis);
        bundle.putInt(STATE_LAST_POSITION_MILLIS, lastPositionMillis);
        bundle.putBoolean(STATE_WAS_PLAYING, wasPlaying);
        super.onSaveInstanceState(bundle);
    }

    private boolean isYoutubePlayerViewReady() {
        boolean isReady = getView() != null;
        logWrapper.d(TAG, "isYoutubePlayerViewReady: %s", isReady);
        return isReady;
    }

    private void initializeYoutubePlayer() {
        logWrapper.d(TAG, "In initializeYoutubePlayer");
        initialize(apiKey, this);
    }

    private void toggleControlsVisibility() {
        if(playerControls.isShowing()) {
            logWrapper.d(TAG, "In toggleControlsVisibility. Hiding player controls");
            playerControls.dismiss();
        } else {
            logWrapper.d(TAG, "In toggleControlsVisibility. Showing player controls");
            if ( isYoutubePlayerViewReady() ) {
                int height = Utils.getMetrics(getActivity()).heightPixels;
                playerControls.showAtLocation(getView(), Gravity.CENTER_VERTICAL, 0, height/2);
            }
        }
    }

    private void wrapContext(Context contextToWrap) {
        final int themeResourceId = getArguments().getInt(ARG_THEME_RESOURCE_ID, 0);
        wrappedContext = new ContextThemeWrapper(contextToWrap, themeResourceId != 0 ? themeResourceId
                :  R.style.YouTubePlayerTheme);
    }

    private void scheduleSeekBarUpdate() {
        seekBarPositionHandler.postDelayed(seekBarPositionRunnable, SEEK_BAR_UPDATE_INTERVAL);
    }

    private void stopSeekBarUpdates() {
        seekBarPositionHandler.removeCallbacks(seekBarPositionRunnable);
    }

    /////////////////////////////////////////////
    //YouTubePlayer.OnInitializedListener Methods
    /////////////////////////////////////////////

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        logWrapper.d(TAG, "In YouTubePlayer.OnInitializedListener.onInitializationSuccess. wasRestored = %s", wasRestored);
        youtubePlayer = player;
        youtubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        youtubePlayer.setPlayerStateChangeListener(this);
        youtubePlayer.setPlaybackEventListener(this);
        if (!wasRestored) {
            logWrapper.d(TAG, "In YouTubePlayer.OnInitializedListener.onInitializationSuccess. Cueing video with id = %s", youtubeId);
            player.loadVideo(youtubeId);
        }
        if(Utils.isLandscape(getActivity())){
            int height = Utils.getMetrics(getActivity()).heightPixels;
            playerControls.rootPopupView.getLayoutParams().height = height;
        }else{
            float density = Utils.getMetrics(getActivity()).density;
            playerControls.rootPopupView.getLayoutParams().height = (int)(240*density);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        logWrapper.d(TAG, "In YouTubePlayer.OnInitializedListener.onInitializationFailure. errorReason = %s", errorReason.toString());
        final Activity activity = getActivity();
        if(activity != null) {
            if (errorReason.isUserRecoverableError()) {
                errorReason.getErrorDialog(activity, RECOVERY_DIALOG_REQUEST).show();
            } else {
                String errorMessage = "Error, Plese try again!";
                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
            }
        }

    }

    /////////////////////////////////////////////////
    //YouTubePlayer.PlayerStateChangeListener Methods
    /////////////////////////////////////////////////

    @Override
    public void onLoading() {
        logWrapper.d(TAG, "In YouTubePlayer.PlayerStateChangeListener.onLoading");
        //Set controls to their initial state and lock them until we have all the information
        //necessary to fully set their state
        playerControls.setEnabled(false);
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
        playerControls.setSeekBarPosition(0);
    }

    @Override
    public void onLoaded(String videoId) {
        logWrapper.d(TAG, "In YouTubePlayer.PlayerStateChangeListener.onLoaded");
        //Complete initializing controls and enable them for interaction
        playerControls.setSeekBarMax(youtubePlayer.getDurationMillis());
        if (lastPositionMillis != 0) {
            youtubePlayer.seekToMillis(lastPositionMillis);
            playerControls.setSeekBarPosition(lastPositionMillis);
        }
        if(wasPlaying) {
            youtubePlayer.play();
        }
        playerControls.setEnabled(true);
    }

    @Override
    public void onAdStarted() {
        logWrapper.d(TAG, "In YouTubePlayer.PlayerStateChangeListener.onAdStarted");
        playerControls.setEnabled(false);
    }

    @Override
    public void onVideoStarted() {
        logWrapper.d(TAG, "In YouTubePlayer.PlayerStateChangeListener.onVideoStarted. youtubePlayer.isPlaying(): %s", youtubePlayer.isPlaying());
        //We need to set button state to either play or pause, because if the video has ended and you scrub back
        //This callback fires but you won't necessarily actually be playing
        playerControls.setPlayPauseButtonState(youtubePlayer.isPlaying() ? PlayerControlsPopupWindow.PlayPauseButtonState.PAUSE : PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
    }

    @Override
    public void onVideoEnded() {
        logWrapper.d(TAG, "In YouTubePlayer.PlayerStateChangeListener.onVideoEnded");
        //Since the video has ended, set the controls to the restart state
        playerControls.setSeekBarPosition(playerControls.getSeekBarMax());
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.REPLAY);
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        logWrapper.d(TAG, "In YouTubePlayer.PlayerStateChangeListener.onError: %s", errorReason);
        if(errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
            if(getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    /////////////////////////////////////////////////
    //YouTubePlayer.PlaybackEventListener Methods
    /////////////////////////////////////////////////

    @Override
    public void onPlaying() {
        logWrapper.d(TAG, "In YouTubePlayer.PlaybackEventListener.onPlaying");
        scheduleSeekBarUpdate();
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PAUSE);
    }

    @Override
    public void onPaused() {
        logWrapper.d(TAG, "In YouTubePlayer.PlaybackEventListener.onPaused");
        stopSeekBarUpdates();
        playerControls.setPlayPauseButtonState(PlayerControlsPopupWindow.PlayPauseButtonState.PLAY);
    }

    @Override
    public void onStopped() {
        logWrapper.d(TAG, "In YouTubePlayer.PlaybackEventListener.onStopped");
        stopSeekBarUpdates();
    }

    @Override
    public void onBuffering(boolean isBuffering) {
        logWrapper.d(TAG, "In YouTubePlayer.PlaybackEventListener.onBuffering, isBuffering = %s, youtubePlayer.isPlaying = %s", isBuffering, youtubePlayer.isPlaying());

        //If we're buffering, we're not playing. So we don't need to monitor the video's current position
        //until we stop buffering and start playing again
        if(isBuffering) {
            stopSeekBarUpdates();
        } else if(youtubePlayer.isPlaying()) {
            scheduleSeekBarUpdate();
        }
    }

    @Override
    public void onSeekTo(int newPositionMillis) {
        logWrapper.d(TAG, "In YouTubePlayer.PlaybackEventListener.onSeekTo, newPositionMillis = %d", newPositionMillis);
    }

    /**
     * Class containing the player controls. Has to be a PopupWindow due to the YoutubePlayer
     * library disallowing you to draw a View on top of it.
     */
    private class PlayerControlsPopupWindow extends PopupWindow implements SeekBar.OnSeekBarChangeListener {

        private SeekBar seekBar;
        private ImageButton playPauseButton;
        private TextView yt_duration, yt_position;
        private boolean isEnabled;
        private int playPauseButtonState;
        public RelativeLayout rootPopupView;

        private static final String LESS_THAN_HUNDRED_MINUTES_FORMAT = "mm:ss";
        private static final String HUNDRED_MINUTES_FORMAT = "mmm:ss";

        private final class PlayPauseButtonState {
            public static final int PLAY = 1;
            public static final int PAUSE = 0;
            public static final int REPLAY = 2;
        }

        public PlayerControlsPopupWindow(Context context) {
            super(context);
            setContentView(View.inflate(context, R.layout.youtube_player_controls, null));
            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            setFocusable(false);
            setTouchable(true);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                setAttachedInDecor(false);
            }

            yt_duration = (TextView) getContentView().findViewById(R.id.yt_duration);
            yt_position = (TextView)getContentView().findViewById(R.id.yt_position);
            rootPopupView = (RelativeLayout)getContentView().findViewById(R.id.rootPopupView);
            seekBar = (SeekBar) getContentView().findViewById(R.id.yt_progress);
            seekBar.setOnSeekBarChangeListener(this);
            playPauseButton = (ImageButton) getContentView().findViewById(R.id.yt_playPause);
            if(playPauseButton.getDrawable() == null) {
                playPauseButton.setImageResource(R.drawable.ic_media_pause);
            }
            playPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(youtubePlayer.isPlaying()) {
                        setPlayPauseButtonState(PlayPauseButtonState.PLAY);
                        if (youtubePlayer != null) {
                            youtubePlayer.pause();
                        }
                    } else {
                        setPlayPauseButtonState(PlayPauseButtonState.PAUSE);
                        if (youtubePlayer != null) {
                            youtubePlayer.play();
                        }
                    }
                }
            });
        }

        /**
         * Sets the max value of the SeekBar
         *
         * @param milliseconds The # of milliseconds that the max value should be set to
         */
        public void setSeekBarMax(int milliseconds) {
            logWrapper.d(TAG, "In PlayerControlsPopupWindow.setSeekBarMax. Setting max to %d", milliseconds);
            seekBar.setMax(milliseconds);
            String timeFormat = milliseconds / 60000 >= 100 ? HUNDRED_MINUTES_FORMAT : LESS_THAN_HUNDRED_MINUTES_FORMAT;
            yt_duration.setText(DurationFormatUtils.formatDuration(milliseconds, timeFormat, true));
        }
        /**
         * Gets the max value of the SeekBar
         *
         * @return The max value, in milliseconds
         */
        public int getSeekBarMax() {
            return seekBar.getMax();
        }
        /**
         * Sets the state of the Play/Pause button
         *
         * @param state Any of the constants in {@link PlayPauseButtonState}
         */

        public void setPlayPauseButtonState(int state) {
            switch (state) {
                case PlayPauseButtonState.PLAY:
                    playPauseButton.setImageResource(R.drawable.exo_icon_play);
                    break;
                case PlayPauseButtonState.PAUSE:
                    playPauseButton.setImageResource(R.drawable.exo_icon_pause);
                    break;
                case PlayPauseButtonState.REPLAY:
                    playPauseButton.setImageResource(R.drawable.exo_icon_play);
                    break;
                default:
                    logWrapper.w(TAG, "Value passed into setPlayPauseButtonState was not a valid state constant. Value passed in: %d", state);
                    break;
            }
            playPauseButtonState = state;
        }
        /**
         * Gets the state of the Play/Pause button
         *
         * @return Any of the constants in {@link PlayPauseButtonState}
         */
        public int getPlayPauseButtonState() {
            return playPauseButtonState;
        }
        /**
         * Sets the position of the SeekBar
         *
         * @param milliseconds The position the SeekBar should be set to, in milliseconds.
         *                     Should be between 0 and {@link PlayerControlsPopupWindow#getSeekBarMax()}, inclusive
         */
        public void setSeekBarPosition(int milliseconds) {
            logWrapper.d(TAG, "In PlayerControlsPopupWindow.setSeekbarPosition. Passed in milliseconds: %d", milliseconds);
            if(milliseconds >=0 && milliseconds <= seekBar.getMax()) {
                logWrapper.d(TAG, "In PlayerControlsPopupWindow.setSeekbarPosition. Setting seekBar to %d", milliseconds);
                seekBar.setProgress(milliseconds);
            }
        }
        /**
         * Sets the enabled state of the Play/Pause button and the SeekBar
         *
         * @param isEnabled true, if the controls should be enabled
         */
        public void setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            playPauseButton.setEnabled(isEnabled);
            seekBar.setEnabled(isEnabled);
        }
        /**
         * Indicates if the Play/Pause button and SeekBar are enabled
         *
         * @return true, if enabled
         */
        public boolean isEnabled() {
            return isEnabled;
        }

        ///////////////////////
        //SeekBarChangeListener
        ///////////////////////
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            logWrapper.d(TAG, "PlayerControlsPopupWindow.onProgressChanged progress:%d, fromUser:%s", progress, fromUser);
            String timeFormat = progress / 60000 >= 100 ? HUNDRED_MINUTES_FORMAT : LESS_THAN_HUNDRED_MINUTES_FORMAT;
            yt_position.setText(DurationFormatUtils.formatDuration(progress, timeFormat, true));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            logWrapper.d(TAG, "PlayerControlsPopupWindow.onStartTrackingTouch");
            //The user is manipulating the SeekBar, so stop automatically updating it
            stopSeekBarUpdates();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            logWrapper.d(TAG, "PlayerControlsPopupWindow.onStopTrackingTouch. Seeking to %d", seekBar.getProgress());
            //The user ended manipulating the SeekBar. Seek the player to the current place, and restart
            //automatic updates if the video is playing
            youtubePlayer.seekToMillis(seekBar.getProgress());
            if(youtubePlayer.isPlaying()) {
                scheduleSeekBarUpdate();
            }
            if ( isYoutubePlayerViewReady() ) {
                int height = Utils.getMetrics(getActivity()).heightPixels;
                playerControls.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);
            }
        }
    }

    /**
     * Wrapper for YouTubePlayerView to expose touches, since attaching an OnTouchListener
     * to YouTubePlayerView doesn't work
     */
    private class YouTubePlayerViewWrapper extends FrameLayout {

        public YouTubePlayerViewWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            return gestureDetector.onTouchEvent(ev);
        }
    }

    /**
     * Wrapper around Android Log class
     */
    private final class LogWrapper {

        public void d(String tag, String msg, Object... args) {
            if(shouldLog(msg)) {
                Log.d(tag, assembleMessageString(msg, args));
            }
        }

        public void w(String tag, String msg, Object... args) {
            if(shouldLog(msg)) {
                Log.w(tag, assembleMessageString(msg, args));
            }
        }

        private String assembleMessageString(String msg, Object... args) {
            if(args.length > 0) {
                return String.format(msg, args);
            } else {
                return msg;
            }

        }

        private boolean shouldLog(String msg) {
            return !TextUtils.isEmpty(msg) && (BuildConfig.DEBUG || enableLogging);
        }
    }
}
