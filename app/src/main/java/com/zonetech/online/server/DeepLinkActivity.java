package com.zonetech.online.server;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.zonetech.online.SplashActivity;
import com.zonetech.online.classes.ClassPackageDetailsActivity;
import com.zonetech.online.classes.ClassPackagePlanActivity;
import com.zonetech.online.common.CommonWebViewActivity;
import com.zonetech.online.dailyQuiz.DailyQuizActvity;
import com.zonetech.online.freecourses.FreeCoursesActivity;
import com.zonetech.online.mypackage.MyPackageActivity;
import com.zonetech.online.news.NewsActivity;
import com.zonetech.online.offers.OfferActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.testseries.TestPackageDetailsActivity;
import com.zonetech.online.testseries.TestSeriesPlanActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class DeepLinkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        checkForDeepLink(deepLink == null ? null : deepLink.toString());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        checkForDeepLink(null);
                    }
                });
    }
    private void checkForDeepLink(String deepLink){
        try {
            if (!Utils.isValidString(deepLink)) {
                deepLink = getIntent().getExtras().getString("url");
                if (getIntent() != null && getIntent().hasExtra("url")) {
                    deepLink = getIntent().getStringExtra("url");
                } else {
                    if (getIntent() != null && getIntent().getData() != null) {
                        deepLink = getIntent().getData().toString();
                    }
                }
            }
            Intent intent = new Intent(DeepLinkActivity.this, SplashActivity.class);
            if(deepLink.startsWith("https://zonetech.in")){
                String delims = "[/]+";
                String[] tokens = String.valueOf(deepLink).split(delims);
                if(Utils.isLoginCompleted(getApplicationContext()) && tokens.length > 2){
                    String token2 = tokens[2];
                    switch (token2){
                        case "onlineClasses":
                            intent = onlineClass(tokens);
                            break;
                        case "onlineTestSeries":
                            intent = onlineTestSeries(tokens);
                            break;
                        case "publications":
                            intent = new Intent(DeepLinkActivity.this, PublicationActivity.class);
                            break;
                        case "myPackages":
                            intent = new Intent(DeepLinkActivity.this, MyPackageActivity.class);
                            break;
                        case "freeCourses":
                            intent = new Intent(DeepLinkActivity.this, FreeCoursesActivity.class);
                            break;
                        case "dailyQuiz":
                            intent = new Intent(DeepLinkActivity.this, DailyQuizActvity.class);
                            break;
                        case "claimPromoCode":
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://onlinezonetech.in/home/claimpromocode"));
                            break;
                        case "offers":
                            intent = new Intent(DeepLinkActivity.this, OfferActivity.class);
                            break;
                        case "news":
                            intent = new Intent(DeepLinkActivity.this, NewsActivity.class);
                            break;
                        default:
                            intent = new Intent(DeepLinkActivity.this, CommonWebViewActivity.class);
                            intent.putExtra("url", deepLink);
                    }
                }else{
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }else{
                intent = new Intent(DeepLinkActivity.this, CommonWebViewActivity.class);
                intent.putExtra("url", deepLink);
            }
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(intent);
            stackBuilder.startActivities();
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        finish();
    }

    private Intent onlineClass(String[] tokens){
        Intent intent = new Intent(DeepLinkActivity.this, ClassPackagePlanActivity.class);
        if(tokens.length > 3){
            try{
                JSONObject object = new JSONObject();
                object.put("OrgPlanID", Integer.parseInt(tokens[3]));
                object.put("OrgPlanName", "Online Class");
                intent = new Intent(DeepLinkActivity.this, ClassPackageDetailsActivity.class);
                intent.putExtra("item", object.toString());
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        return intent;
    }

    private Intent onlineTestSeries(String[] tokens){
        Intent intent = new Intent(DeepLinkActivity.this, TestSeriesPlanActivity.class);
        if(tokens.length > 3){
            try{
                JSONObject object = new JSONObject();
                object.put("OrgPlanID", Integer.parseInt(tokens[3]));
                object.put("OrgPlanName", "Online Test Series");
                intent = new Intent(DeepLinkActivity.this, TestPackageDetailsActivity.class);
                intent.putExtra("item", object.toString());
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
        }
        return intent;
    }
}
