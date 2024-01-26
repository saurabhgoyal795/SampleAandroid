package com.zonetech.online.freecourses;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.classes.ClassProductItemAdapter;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.dailyQuiz.DailyQuizActvity;
import com.zonetech.online.payment.CartActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FreeClassPackageDetailsActivity extends ZTAppCompatActivity implements ClassProductItemAdapter.ClickListener, View.OnClickListener {
    private ImageView bannerImage;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeigth;
    private JSONObject itemObj;
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private ClassProductItemAdapter adapter = null;
    private String packageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_class_details);
        metrics = Utils.getMetrics(this);
        Bundle bundle = getIntent().getExtras();
        bannerImage = findViewById(R.id.bannerImage);
        if(bundle != null){
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                setTitle(itemObj.optString("OrgPlanName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
            if(Utils.isLollipop()) {
                int position = bundle.getInt("position");
                bannerImage.setTransitionName("class_"+position);
            }
        }
        mRecyclerView = findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setImageViewSize();
        setBannerImage();
        setPriceText(itemObj);
        getPlanDetails();
        findViewById(R.id.buyButton).setOnClickListener(this);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    Utils.openHome(FreeClassPackageDetailsActivity.this);
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackagesNewTask(FreeClassPackageDetailsActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackagesNewTask(FreeClassPackageDetailsActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(FreeClassPackageDetailsActivity.this);
                }
                return false;
            }
        });
    }

    private void getPlanDetails(){
        JSONObject params = new JSONObject();
        try{
            params.put("OrgPlanID", itemObj.optInt("OrgPlanID"));
            params.put("StudentID", Utils.getStudentId(this));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "OnlineClassPackageDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(FreeClassPackageDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONObject data = response.optJSONObject("Body");
                setViews(data);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void alreadyEntrolled(){
        ((TextView)findViewById(R.id.buyButton)).setText("Enrolled");
        findViewById(R.id.buyButton).setEnabled(false);
        findViewById(R.id.buyButton).setBackgroundResource(R.drawable.button_green_rounded_25dp);
    }

    private void setViews(JSONObject data){
        if (data != null){
            String featureVideo = data.optString("FeatureVideo");
            if(Utils.isValidString(featureVideo)){
                findViewById(R.id.featuresLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.featuresLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(featureVideo));
                        startActivity(i);
                    }
                });
            }
            findViewById(R.id.buyButton).setVisibility(View.VISIBLE);
            if(data.optBoolean("IsPurchased")){
                alreadyEntrolled();
            }
            String highlights  = data.optString("Highlights");
            ZTWebView descriptionView = findViewById(R.id.description);
            descriptionView.loadData(highlights, "text/html", "UTF-8");
            setPriceText(data);
            packageName = data.optString("OrgPlanName");
            setTitle(packageName);

            TextView totalTests = findViewById(R.id.totalTests);
            totalTests.setText(data.optString("TotalExam"));
            TextView validity = findViewById(R.id.validity);
            validity.setText("Course Validity: " + data.optString("DurationText"));

            TextView startDate = findViewById(R.id.StartDate);
            startDate.setText(data.optString("StartDate"));
            TextView endDate = findViewById(R.id.EndDate);
            endDate.setText(data.optString("EndDate"));
            JSONArray jsonArray = data.optJSONArray("OrgStudentSpecializationList");
            JSONArray tempjsonArray = new JSONArray();
            for (int i = 0 ; i<jsonArray.length(); i++){
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int specId = Preferences.get(getApplicationContext(), Preferences.KEY_SPEC_ID, 0);
                    //for non tech
                    if (jsonObject.optInt("SpecializationID") == 6 ) {
                        tempjsonArray.put(jsonObject);
                    } else if (jsonObject.optInt("SpecializationID") == specId){
                        tempjsonArray.put(jsonObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            JSONArray productArray = new JSONArray();
            for (int i = 0; i<tempjsonArray.length(); i++){
                try {
                    JSONArray subjectList = tempjsonArray.getJSONObject(i).getJSONArray("OrganizationSubjectList");
                    for (int k= 0 ; k<subjectList.length(); k++){
                        productArray.put(subjectList.getJSONObject(k));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            setBannerImage();
            if(adapter == null) {
                adapter = new ClassProductItemAdapter(productArray,R.layout.class_product_subject_item, FreeClassPackageDetailsActivity.this, this, true);
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(productArray);
            }
        }
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    private void setPriceText(JSONObject data){
        try{
            TextView priceView = findViewById(R.id.priceView);
            double mrp = data.optDouble("PlanMRP");
            double price = data.optDouble("Fees");
            String currency = getString(R.string.currency);
            long discount = Math.round(((mrp - price)/mrp)*100);
            String mrpString = currency + Math.round(mrp);
            String priceString = currency + Math.round(price);
            String text = String.format(getResources().getString(R.string.price_value), mrpString, priceString, discount+"%");
            int index = text.indexOf(mrpString);
            SpannableString strNew = new SpannableString(text);
            StrikethroughSpan span = new StrikethroughSpan();
            strNew.setSpan(span, index, index + String.valueOf(mrpString).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            priceView.setText(strNew);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private void setImageViewSize(){
        imageWidth = metrics.widthPixels;
        imageHeigth = (491 * imageWidth)/858;
        bannerImage.getLayoutParams().height = imageHeigth;
        bannerImage.getLayoutParams().width = imageWidth;
    }
    private void setBannerImage(){
        String imagePath = itemObj.optString("ImageURL");
        if(Utils.isValidString(imagePath)) {
            imagePath = ServerApi.IMAGE_URL + imagePath;
            if(Utils.isActivityDestroyed(this)){
                return;
            }
            Glide.with(this)
                    .load(imagePath)
                    .override(imageWidth, imageHeigth)
                    .error(R.drawable.samplepackage)
                    .placeholder(R.drawable.samplepackage)
                    .into(bannerImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200 && resultCode == RESULT_OK){
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buyButton){
                JSONObject params = new JSONObject();
                try{
                    params.put("PlanID", itemObj.optInt("OrgPlanID"));
                    params.put("StudentID", Utils.getStudentId(this));
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
                ServerApi.callServerApi(this, ServerApi.BASE_URL, "EnrollFreePackage", params, new ServerApi.CompleteListener() {
                    @Override
                    public void response(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Successfully Enrolled", Toast.LENGTH_SHORT).show();
                        alreadyEntrolled();
                    }

                    @Override
                    public void error(String error) {
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    public void buy(int subjectId, int mrp, int fees, int duration) {

    }
}
