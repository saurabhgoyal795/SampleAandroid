package com.zonetech.online.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.zonetech.online.BuildConfig;
import com.zonetech.online.OnSwipeTouchListener;
import com.zonetech.online.R;
import com.zonetech.online.classes.ClassPackagePlanActivity;
import com.zonetech.online.dailyQuiz.DailyQuizActvity;
import com.zonetech.online.freecourses.FreeCoursesActivity;
import com.zonetech.online.mypackage.MyPackageActivity;
import com.zonetech.online.news.NewsActivity;
import com.zonetech.online.payment.CartActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.server.RequestSingleton;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.testseries.TestSeriesPlanActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.AnimationListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.os.Looper.getMainLooper;

public class HomeFragment extends Fragment {

    private String homeItemsArray = "[{\"title\":\"Online Classes\",\"icon\":\"home_onlineclass\",\"type\":\"online_class\"},{\"title\":\"Test Series\",\"icon\":\"home_testseries\",\"type\":\"online_series\"},{\"title\":\"Publications\",\"icon\":\"publication\",\"type\":\"publication\"},{\"title\":\"My Packages\",\"icon\":\"mycourses\",\"type\":\"my_packages\"},{\"title\":\"Free Courses\",\"icon\":\"elearning\",\"type\":\"freecourses\"},{\"title\":\"Daily Quiz\",\"icon\":\"quiz\",\"type\":\"daily_quiz\"}]";
    private LinearLayout mainLayout;
    private RecyclerView testimonialList;
    TestimonialAdapter testimonialAdapter;
    private int liveClassVisibleItem;
    private Handler flipHandler;
    DisplayMetrics metrics;
    private View rootView;
    private Runnable flipRunnable = new Runnable() {
        @Override
        public void run() {
            if (flipHandler == null) {
                return;
            }
            showNextItem();
            if(flipHandler != null){
                flipHandler.postDelayed(flipRunnable, 4000);
            }
        }
    };

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mainLayout = rootView.findViewById(R.id.mainLayout);
        testimonialList = rootView.findViewById(R.id.testimonialList);
        metrics = Utils.getMetrics(getActivity());
        setHomeList();
        getTopRanker();
        checkForDueCourses();
        return rootView;
    }

    private void setHomeList() {
        int width = (metrics.widthPixels - (int)(128 * metrics.density))/2;
        int height = (metrics.heightPixels - (int)(350 * metrics.density))/3;
        int minHeight = (int)(120 * metrics.density);
        width = Math.max(width, minHeight);
        height = Math.max(height, minHeight);
        try {
            JSONArray array = new JSONArray(homeItemsArray);
            int index = 0;
            for (int i = 0 ; i < mainLayout.getChildCount() ; i++){
                LinearLayout child = (LinearLayout) mainLayout.getChildAt(i);
                for (int j = 0 ; j < child.getChildCount() ; j++){
                    CardView innerChild = (CardView)child.getChildAt(j);
                    innerChild.getLayoutParams().height = (width > height) ? height : width;
                    JSONObject item = array.optJSONObject(index);
                    ((TextView)innerChild.findViewById(R.id.title)).setText(item.optString("title"));
                    ImageView icon = innerChild.findViewById(R.id.icon);
                    int imageId = getResources().getIdentifier(item.optString("icon"), "drawable", getActivity().getPackageName());
                    if(Utils.isActivityDestroyed(getActivity())){
                        return;
                    }
                    Glide.with(HomeFragment.this)
                            .load(imageId)
                            .into(icon);
                    innerChild.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if("online_series".equalsIgnoreCase(item.optString("type"))){
                                startActivity(new Intent(getActivity(), TestSeriesPlanActivity.class));
                            }else if("my_packages".equalsIgnoreCase(item.optString("type"))){
                                startActivity(new Intent(getActivity(), MyPackageActivity.class));
                            } else if ("online_class".equalsIgnoreCase(item.optString("type"))){
                                startActivity(new Intent(getActivity(), ClassPackagePlanActivity.class));
                            } else if ("publication".equalsIgnoreCase(item.optString("type"))){
                                startActivity(new Intent(getActivity(), PublicationActivity.class));
                            }else if ("freecourses".equalsIgnoreCase(item.optString("type"))){
                                startActivity(new Intent(getActivity(), FreeCoursesActivity.class));
                            }else if ("daily_quiz".equalsIgnoreCase(item.optString("type"))){
                                startActivity(new Intent(getActivity(), DailyQuizActvity.class));
                        }
                        }
                    });
                    index++;
                }
            }
            mainLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }

    private void startAutoFlip() {
        if (flipHandler == null) {
            flipHandler = new Handler(getMainLooper());
        }
        flipHandler.postDelayed(flipRunnable, 4000);
    }

    private void stopAutoFlip() {
        if (flipHandler != null) {
            flipHandler.removeCallbacks(flipRunnable);
            flipHandler = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startAutoFlip();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAutoFlip();
    }


    private void showNextItem() {
        try {
            int finalIndex = liveClassVisibleItem + 1;
            int totalCount = testimonialAdapter.getItemCount();
            if (finalIndex < totalCount) {
                testimonialList.smoothScrollToPosition(finalIndex);
            } else {
                finalIndex = 0;
                testimonialList.scrollToPosition(finalIndex);
            }
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }

    private void getTopRanker() {
        checkCacheValues();
        ServerApi.callServerApiJsonArray(getActivity(), ServerApi.WEB_URL, "homeAchivements", new ServerApi.CompleteListenerArray() {
            @Override
            public void response(JSONArray response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                Utils.saveObject(getActivity(), response.toString(), "homeAchivements");
                setTestimonialList(response);
            }

            @Override
            public void error(String error) {

            }
        });
    }

    private void setTestimonialList(JSONArray response){
        if(testimonialAdapter == null) {
            new PagerSnapHelper().attachToRecyclerView(testimonialList);
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
            testimonialList.setNestedScrollingEnabled(false);
            testimonialList.setHasFixedSize(false);
            testimonialList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    final int position = mLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (position >= 0) {
                        liveClassVisibleItem = position;
                    }
                }
            });
            testimonialAdapter = new TestimonialAdapter(getActivity(), response);
            testimonialList.setLayoutManager(mLayoutManager);
            testimonialList.setAdapter(testimonialAdapter);
        }else{
            testimonialAdapter.refreshValues(response);
        }
    }

    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getActivity(), "homeAchivements");
            if(Utils.isValidString(value)){
                JSONArray response = new JSONArray(value);
                setTestimonialList(response);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }



    private void checkForDueCourses(){
        ServerApi.callServerApi(getActivity(), ServerApi.BASE_URL,"Onlineclassstudentpackage?id="+ Utils.getStudentId(getActivity())+"&v="+ BuildConfig.VERSION_CODE, null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                JSONArray data = response.optJSONArray("Body");
                if (data.length() > 0){
                    JSONObject dueObject = null;
                    for (int i =0; i<data.length(); i++){
                        if (data.optJSONObject(i).optInt("TypeID") == 1 ){
                            if((dueObject == null || dueObject.optInt("Duration") > data.optJSONObject(i).optInt("Duration"))&& data.optJSONObject(i).optInt("Duration") <= 10){
                                dueObject = data.optJSONObject(i);
                            }
                        }
                    }
                    if(dueObject != null){
//                        setDueLayout(dueObject);
                    }else{
                        rootView.findViewById(R.id.dueClass).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void error(String error) {
            }
        });
    }

    private void setDueLayout(JSONObject dueObject){
        ImageView classImage = rootView.findViewById(R.id.classImage);
        TextView classTitle = rootView.findViewById(R.id.classTitle);
        TextView classDuration = rootView.findViewById(R.id.classDuration);
        classDuration.setText(dueObject.optString("DurationText", "Due"));
        classTitle.setText(dueObject.optString("PlanName"));
        rootView.findViewById(R.id.addToCardButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyPackage(dueObject);
            }
        });
        rootView.findViewById(R.id.classPopupCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootView.findViewById(R.id.dueClass).setVisibility(View.GONE);
                rootView.findViewById(R.id.testimonialList).setVisibility(View.VISIBLE);
            }
        });
        rootView.findViewById(R.id.dueClass).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.testimonialList).setVisibility(View.GONE);
        Glide.with(this)
                .load(ServerApi.IMAGE_URL + dueObject.optString("ImageURL"))
                .into(classImage);
    }
    private void buyPackage(JSONObject dueObject){
        Intent intent = new Intent(getActivity(), CartActivity.class);
        intent.putExtra(CartActivity.EXTRA_PACKAGE_ID, dueObject.optInt("PlanID"));
        intent.putExtra(CartActivity.EXTRA_SUBJECT_ID, dueObject.optInt("SubjectID"));
        intent.putExtra(CartActivity.EXTRA_SCREEN_TYPE, "class");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 200);
    }
}