package com.zonetech.online.testseries;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.classes.ClassPackageDetailsActivity;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestSeriesDetailsActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestActivity;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestResultActivity;
import com.zonetech.online.payment.CartActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.publication.PublicationCategoryActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class TestPackageDetailsActivity extends ZTAppCompatActivity implements View.OnClickListener {
    private ImageView bannerImage;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeigth;
    private JSONObject itemObj;
    private JSONObject demoTestItem;
    private RecyclerView detailsList;
    private JSONArray finalDetailsList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);
        metrics = Utils.getMetrics(this);
        bannerImage = findViewById(R.id.bannerImage);
        detailsList = findViewById(R.id.detailsList);
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
        findViewById(R.id.buyButton).setOnClickListener(this);
        setImageViewSize();
        setBannerImage();
        setPriceText(itemObj);
        getPlanDetails();
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    Utils.openHome(TestPackageDetailsActivity.this);
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(TestPackageDetailsActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(TestPackageDetailsActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(TestPackageDetailsActivity.this);
                }
                return false;
            }
        });
    }

    private void getPlanDetails(){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(this));
            params.put("SpecializationID", Utils.getStudentSpecID(this));
            params.put("OrgPlanID", itemObj.optInt("OrgPlanID"));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.TESTING_BASE_URL, "PackageDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(TestPackageDetailsActivity.this)){
                    return;
                }
                JSONObject data = response.optJSONObject("Body");
                getPlanListDetails(data);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    TestPackageDetailsAdapter testPackageDetailsAdapter;

    private void setUI(JSONArray array, JSONObject data){
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        setListView(array);
        setViews(data.optJSONObject("PlanDetails"));
    }

    private void setListView(JSONArray array){
        if(testPackageDetailsAdapter != null){
            testPackageDetailsAdapter.refreshAdapter(array);
        }else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            detailsList.setLayoutManager(linearLayoutManager);
            testPackageDetailsAdapter = new TestPackageDetailsAdapter(array, this);
            detailsList.setAdapter(testPackageDetailsAdapter);
        }
    }

    private void setViews(JSONObject data){
        setPriceText(data);
        setTitle(data.optString("OrgPlanName"));
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
        if  (v.getId() == R.id.buyButton){
                startPayment();
        }
    }

    public void openPdf(String pdfPath){
        if(Utils.isValidString(pdfPath)){
            Intent intent = new Intent(TestPackageDetailsActivity.this, PdfOpenActivity.class);
            intent.putExtra("fileName", pdfPath);
            intent.putExtra("downloadPath", ServerApi.PDF_BASE_PATH);
            intent.putExtra("basePath", getFilesDir() + "/pdf/");
            startActivity(intent);
        }
    }
    public void startPayment(){
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

    private void getPlanListDetails(JSONObject detailData){
        JSONObject params = new JSONObject();
        try{
            int specializationId = Preferences.get(getApplicationContext(), Preferences.KEY_SPEC_ID, 0);
            params.put("OrgPlanID", itemObj.optInt("OrgPlanID"));
            params.put("StudentID", Utils.getStudentId(getApplicationContext()));
            params.put("SpecializationID", specializationId);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.TESTING_BASE_URL, "ExamByPackage", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                try {
                    if (Utils.isActivityDestroyed(TestPackageDetailsActivity.this)) {
                        return;
                    }
                    JSONArray data = response.optJSONArray("Body");
                    finalDetailsList = new JSONArray();
                    detailData.put("itemType", 0);
                    finalDetailsList.put(detailData);
                    for (int i = 0; i < data.length(); i++) {
                        data.optJSONObject(i).put("itemType", 1);
                        finalDetailsList.put(data.optJSONObject(i));
                    }
                    setUI(finalDetailsList, new JSONObject(detailData.toString()));
                    fetchDemoTest();
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if(Utils.isActivityDestroyed(TestPackageDetailsActivity.this)){
                    return;
                }
            }
        });
    }

    private void fetchDemoTest(){
        TestUtils.practiceExamByCourse(this, itemObj.optInt("CourseID"),itemObj.optInt("OrgPlanID"), new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                try {
                    JSONArray array = response.optJSONArray("Body");
                    if (array != null && array.length() > 0) {
                        demoTestItem = array.optJSONObject(0);
                        if (demoTestItem != null && demoTestItem.length() > 0) {
                            demoTestItem.put("isDemo", true);
                            demoTestItem.put("itemType",1);
                            JSONArray newArray = new JSONArray();
                            newArray.put(finalDetailsList.optJSONObject(0));
                            newArray.put(demoTestItem);
                            for (int i = 1 ; i < finalDetailsList.length() ; i++){
                                if(finalDetailsList.optJSONObject(i).optInt("ExamID") == demoTestItem.optInt("ExamID")){
                                    continue;
                                }
                                newArray.put(finalDetailsList.optJSONObject(i));
                            }
                            finalDetailsList = new JSONArray(newArray.toString());
                            setListView(finalDetailsList);
                        }
                    }
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void error(String error) {
            }
        });
    }

    public void startDemoTest(){
        if(demoTestItem.optInt("ExamStatus") == 3){
            Intent intent = new Intent(this, TestResultActivity.class);
            intent.putExtra("studentExamID", demoTestItem.optInt("StudentExamID"));
            intent.putExtra("title", demoTestItem.optString("ExamName"));
            intent.putExtra("examDuration", demoTestItem.optInt("ExamDuration"));
            startActivity(intent);
        }else {
            Intent intent = new Intent(TestPackageDetailsActivity.this, TestActivity.class);
            intent.putExtra("item", demoTestItem.toString());
            startActivity(intent);
        }
    }
}
