package com.zonetech.online.classes;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.zonetech.online.InitialScreenActivity;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.SectionsPagerAdapter;
import com.zonetech.online.mypackage.onlineClass.ClassVideoListActivity;
import com.zonetech.online.payment.CartActivity;
import com.zonetech.online.payment.PaymentUtils;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.testseries.TestPackageDetailsActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClassPackageDetailsActivity extends ZTAppCompatActivity implements ClassProductItemAdapter.ClickListener, View.OnClickListener {
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
        setContentView(R.layout.activity_class_details);
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
                    Utils.openHome(ClassPackageDetailsActivity.this);
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(ClassPackageDetailsActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(ClassPackageDetailsActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(ClassPackageDetailsActivity.this);
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
        ServerApi.callServerApi(this, ServerApi.TESTING_BASE_URL, "OnlineClassPackageDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(ClassPackageDetailsActivity.this)){
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
            String highlights  = data.optString("Highlights");
            ZTWebView descriptionView = findViewById(R.id.description);
            descriptionView.loadData(highlights, "text/html", "UTF-8");
            setPriceText(data);
            packageName = data.optString("OrgPlanName");
            setTitle(packageName);
            TextView startDate = findViewById(R.id.StartDate);
            TextView totalTests = findViewById(R.id.totalTests);
            totalTests.setText("Total Course Duration : " + data.optString("TotalExam") +" + Hours");
            TextView validity = findViewById(R.id.validity);
            validity.setText("Course Validity : " + data.optString("DurationText"));

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
                adapter = new ClassProductItemAdapter(productArray,R.layout.class_product_subject_item, ClassPackageDetailsActivity.this, this, false);
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(productArray);
            }
            if(data.optJSONObject("TestSeriesPlan") != null && data.optJSONObject("TestSeriesPlan").optInt("DefaultPlanID") > 0){
                showFreeTestSeries(data.optJSONObject("TestSeriesPlan"));
            }
        }
        findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    private void setPriceText(JSONObject data){
        try{
            JSONArray organizationPlanValidityList = data.optJSONArray("OrganizationPlanValidityList");
            if(organizationPlanValidityList != null && organizationPlanValidityList.length() > 0){
                setPricePlanLayout(organizationPlanValidityList);
            }else{
                setBottomPriceView(data);
                findViewById(R.id.rootPricePlanLayout).setVisibility(View.GONE);
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private void setBottomPriceView(JSONObject data){
        try {
            TextView priceView = findViewById(R.id.bottomPriceView);
            double mrp = data.optDouble("PlanMRP");
            double price = data.optDouble("Fees");
            String currency = getString(R.string.currency);
            long discount = Math.round(((mrp - price) / mrp) * 100);
            String mrpString = currency + Math.round(mrp);
            String priceString = currency + Math.round(price);
            String text = String.format(getResources().getString(R.string.price_value), mrpString, priceString, discount + "%");
            int index = text.indexOf(mrpString);
            SpannableString strNew = new SpannableString(text);
            StrikethroughSpan span = new StrikethroughSpan();
            strNew.setSpan(span, index, index + String.valueOf(mrpString).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            priceView.setText(strNew);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
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
    public void buy(int subjectId, int mrp, int fees, int duration) {
//        Intent intent = new Intent(this, CartActivity.class);
//        intent.putExtra(CartActivity.EXTRA_PACKAGE_ID, itemObj.optInt("OrgPlanID"));
//        intent.putExtra(CartActivity.EXTRA_SUBJECT_ID, subjectId);
//        intent.putExtra(CartActivity.EXTRA_SCREEN_TYPE, "class");
//        if(duration > 0) {
//            intent.putExtra(CartActivity.EXTRA_SCREEN_DURATION, duration);
//            intent.putExtra(CartActivity.EXTRA_SCREEN_MRP, mrp);
//            intent.putExtra(CartActivity.EXTRA_SCREEN_FEES, fees);
//        }
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivityForResult(intent, 200);
        showDialog();
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
        if (v.getId() ==  R.id.buyButton){
                if(selectPlan != null){
                    buy(0, selectPlan.optInt("MRP"), selectPlan.optInt("Fees"), selectPlan.optInt("PlanDuration"));
                }else{
                    buy(0, 0, 0, 0);
                }
        }
    }
    JSONObject selectPlan;
    private void setPricePlanLayout(JSONArray array){
        try {
            LinearLayout pricePlanLayout = findViewById(R.id.pricePlanLayout);
            pricePlanLayout.removeAllViews();
            for (int i = 0; i < array.length(); i++) {
                JSONObject data = array.optJSONObject(i);
                View view = LayoutInflater.from(pricePlanLayout.getContext()).inflate(R.layout.plan_price_item_layout, pricePlanLayout, false);
                TextView priceView = view.findViewById(R.id.planPriceView);
                AppCompatRadioButton radioButton = view.findViewById(R.id.planRadioButton);
                double mrp = data.optDouble("MRP");
                data.put("PlanMRP", mrp);
                double price = data.optDouble("Fees");
                final int planDuration = data.optInt("PlanDuration");
                String currency = getString(R.string.currency);
                long discount = Math.round(((mrp - price) / mrp) * 100);
                String mrpString = currency + Math.round(mrp);
                String priceString = currency + Math.round(price);
                String text = String.format(getResources().getString(R.string.plan_price_item_value), mrpString, priceString, discount + "%", planDuration);
                int index = text.indexOf(mrpString);
                SpannableString strNew = new SpannableString(text);
                StrikethroughSpan span = new StrikethroughSpan();
                strNew.setSpan(span, index, index + mrpString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                priceView.setText(strNew);
                int finalI = i;
                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            setBottomPriceView(data);
                            selectPlan = data;
                            setPlanPrice(finalI);
                        }
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioButton.setChecked(true);
                    }
                });
                if (i == 0) {
                    radioButton.setChecked(true);
                }
                pricePlanLayout.addView(view);
            }
            findViewById(R.id.rootPricePlanLayout).setVisibility(View.VISIBLE);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
    private void setPlanPrice(int index){
        LinearLayout pricePlanLayout = findViewById(R.id.pricePlanLayout);
        for (int j = 0 ; j < pricePlanLayout.getChildCount() ; j++){
            if(index != j){
                ((AppCompatRadioButton)pricePlanLayout.getChildAt(j).findViewById(R.id.planRadioButton)).setChecked(false);
            }else{
                ((AppCompatRadioButton)pricePlanLayout.getChildAt(j).findViewById(R.id.planRadioButton)).setChecked(true);
            }
        }
    }

    private void showFreeTestSeries(JSONObject testSeriesPlan){
        try {
            String title = testSeriesPlan.optString("OrgPlanName");
            String subTitle = "Total Exams: " + testSeriesPlan.optString("TotalExam");
            ((TextView)findViewById(R.id.freeTestTitle)).setText(title + " (Free with online class)");
            ((TextView)findViewById(R.id.freeTestSubTitle)).setText(subTitle);
            findViewById(R.id.freeTestSeries).setVisibility(View.VISIBLE);
            String imagePath = testSeriesPlan.optString("ImageURL");
            if(Utils.isValidString(imagePath)) {
                imagePath = ServerApi.IMAGE_URL + imagePath;
                if(Utils.isActivityDestroyed(this)){
                    return;
                }
                Glide.with(this)
                        .load(imagePath)
                        .error(R.drawable.samplepackage)
                        .placeholder(R.drawable.samplepackage)
                        .into(((ImageView)findViewById(R.id.freeTestImage)));
            }
            findViewById(R.id.freeTestSeries).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        JSONObject item = new JSONObject();
                        item.put("PlanMRP", testSeriesPlan.optString("PlanMRP"));
                        item.put("Fees", testSeriesPlan.optString("Fees"));
                        item.put("ImageURL", testSeriesPlan.optString("ImageURL"));
                        item.put("OrgPlanID", testSeriesPlan.optString("OrgPlanID"));
                        item.put("CourseID", testSeriesPlan.optString("CourseID"));
                        item.put("OrgPlanName", testSeriesPlan.optString("OrgPlanName"));

                        if(Utils.isLollipop()){
                            Intent intent = new Intent(ClassPackageDetailsActivity.this, TestPackageDetailsActivity.class);
                            intent.putExtra("item", item.toString());
                            intent.putExtra("position", 0);
                            ((ImageView)findViewById(R.id.freeTestImage)).setTransitionName("test_"+0);
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ClassPackageDetailsActivity.this, ((ImageView)findViewById(R.id.freeTestImage)), "test_"+0);
                            startActivity(intent, options.toBundle());
                        }else {
                            Intent intent = new Intent(ClassPackageDetailsActivity.this, TestPackageDetailsActivity.class);
                            intent.putExtra("item", item.toString());
                            startActivity(intent);
                        }
                    }catch (Exception e){
                        if(Utils.isDebugModeOn){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
    AlertDialog dialog;
    public void showDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.alert_dialog, null);
            TextView message = promptsView.findViewById(R.id.message);
            String styledText = "This is <font color='red'>simple</font>.";
            String html = "Dear Students ,<br>From Today Onwards All AEn and JEn Online Classes will be available on<br><font color='red'><b>ZONE TECH DIGITAL</b></font> App.<br>Click here to download the app";
            message.setText(Html.fromHtml(html.trim()), TextView.BufferType.SPANNABLE);
            TextView download = promptsView.findViewById(R.id.ok);
            download.setText("Download");
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    String url = "http://on-app.in/app/home?orgCode=yamgu&referrer=utm_source%3Dcopy-link%26utm_medium%3Dstudent-app-referral";
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    try {
                        startActivity(browserIntent);
                    }catch (Exception e){
                        if(Utils.isDebugModeOn){
                            e.printStackTrace();
                        }
                    }
                }
            });
            builder.setView(promptsView);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }
}
