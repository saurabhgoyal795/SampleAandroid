package com.zonetech.online.player;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.view.GestureDetectorCompat;

import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.zonetech.online.utils.Utils;

public class ZTYoutubeSupportFragment extends YouTubePlayerSupportFragment {
    private GestureDetectorCompat gestureDetector;
    private Activity activity;

    public interface ToggleListener{
        void toggleControls();
    }

    private ToggleListener toggleListener;
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        final LayoutInflater localInflater = layoutInflater.cloneInContext(activity);
        YouTubePlayerViewWrapper wrapper = null;
        try {
            wrapper = new YouTubePlayerViewWrapper(activity);
            wrapper.addView(super.onCreateView(localInflater, viewGroup, bundle));
            gestureDetector = new GestureDetectorCompat(activity, onGestureListener);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(wrapper == null){
           return super.onCreateView(localInflater, viewGroup, bundle);
        }else{
            return wrapper;
        }
    }

    public ZTYoutubeSupportFragment(){

    }

    public ZTYoutubeSupportFragment(Activity activity, ToggleListener toggleListener){
        this.activity = activity;
        this.toggleListener = toggleListener;
    }

    private class YouTubePlayerViewWrapper extends FrameLayout {

        public YouTubePlayerViewWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            return gestureDetector.onTouchEvent(ev);
        }
    }
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
            if(toggleListener != null){
                toggleListener.toggleControls();
            }
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
}
