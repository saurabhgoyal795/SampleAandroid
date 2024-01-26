package com.zonetech.online.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.install.model.ActivityResult;
import com.zonetech.online.BaseApplication;
import com.zonetech.online.BuildConfig;
import com.zonetech.online.ContactUsActivity;
import com.zonetech.online.R;
import com.zonetech.online.classes.ClassPackagePlanActivity;
import com.zonetech.online.common.CommonWebViewActivity;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.downloads.MyDownloadActivity;
import com.zonetech.online.login.LoginActivity;
import com.zonetech.online.mypackage.MyPackageActivity;
import com.zonetech.online.mypackage.MyPackageAdapter;
import com.zonetech.online.news.NewsActivity;
import com.zonetech.online.notification.NotificationActivity;
import com.zonetech.online.notification.ZTFirebaseMessagingService;
import com.zonetech.online.offers.OfferActivity;
import com.zonetech.online.payment.CartActivity;
import com.zonetech.online.payment.PaymentUtils;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.profile.NewProfileActivity;
import com.zonetech.online.profile.ProfileActivity;
import com.zonetech.online.profile.TransactionActivity;
import com.zonetech.online.remoteConfig.RemoteConfig;
import com.zonetech.online.server.DownloadContent;
import com.zonetech.online.server.InAppUpdate;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.server.UpdateActivity;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.CountDrawable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends ZTAppCompatActivity implements View.OnClickListener {
    Menu defaultMenu;
    ActionBarDrawerToggle mDrawerToggle;
    private ExpandableListView expListView;
    private HashMap<String, List<String>> listDataChild;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
//    R.drawable.baseline_home_black_24,
    private int[] icons = {
        R.drawable.baseline_account_circle_white_24,
        R.drawable.baseline_group_black_24,
            R.drawable.baseline_code_black_18,
            R.drawable.outline_payment_black_24,
            R.drawable.baseline_local_offer_black_24dp,
            R.drawable.icons8news24,
            R.drawable.onlineclass_old,
            R.drawable.baseline_emoji_events_white_24,
            R.drawable.baseline_share_black_24,
            R.drawable.baseline_follow_the_signs_black_24,
            R.drawable.baseline_contact_support_black_24,
            R.drawable.logout
    };
    DrawerLayout drawer;
    BottomNavigationView navView;
    boolean mIsReentering;
    public static String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        navView = findViewById(R.id.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        setUpDrawer();
        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commitAllowingStateLoss();
        ((TextView)findViewById(R.id.userName)).setText(Utils.getName(this));
        ((TextView)findViewById(R.id.userBranch)).setText(Utils.getStudentSpec(this));
        findViewById(R.id.userImage).setOnClickListener(this);
        navView.getMenu().getItem(0).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
              if (item.getItemId() == R.id.navigation_classes) {
                    openMyPackages( 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    openMyPackages(1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    openMyDownload();
                }
                return false;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showWelcomeDialog();
            }
        }, 2000);
        Utils.getCourseSpec(this, null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkStudentSession();
            }
        }, 3000);
        Utils.subscribeToTopic("zonetech_user");
        ZTFirebaseMessagingService.saveToken(getApplicationContext());
        if (Utils.isLollipop()) {
            InAppUpdate.checkForAppUpdate(this, new InAppUpdate.AppListener() {
                @Override
                public void updateDownloading(int max, int progress) {
                    showDownloadDialog(max, progress);
                }
                @Override
                public void updateCompleted() {
                    if(progressDialog != null){
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void updateNotAvailable() {
                    if(Utils.isForceUpdateEnabled(getApplicationContext())){
                        startActivity(new Intent(MainActivity.this, UpdateActivity.class));
                    }
                }
            });
        }
        ((TextView)findViewById(R.id.versionText)).setText("Version: "+BuildConfig.VERSION_NAME);
        DownloadContent.resumeDownload(getApplicationContext());
    }

    private void setProfileImage(){
        DisplayMetrics metrics = Utils.getMetrics(this);
        String fileName = Preferences.get(getApplicationContext(), Preferences.KEY_STUDENT_PROFILE_PIC, "");
        if(Utils.isValidString(fileName)){
            Glide.with(this)
                    .load(ServerApi.PROFILE_BASE_PATH + fileName)
                    .override((int)(60*metrics.density), (int)(60*metrics.density))
                    .circleCrop()
                    .into((ImageView)findViewById(R.id.userImage));
        }
    }

    private void openMyPackages(int screen){
        Intent intent = new Intent(this, MyPackageActivity.class);
        intent.putExtra("position", screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void openMyProfile(){
        Intent intent = new Intent(this, NewProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void openMyDownload(){
        Intent intent = new Intent(this, MyDownloadActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        defaultMenu = menu;
        setCount(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.whatsapp) {
            openWhatsApp();
            return true;
        } else if (item.getItemId() == R.id.cart) {
            openCart();
            return true;
        } else if (item.getItemId() == R.id.profile) {
            openCart();
            return true;
        } else if (item.getItemId() == R.id.notification) {
            openNotification();
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    private void openWhatsApp(){
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=+916367244454";
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private void openCart(){
        startActivity(new Intent(this, CartActivity.class));
    }

    private void openNotification() {
        startActivity(new Intent(this, NotificationActivity.class));
    }

    private void openProfile(){
        startActivity(new Intent(this, NewProfileActivity.class));

    }

    @Override
    protected void onStart() {
        super.onStart();
        setProfileImage();
        if(-1 == Preferences.get(getApplicationContext(), Preferences.KEY_CART_COUNT, -1)){
            PaymentUtils.getCart(this, new CompleteListener() {
                @Override
                public void success(JSONObject response) {
                    JSONObject data = response.optJSONObject("Body");
                    JSONArray items = data.optJSONArray("Items");
                    if(items != null && items.length() > 0) {
                        Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, items.length());
                        setCount(MainActivity.this);
                    }else{
                        Preferences.put(getApplicationContext(), Preferences.KEY_CART_COUNT, 0);
                    }
                }

                @Override
                public void error(String error) {

                }
            });
        }else{
            setCount(this);
        }
        RemoteConfig.fetchRemoteConfig(this);
    }

    public void setCount(Context context) {
        if(defaultMenu == null){
            return;
        }
        int count = Preferences.get(getApplicationContext(), Preferences.KEY_CART_COUNT, -1);
        if(count >= 0){
            MenuItem menuItem = defaultMenu.findItem(R.id.cart);
            LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
            Drawable reuse = icon.findDrawableByLayerId(R.id.cartCount);
            CountDrawable badge;
            if (reuse != null && reuse instanceof CountDrawable) {
                badge = (CountDrawable) reuse;
            } else {
                badge = new CountDrawable(context);
            }
            badge.setCount(count+"");
            icon.mutate();
            icon.setDrawableByLayerId(R.id.cartCount, badge);
        }
        getNotificationCount();
    }

    public void setNotificationCount(Context context, int count) {
        if(defaultMenu == null){
            return;
        }
        if(count >= 0){
            MenuItem menuItem = defaultMenu.findItem(R.id.notification);
            LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
            Drawable reuse = icon.findDrawableByLayerId(R.id.notificationCount);
            CountDrawable badge;
            if (reuse != null && reuse instanceof CountDrawable) {
                badge = (CountDrawable) reuse;
            } else {
                badge = new CountDrawable(context);
            }
            badge.setCount(count+"");
            icon.mutate();
            icon.setDrawableByLayerId(R.id.notificationCount, badge);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void setUpDrawer() {
        expListView = (ExpandableListView) findViewById(R.id.list_slidermenu);
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader,
                listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);
        // expandable list view click listener

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                boolean isDrawerClose = false;
                switch (groupPosition){
//                    case 0: //home
//                        isDrawerClose = true;
//                        break;
                    case 0: //my profile
                        openProfile();
                        isDrawerClose = true;
                        break;
                    case 1: //about us
                        openCommonWebView("https://zonetech.in/about-zonetech");
                        isDrawerClose = true;
                        break;
                    case 2: //promo code
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://onlinezonetech.in/home/claimpromocode"));
                        startActivity(browserIntent);
                        isDrawerClose = true;
                        break;
//                    case 3:  //my cart
//                        openCart();
//                        isDrawerClose = true;
//                        break;
                    case 3:  //transaction history
                        startActivity(new Intent(MainActivity.this, TransactionActivity.class));
                        isDrawerClose = true;
                        break;
//                    case 5:  //my downloads
//                        openDownloads();
//                        isDrawerClose = true;
//                        break;
                    case 4:  //my offer
                        startActivity(new Intent(MainActivity.this, OfferActivity.class));
                        isDrawerClose = true;
                        break;
                    case 5:  //news
                        startActivity(new Intent(MainActivity.this, NewsActivity.class));
                        isDrawerClose = true;
                        break;
                    case 8:   //share the app
                        shareApp();
                        isDrawerClose = true;
                        break;
                    case 9:   //follow us
                        showDialog();
                        isDrawerClose = true;
                        break;
                    case 10:  //contact us
                        startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                        isDrawerClose = true;
                        break;
                    case 11:  //logout
                        logout();
                        isDrawerClose = true;
                        break;
                }
                if(isDrawerClose) {
                    expListView.setItemChecked(groupPosition, true);
                    drawer.closeDrawer(Gravity.LEFT);
                }
                return false;
            }
        });
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                v.setSelected(true);
                switch (groupPosition) {
                    case 6: //student corner
                        switch (childPosition){
                            case 0: // downloads
                                openCommonWebView("https://www.zonetech.in/downloads");
                                break;
                            case 1: //job notificaitons
                                openCommonWebView("https://www.zonetech.in/job-notification");
                                break;
                            case 2: //submit result
                                openCommonWebView("https://www.zonetech.in/examresult");
                                break;
                            case 3: //interview
                                openCommonWebView("https://www.zonetech.in/rpsc-aen-interview-Guidance-programme");
                                break;
                            case 4: //ZT blog
                                openCommonWebView("https://blog.zonetech.in");
                                break;
                        }
                        break;
                    case 7:   // our achievements
                        switch (childPosition){
                            case 0: //selections
                                openCommonWebView("https://www.zonetech.in/achievements");
                                break;
                            case 1: //testimonials
                                openCommonWebView("https://www.zonetech.in/testimonials");
                                break;
                        }
                        break;
                }
                expListView.setItemChecked(childPosition, true);
                drawer.closeDrawer(Gravity.LEFT);
                return false;
            }
        });
    }
    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        String[] array = getResources().getStringArray(R.array.nav_drawer_items);
        listDataHeader = Arrays.asList(array);
        List<String> studentCorner = Arrays.asList(getResources().getStringArray(R.array.student_corner_array));
        List<String> achievements = Arrays.asList(getResources().getStringArray(R.array.achievements_array));

        listDataChild.put(listDataHeader.get(0), new ArrayList<>());
        listDataChild.put(listDataHeader.get(1), new ArrayList<>());
        listDataChild.put(listDataHeader.get(2), new ArrayList<>());
        listDataChild.put(listDataHeader.get(3), new ArrayList<>());
        listDataChild.put(listDataHeader.get(4), new ArrayList<>());
        listDataChild.put(listDataHeader.get(5), new ArrayList<>());
        listDataChild.put(listDataHeader.get(6), studentCorner);
        listDataChild.put(listDataHeader.get(7), achievements);
        listDataChild.put(listDataHeader.get(8), new ArrayList<>());
        listDataChild.put(listDataHeader.get(9), new ArrayList<>());
        listDataChild.put(listDataHeader.get(10), new ArrayList<>());
        listDataChild.put(listDataHeader.get(11), new ArrayList<>());
//        listDataChild.put(listDataHeader.get(12), new ArrayList<>());
//        listDataChild.put(listDataHeader.get(13), new ArrayList<>());
//        listDataChild.put(listDataHeader.get(14), new ArrayList<>());
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.userImage:
//                openProfile();
//                if (drawer.isDrawerOpen(GravityCompat.START)) {
//                    drawer.closeDrawer(GravityCompat.START);
//                }
//                break;
//            case R.id.facebook:
//                openFollowUs("facebook");
//                break;
//            case R.id.telegram:
//                openFollowUs("telegram");
//                break;
//            case R.id.youtube:
//                openFollowUs("youtube");
//                break;
//            case R.id.instagram:
//                openFollowUs("instagram");
//                break;
//            case R.id.linkedin:
//                openFollowUs("linkedin");
//                break;
//            case R.id.twitter:
//                openFollowUs("twitter");
//                break;
//            case R.id.cancel:
//                if(dialog != null){
//                    dialog.dismiss();
//                }
//                break;
//        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        private HashMap<String, List<String>> _listDataChild;

        public ExpandableListAdapter(Context context,
                                     List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(
                    this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition,
                    childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.drawer_item_submenu, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblListItem);

            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(
                    this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.drawer_item_menu, null);
            }
            ImageView txt_plusminus =  convertView.findViewById(R.id.plus_txt);
            if(getChildrenCount(groupPosition) == 0){
                txt_plusminus.setVisibility(View.GONE);
            }else{
                txt_plusminus.setVisibility(View.VISIBLE);
            }
            if (isExpanded) {
                txt_plusminus.setRotation(270);
            } else {
                txt_plusminus.setRotation(90);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setText(headerTitle);
            ImageView imgListGroup = (ImageView) convertView
                    .findViewById(R.id.ic_txt);
            imgListGroup.setImageResource(icons[groupPosition]);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private void openCommonWebView(String url){
        Intent intent = new Intent(MainActivity.this, CommonWebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    private void shareApp(){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ZONE TECH");
            String shareMessage= "\n"+getString(R.string.zontech)+"\n\nDownload app now:\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Choose one"));
        } catch(Exception e) {
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        mIsReentering = true;
        super.onActivityReenter(resultCode, data);
    }
    AlertDialog dialog;
    private void showDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.follow_popup, null);
            View facebook = promptsView.findViewById(R.id.facebook);
            View telegram = promptsView.findViewById(R.id.telegram);
            View youtube = promptsView.findViewById(R.id.youtube);
            View instagram = promptsView.findViewById(R.id.instagram);
            View linkedin = promptsView.findViewById(R.id.linkedin);
            View twitter = promptsView.findViewById(R.id.twitter);
            View cancel = promptsView.findViewById(R.id.cancel);
            builder.setView(promptsView);
            dialog = builder.create();
            cancel.setOnClickListener(this);
            facebook.setOnClickListener(this);
            telegram.setOnClickListener(this);
            youtube.setOnClickListener(this);
            instagram.setOnClickListener(this);
            linkedin.setOnClickListener(this);
            twitter.setOnClickListener(this);
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    private void openFollowUs(String type){
        if(dialog != null){
            dialog.dismiss();
        }
        String url = Utils.getFollowUsUrl(type);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            openCommonWebView(url);
        }
    }

    private void showWelcomeDialog(){
        if(Preferences.get(getApplicationContext(), Preferences.KEY_IS_WELCOME_MSG_SHOWN, false)){
            ServerApi.alertMsg(this, new ServerApi.CompleteListener() {
                @Override
                public void response(JSONObject response) {
                    response = response.optJSONObject("Body");
                    msg = response.optString("Descriptions");
                    if(Utils.isValidString(msg ) && BaseApplication.isShownAlert== false) {
                        BaseApplication.isShownAlert = true;
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        intent.putExtra("title", response.optString("Title"));
                        startActivity(intent);
                    }
                }

                @Override
                public void error(String error) {

                }
            });
        }else{
            ServerApi.welcomeMsg(this, new ServerApi.CompleteListener() {
                @Override
                public void response(JSONObject response) {
                    msg = response.optString("Message");
                    if(Utils.isValidString(msg)) {
                        msg = msg.replace("{Student name}", Utils.getName(MainActivity.this));
                        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void error(String error) {

                }
            });
        }
    }

    private void logout(){
        startActivity(new Intent(this, LogoutActivity.class));
    }

    private void openDownloads(){
        startActivity(new Intent(this, MyDownloadActivity.class));
    }

    private void getNotificationCount(){
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "StudentNotificationCount/"+Utils.getStudentId(this), null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                int count = response.optInt("RecordCount");
                setNotificationCount(getApplicationContext(), count);
            }

            @Override
            public void error(String error) {

            }
        });
    }

    private void checkStudentSession(){
        JSONObject params = new JSONObject();
        try{
            String deviceType = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            if ( Utils.getStudentSessionId(this) == 0) {
                return;
            }
            params.put("StudentSessionID", Utils.getStudentSessionId(this));
            params.put("DeviceType", deviceType);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.TESTING_BASE_URL, "CheckStudentSession", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                Log.i("CheckStudentSession", "response = "+response);
            }

            @Override
            public void error(String error) {
                String deviceType = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                ServerApi.logoutDevice(MainActivity.this, Utils.getStudentId(getApplicationContext()), deviceType, new ServerApi.CompleteListener() {
                    @Override
                    public void response(JSONObject response) {
                        resetValues();
                    }

                    @Override
                    public void error(String error) {

                    }
                });
            }
        });
    }

    public void resetValues(){
        Preferences.remove(this, Preferences.KEY_STUDENT_ID);
        Preferences.remove(this, Preferences.KEY_SPEC_ID);
        Preferences.remove(this, Preferences.KEY_COURSE_ID);
        Preferences.remove(this, Preferences.KEY_STUDENT_CODE);
        Preferences.remove(this, Preferences.KEY_STUDENT_NAME);
        Preferences.remove(this, Preferences.KEY_STUDENT_EMAIL);
        Preferences.remove(this, Preferences.KEY_STUDENT_PHONE);
        Preferences.remove(this, Preferences.KEY_COURSE_NAME);
        Preferences.remove(this, Preferences.KEY_SPEC_NAME);
        Preferences.remove(this, Preferences.KEY_STUDENT_ROLLNO);
        Preferences.remove(this, Preferences.KEY_STUDENT_PROFILE_PIC);
        Preferences.remove(this, Preferences.KEY_IS_LOGIN_COMPLTED);
        Preferences.remove(this, Preferences.KEY_CART_COUNT);
        Preferences.remove(this, Preferences.KEY_IS_WELCOME_MSG_SHOWN);
        Preferences.remove(this, Preferences.KEY_IS_NEW_USER);

        Utils.deleteObject(this, "homenews");
        Utils.deleteObject(this, "packages_0");
        Utils.deleteObject(this, "OnlineClassPackages_0");
        Utils.deleteObject(this, "homeAchivements");
        Utils.deleteObject(this, "notifications");
        Utils.deleteObject(this, "videoImages");
        Utils.deleteObject(this, "profile");
        Utils.deleteObject(this, "books");
        Utils.deleteObject(this, "seriesDetails");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(MainActivity.this).clearDiskCache();
            }
        }).start();
        Glide.get(MainActivity.this).clearMemory();
        Toast.makeText(getApplicationContext(), "You have successfully logged out!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.APP_UPDATE_REQUEST) {
            if(requestCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED){
            }else if (resultCode != RESULT_OK) {
            }
        }
    }
    ProgressDialog progressDialog;
    private void showDownloadDialog(int max, int progress){
        try {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Downloading...");
                progressDialog.setMax(max);
                progressDialog.setProgress(progress);
                if (!Utils.isActivityDestroyed(this)) {
                    progressDialog.show();
                }
            } else {
                progressDialog.setProgress(progress);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
}