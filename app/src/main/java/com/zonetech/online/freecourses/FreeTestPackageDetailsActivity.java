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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestActivity;
import com.zonetech.online.payment.CartActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class FreeTestPackageDetailsActivity extends ZTAppCompatActivity implements View.OnClickListener {
    private ImageView bannerImage;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeigth;
    private JSONObject itemObj;
    private String pdfPath;
    private String packageName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_test_details);
        metrics = Utils.getMetrics(this);
        bannerImage = findViewById(R.id.bannerImage);
        Bundle bundle = getIntent().getExtras();
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
                bannerImage.setTransitionName("test_"+position);
            }
        }
        findViewById(R.id.schedule).setOnClickListener(this);
        findViewById(R.id.buyButton).setOnClickListener(this);
        findViewById(R.id.freeDemo).setOnClickListener(this);
        findViewById(R.id.freeDemo).setVisibility(View.GONE);
        setImageViewSize();
        setBannerImage();
        setPriceText(itemObj);
        checkSavedValues();
        getPlanDetails();
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    Utils.openHome(FreeTestPackageDetailsActivity.this);
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackagesNewTask(FreeTestPackageDetailsActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackagesNewTask(FreeTestPackageDetailsActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(FreeTestPackageDetailsActivity.this);
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
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "PackageDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(FreeTestPackageDetailsActivity.this)){
                    return;
                }
                JSONObject data = response.optJSONObject("Body");
                saveObject(data);
                setUI(data);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void setUI(JSONObject data){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        JSONArray schedule = data.optJSONArray("Schedule");
        data = data.optJSONObject("PlanDetails");
        packageName = data.optString("OrgPlanName");
        setViews(data);
        setScheduleView(schedule);
        if(data.optBoolean("IsPurchased")){
            alreadyEntrolled();
        }
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
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    private void alreadyEntrolled(){
        ((TextView)findViewById(R.id.buyButton)).setText("Enrolled");
        findViewById(R.id.buyButton).setEnabled(false);
        findViewById(R.id.buyButton).setBackgroundResource(R.drawable.button_green_rounded_25dp);
    }

    private void setViews(JSONObject data){
        String description  = data.optString("Highlights");
        ZTWebView descriptionView = findViewById(R.id.description);
        descriptionView.loadData(description, "text/html", "UTF-8");
        setPriceText(data);
        setTitle(data.optString("OrgPlanName"));
        ((TextView)findViewById(R.id.startDate)).setText(data.optString("DurationText"));
        ((TextView)findViewById(R.id.endDate)).setText(data.optString("EndDate"));
        ((TextView)findViewById(R.id.totalTests)).setText(data.optString("TotalExam"));
        setBannerImage();
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
    public void onClick(View v) {
        if (v.getId() ==  R.id.freeDemo) {
            startDemoTest();
        } else if(v.getId() ==  R.id.schedule) {
            if (Utils.isValidString(pdfPath)) {
                Intent intent = new Intent(FreeTestPackageDetailsActivity.this, PdfOpenActivity.class);
                intent.putExtra("fileName", pdfPath);
                intent.putExtra("downloadPath", ServerApi.PDF_BASE_PATH);
                intent.putExtra("basePath", getFilesDir() + "/pdf/");
                startActivity(intent);
            }
        } else if(v.getId() == R.id.buyButton){
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
    private void setScheduleView(JSONArray array){
        for (int i = 0 ; i < array.length() ; i++){
            if(array.optJSONObject(i).optInt("SpecializationId") == Preferences.get(getApplicationContext(), Preferences.KEY_SPEC_ID, 0)){
                pdfPath = array.optJSONObject(i).optString("FilePath");
                if(Utils.isValidString(pdfPath)){
                    findViewById(R.id.schedule).setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    private void checkSavedValues(){
        JSONObject data = getItemObj();
        if(data != null){
            setUI(data);
        }
    }
    private JSONObject getItemObj(){
        try{
            HashMap<Integer, String> detailsMaps = (HashMap<Integer, String>) Utils.getObject(this, "seriesDetails");
            if(detailsMaps != null){
                String item = detailsMaps.get(itemObj.optInt("OrgPlanID"));
                if(Utils.isValidString(item)){
                    return new JSONObject(item);
                }
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        return null;
    }
    private void saveObject(JSONObject data){
        HashMap<Integer, String> detailsMaps = null;
        try{
            detailsMaps = (HashMap<Integer, String>) Utils.getObject(this, "seriesDetails");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(detailsMaps == null){
            detailsMaps = new HashMap<>();
        }
        detailsMaps.put(itemObj.optInt("OrgPlanID"), data.toString());
        Utils.saveObject(this, detailsMaps, "seriesDetails");
    }

    private void startPayment(){
        Intent intent = new Intent(this, CartActivity.class);
        intent.putExtra(CartActivity.EXTRA_PACKAGE_ID, itemObj.optInt("OrgPlanID"));
        intent.putExtra(CartActivity.EXTRA_SUBJECT_ID, 0);
        intent.putExtra(CartActivity.EXTRA_SCREEN_TYPE, "test");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent,  100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK){
            finish();
        }
    }

    private void startDemoTest(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        TestUtils.practiceExamByCourse(this, itemObj.optInt("CourseID"),itemObj.optInt("OrgPlanID"), new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray array = response.optJSONArray("Body");
                if(array != null && array.length() > 0){
                    JSONObject item = array.optJSONObject(0);
                    Intent intent = new Intent(FreeTestPackageDetailsActivity.this, TestActivity.class);
                    intent.putExtra("item", item.toString());
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "No Free demo available for "+itemObj.optString("OrgPlanName"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
