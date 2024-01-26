package com.zonetech.online;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.login.LoginActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.AnimationListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class InitialScreenActivity extends ZTAppCompatActivity {
    private ViewFlipper viewFlipper;
    private LinearLayout circleLayout;
    private TextView nextButton;
    private Handler flipHandler;
    private Runnable flipRunnable = new Runnable() {
        @Override
        public void run() {
            if(flipHandler == null){
                return;
            }
            viewFlipper.setInAnimation(InitialScreenActivity.this, R.anim.right_in );
            viewFlipper.setOutAnimation(InitialScreenActivity.this, R.anim.left_out);
            showNextItem("left");
            if(flipHandler == null){
                return;
            }
        }
    };
    DisplayMetrics metrics;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screen);
        metrics = Utils.getMetrics(this);
        viewFlipper = findViewById(R.id.viewFlipper);
        circleLayout = findViewById(R.id.circleLayout);
        nextButton = findViewById(R.id.nextButton);
        getSplashImages();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InitialScreenActivity.this, LoginActivity.class));
            }
        });
    }
    private void startAutoFlip(){
        stopAutoFlip();
        if(flipHandler == null){
            flipHandler = new Handler(getMainLooper());
        }
        flipHandler.postDelayed(flipRunnable, 4000);
    }

    private void stopAutoFlip(){
        if(flipHandler != null){
            flipHandler.removeCallbacks(flipRunnable);
            flipHandler = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startAutoFlip();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAutoFlip();
    }
    private void showNextItem(String calledFrom){
        try {
            animateCircle(calledFrom);
            if (calledFrom.equals("left")) {
                viewFlipper.showNext();
            } else {
                viewFlipper.showPrevious();
            }
            int childNum = viewFlipper.getDisplayedChild();
            for (int i = 0; i < circleLayout.getChildCount(); i++) {
                ImageView view = (ImageView) circleLayout.getChildAt(i);
                if (i == childNum) {
                    view.setColorFilter(ContextCompat.getColor(InitialScreenActivity.this, R.color.ca_green));
                    view.getLayoutParams().height = view.getLayoutParams().width = (int)(10*metrics.density);
                } else {
                    view.setColorFilter(ContextCompat.getColor(InitialScreenActivity.this, android.R.color.white));
                    view.getLayoutParams().height = view.getLayoutParams().width = (int)(6*metrics.density);
                }
            }
            if(childNum == viewFlipper.getChildCount() - 1){
                nextButton.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
    private void animateCircle(String moveTo){
        ScaleAnimation animation = new ScaleAnimation(1,(float)1.2, 1,(float) 1.2,
                (float)5*metrics.density,(float)5*metrics.density);
        ScaleAnimation animation1 = new ScaleAnimation((float)1.2,(float)1, (float)1.2,(float) 1,
                (float)5*metrics.density,(float)5*metrics.density);
        animation.setDuration(400);
        animation.setStartOffset(0);
        animation.setFillAfter(true);
        animation1.setDuration(400);
        animation1.setStartOffset(0);
        animation1.setFillAfter(true);

        animation.setAnimationListener(new AnimationListener(){
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                startAutoFlip();
            }
        });

        ImageView[] views = new ImageView[circleLayout.getChildCount()];
        for (int i = 0 ; i < circleLayout.getChildCount() ; i++){
            ImageView view = (ImageView)circleLayout.getChildAt(i);
            views[i] = view;
            view.setAnimation(null);
        }

        int index = viewFlipper.getDisplayedChild();
        if(moveTo.equalsIgnoreCase("left")){
            if(index >= viewFlipper.getChildCount() - 1){
                views[circleLayout.getChildCount() - 1].startAnimation(animation1);
                views[0].startAnimation(animation);
            }else{
                views[index].startAnimation(animation1);
                views[index + 1].startAnimation(animation);
            }
        }else{
            if(index == 0){
                views[0].startAnimation(animation1);
                views[0].startAnimation(animation);
            }else{
                views[index].startAnimation(animation1);
                views[index - 1].startAnimation(animation);
            }
        }
    }

    private void getSplashImages(){
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "appbanner", null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(InitialScreenActivity.this)){
                    return;
                }
                String message = response.optString("Message");
                if("Success".equalsIgnoreCase(message)){
                    JSONArray array = response.optJSONArray("Body");
                    for (int i = 0 ; i < array.length() ; i++){
                        ImageView view = (ImageView) LayoutInflater.from(viewFlipper.getContext()).inflate(R.layout.flipper_item, viewFlipper, false);
                        String imagePath = array.optJSONObject(i).optString("ImagePath");
                        imagePath = ServerApi.BASE_IMAGE_PATH + "Upload/AppBanner/"+imagePath;
                        if(Utils.isActivityDestroyed(InitialScreenActivity.this)){
                            return;
                        }
                        Glide.with(InitialScreenActivity.this)
                                .load(imagePath)
                                .into(view);
                        viewFlipper.addView(view);
                        ImageView dotView = (ImageView) LayoutInflater.from(circleLayout.getContext()).inflate(R.layout.circle_layout, circleLayout, false);
                        if(circleLayout.getChildCount() == 0){
                            dotView.getLayoutParams().height = dotView.getLayoutParams().width = (int)(10*metrics.density);
                            dotView.setColorFilter((ContextCompat.getColor(InitialScreenActivity.this, R.color.ca_green)));
                        }
                        circleLayout.addView(dotView);
                    }
                    if(viewFlipper.getChildCount() > 1){
                        OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener(){
                            @Override
                            public void onSwipeLeft() {
                                super.onSwipeLeft();
                                stopAutoFlip();
                                viewFlipper.setInAnimation(InitialScreenActivity.this ,R.anim.right_in );
                                viewFlipper.setOutAnimation(InitialScreenActivity.this, R.anim.left_out);
                                showNextItem("left");
                            }

                            @Override
                            public void onSwipeRight() {
                                super.onSwipeRight();
                                stopAutoFlip();
                                viewFlipper.setInAnimation(InitialScreenActivity.this, R.anim.left_in );
                                viewFlipper.setOutAnimation(InitialScreenActivity.this, R.anim.right_out);
                                showNextItem("right");
                            }
                        };
                        viewFlipper.setOnTouchListener(onSwipeTouchListener);
                        circleLayout.setVisibility(View.VISIBLE);
                    }else{
                        nextButton.setVisibility(View.VISIBLE);
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    startAutoFlip();
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }
}
