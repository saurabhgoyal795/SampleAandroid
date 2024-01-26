package com.zonetech.online.mypackage.onlineTestSeries;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.zonetech.online.R;
import com.zonetech.online.classes.ClassPackageDetailsActivity;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.cropper.CropImage;
import com.zonetech.online.cropper.CropImageView;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.remoteConfig.RemoteConfig;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class TestSeriesDetailsActivity extends ZTAppCompatActivity {
    public static final String ACTIVE = "Active";
    public static final String UPCOMING = "Upcoming";
    public static final String COMPLETED = "Completed";
    public static final String MISSED = "Missed";

    private SectionsPagerAdapter sectionsPagerAdapter;
    private String[] TAB_TITLES;
    private JSONObject itemObj;
    private static HashMap<String, JSONArray> tabsValues;
    BottomNavigationView navView;
    private Handler handler;
    private Runnable fetcher = new Runnable() {
        @Override
        public void run() {
            if(handler == null){
                return;
            }
            getPlanDetails();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testseries_details);

        Bundle bundle = getIntent().getExtras();
        String item = bundle.getString("item");
        try {
            itemObj = new JSONObject(item);
            setTitle(itemObj.optString("PlanName"));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        TAB_TITLES = new String[]{getString(R.string.active), getString(R.string.upcoming), getString(R.string.completed), getString(R.string.missed_test)};
        navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(2).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    Utils.openHome(TestSeriesDetailsActivity.this);
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(TestSeriesDetailsActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(TestSeriesDetailsActivity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(TestSeriesDetailsActivity.this);
                }

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPlanDetails();
        RemoteConfig.fetchRemoteConfig(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopFetchTimer();
    }

    private void startFetchTimer(){
        stopFetchTimer();
        if(Utils.isActivityDestroyed(this)){
            return;
        }
        handler = new Handler();
        handler.postDelayed(fetcher, 2*60*1000);
    }

    private void stopFetchTimer(){
        if(handler != null){
            handler.removeCallbacks(fetcher);
            handler = null;
        }
    }

    private void setTabsValues(JSONArray data){
        tabsValues = new HashMap<>();
        for (int i = 0 ; i < data.length() ; i++){
            switch (data.optJSONObject(i).optInt("ExamStatus")){
                case 0:
                    JSONArray upComingArray = tabsValues.get(UPCOMING);
                    if(upComingArray == null){
                        upComingArray = new JSONArray();
                    }
                    upComingArray.put(data.optJSONObject(i));
                    tabsValues.put(UPCOMING, upComingArray);
                    break;
                case 1:
                case 2:
                    JSONArray activeArray = tabsValues.get(ACTIVE);
                    if(activeArray == null){
                        activeArray = new JSONArray();
                    }
                    activeArray.put(data.optJSONObject(i));
                    tabsValues.put(ACTIVE, activeArray);
                    break;
                case 3:
                    JSONArray completedArray = tabsValues.get(COMPLETED);
                    if(completedArray == null){
                        completedArray = new JSONArray();
                    }
                    completedArray.put(data.optJSONObject(i));
                    tabsValues.put(COMPLETED, completedArray);
                    break;
                case 4:
                    JSONArray missedArray = tabsValues.get(MISSED);
                    if(missedArray == null){
                        missedArray = new JSONArray();
                    }
                    missedArray.put(data.optJSONObject(i));
                    tabsValues.put(MISSED, missedArray);
                    break;
            }
        }
        TabLayout tabs = findViewById(R.id.tabs);
        if(sectionsPagerAdapter != null){
            sectionsPagerAdapter.notifyDataSetChanged();
        }else {
            ViewPager viewPager = findViewById(R.id.view_pager);
            sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(sectionsPagerAdapter);
            tabs.setupWithViewPager(viewPager);
        }
        for(int i=0; i < tabs.getTabCount(); i++) {
            View tab = ((ViewGroup) tabs.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(0, 0, 8, 0);
            tab.requestLayout();
        }
    }
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public Fragment getItem(int position) {
            Fragment testFragment = new TestSeriesDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            testFragment.setArguments(bundle);
            return testFragment;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TITLES[position];
        }

        @Override
        public int getCount() {
            return TAB_TITLES.length;
        }
    }


    public static Bundle getItemData(int position){
        Bundle bundle = new Bundle();
        switch (position){
            case 0:
                bundle.putString("item", tabsValues.get(ACTIVE) != null ? tabsValues.get(ACTIVE).toString() : "[]");
                bundle.putString("type", ACTIVE);
                break;
            case 1:
                bundle.putString("item", tabsValues.get(UPCOMING) != null ? tabsValues.get(UPCOMING).toString() : "[]");
                bundle.putString("type", UPCOMING);
                break;
            case 2:
                bundle.putString("item", tabsValues.get(COMPLETED) != null ? tabsValues.get(COMPLETED).toString() : "[]");
                bundle.putString("type", COMPLETED);
                break;
            case 3:
                bundle.putString("item", tabsValues.get(MISSED) != null ? tabsValues.get(MISSED).toString() : "[]");
                bundle.putString("type", MISSED);
                break;
        }
        return bundle;
    }

    private void getPlanDetails(){
        JSONObject params = new JSONObject();
        try{
            int specializationId = Preferences.get(getApplicationContext(), Preferences.KEY_SPEC_ID, 0);
            params.put("OrgPlanID", itemObj.optInt("PlanID"));
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
                if(Utils.isActivityDestroyed(TestSeriesDetailsActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray data = response.optJSONArray("Body");
                setTabsValues(data);
                startFetchTimer();
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if(Utils.isActivityDestroyed(TestSeriesDetailsActivity.this)){
                    return;
                }
                startFetchTimer();
            }
        });
    }
    View promptsView;
    public void showDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            promptsView = li.inflate(R.layout.saveexamcenter_alert, null);
            builder.setView(promptsView);
            AlertDialog dialog = builder.create();
            TextView submitButton = promptsView.findViewById(R.id.submitButton);
            EditText rollNo = promptsView.findViewById(R.id.rollNo);
            EditText examCenter = promptsView.findViewById(R.id.examCenter);
            EditText picker = promptsView.findViewById(R.id.dob);
            picker.setInputType(InputType.TYPE_NULL);
            Button selectImage = promptsView.findViewById(R.id.buttonLoadPicture);
            selectImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFileTypeDialog();
                }
            });

            final Calendar myCalendar = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    // TODO Auto-generated method stub
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    String myFormat = "dd/MM/yyyy"; //In which you need put here
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                    picker.setText(sdf.format(myCalendar.getTime()));
                }
            };

            picker.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    DatePickerDialog datePicker = new DatePickerDialog(TestSeriesDetailsActivity.this, date, myCalendar
                            .get(Calendar.YEAR) - 20, myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            datePicker .show();

                        }
                    }, 300);
                }
            });
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(submitExamCenter(rollNo.getText().toString(), examCenter.getText().toString(), picker.getText().toString())){
                        dialog.dismiss();
                    }
                }
            });
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    private boolean submitExamCenter(String rollNo, String examCenter, String dob){
        String msg = "";
        if(!Utils.isValidString(rollNo)){
            msg = "Invalid roll number";
        }
        if(!Utils.isValidString(examCenter)){
            if(Utils.isValidString(msg)){
                msg = msg + ", ";
            }
            msg = msg + "Invalid exam center";
        }
        if(!Utils.isValidString(dob)){
            if(Utils.isValidString(msg)){
                msg = msg + ", ";
            }
            msg = msg + "Invalid date of birth";
        }
        if(!Utils.isValidString(admitCardPath)){
            if(Utils.isValidString(msg)){
                msg = msg + ", ";
            }
            msg = msg + "Invalid image, please upload image!";
        }
        if(Utils.isValidString(msg)){
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
        saveExamCenter(rollNo, examCenter, dob);
        return true;
    }

    private void saveExamCenter(String rollNo, String examCenter, String dob){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(this));
            params.put("RollNo", rollNo);
            params.put("ExamCenter", examCenter);
            params.put("DOB", dob);
            params.put("AdmitCardPath", admitCardPath);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.TESTING_BASE_URL, "SaveExamCenter", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(TestSeriesDetailsActivity.this, "Submitted sucessfully!", Toast.LENGTH_SHORT).show();
                Preferences.put(TestSeriesDetailsActivity.this, Preferences.KEY_IS_TEST_FORM_SUBMITTED, true);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(TestSeriesDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private int RESULT_LOAD_FILE = 105;
    private int RESULT_LOAD_FILE2 = 106;
    private int RESULT_LOAD_FILE3 = 107;

    int orgPlanID, orgExamID;
    public void uploadFile(int orgPlanID, int orgExamID){
        this.orgPlanID = orgPlanID;
        this.orgExamID = orgExamID;
        if(Utils.checkForPermissions(this, Utils.REQUEST_CODE_ASK_PERMISSIONS)){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            try {
                startActivityForResult(intent, RESULT_LOAD_FILE);
            } catch (ActivityNotFoundException e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
        }
    }
    private String admitCardPath;
    public void uploadFileForIDCard(){
        if(Utils.checkForPermissions(this, Utils.REQUEST_CODE_ASK_PERMISSIONS_IMAGE)){
            Intent galleryIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            try {
                startActivityForResult(galleryIntent,
                        RESULT_LOAD_FILE2);
            } catch (ActivityNotFoundException e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void uploadFileForIDCardPdf(){
        if(Utils.checkForPermissions(this, Utils.REQUEST_CODE_ASK_PERMISSIONS_PDF)){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            try {
                startActivityForResult(intent, RESULT_LOAD_FILE3);
            } catch (ActivityNotFoundException e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_FILE || requestCode == RESULT_LOAD_FILE3) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                final Uri uri = data.getData();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String selectedImagePath = uri.getPath();
                            InputStream in = null;
                            OutputStream out = null;
                            String destinationPath = getFilesDir() + "/uploadTestFiles/uploadFile.pdf";
                            File file = new File(destinationPath);
                            try {
                                in = getContentResolver().openInputStream(uri);
                                file.getParentFile().mkdirs();
                                out = new FileOutputStream(file);
                                byte[] buffer = new byte[1024];
                                int len;
                                while ((len = in.read(buffer)) != -1) {
                                    out.write(buffer, 0, len);
                                }
                                selectedImagePath = destinationPath;
                            } catch (Exception e) {
                                if (Utils.isDebugModeOn) {
                                    e.printStackTrace();
                                }
                                selectedImagePath = uri.getPath();
                            } finally {
                                if (in != null) {
                                    in.close();
                                }
                                if (out != null) {
                                    out.close();
                                }
                            }
                            if(requestCode == RESULT_LOAD_FILE3){
                                uploadIDFile(selectedImagePath, "pdf", file);
                            }else {
                                Utils.uploadPdf(TestSeriesDetailsActivity.this, selectedImagePath, orgPlanID, orgExamID, new CompleteListener() {
                                    @Override
                                    public void success(JSONObject response) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Utils.showToast(getApplicationContext(), "Successfully uploaded!");
                                                Log.i("IDCardImageUpload", "response = " + response);
                                                file.delete();
                                                findViewById(R.id.progressBar).setVisibility(View.GONE);

                                            }
                                        });
                                    }

                                    @Override
                                    public void error(String error) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Utils.showToast(getApplicationContext(), "Uploading failed!");
                                                file.delete();
                                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                });
                            }
                        }catch (Exception e){
                            if(Utils.isDebugModeOn){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            } else {
                Toast.makeText(getApplicationContext(), "No file selected", Toast.LENGTH_SHORT).show();
            }
        } else  if (requestCode == RESULT_LOAD_FILE2) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                try {
                    final Uri selectedImage = data.getData();
                    CropImage.activity(selectedImage)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.picked_no_image), Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                String imagePath = result.getUri().getPath();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        uploadIDFile(imagePath, "image", null);
                    }
                }).start();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.picked_no_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        try {
            switch (requestCode) {
                case Utils.REQUEST_CODE_ASK_PERMISSIONS:
                case Utils.REQUEST_CODE_ASK_PERMISSIONS_IMAGE:
                case Utils.REQUEST_CODE_ASK_PERMISSIONS_PDF:{
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(requestCode == Utils.REQUEST_CODE_ASK_PERMISSIONS_IMAGE){
                            uploadFileForIDCard();
                        }else if(requestCode == Utils.REQUEST_CODE_ASK_PERMISSIONS_PDF){
                            uploadFileForIDCardPdf();
                        }else {
                            uploadFile(orgPlanID, orgExamID);
                        }
                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showWhyWeNeedPermissionDialog(R.string.perm_readexternal_why_we_need_message);
                    } else {
                        showEnablePermissionInSettingsDialog(R.string.perm_readexternal_go_to_settings_message);
                    }
                    break;
                }
                default: {
                    super.onRequestPermissionsResult(requestCode, permissions,
                            grantResults);
                }
            }
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showWhyWeNeedPermissionDialog(int messageId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this,
                android.R.style.Theme_Material_Light_Dialog);
        builder.setCancelable(false);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.unlock_confirm_accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create();
        if (!Utils.isActivityDestroyed(this))
            builder.show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showEnablePermissionInSettingsDialog(int messageId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this,
                android.R.style.Theme_Material_Light_Dialog);
        builder.setCancelable(false);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.unlock_confirm_accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create();
        if (!Utils.isActivityDestroyed(this))
            builder.show();
    }

    private void showUploadLayout(String path, String type){
        try{
            promptsView.findViewById(R.id.uploadImageLayout).setVisibility(View.VISIBLE);
            if("pdf".equalsIgnoreCase(type)){
                Glide.with(this)
                        .load(R.drawable.baseline_picture_as_pdf_black_24)
                        .override((int) (100 * Utils.getMetrics(this).density))
                        .into((ImageView) promptsView.findViewById(R.id.uploadImageView));
                promptsView.findViewById(R.id.uploadImageLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TestSeriesDetailsActivity.this, PdfOpenActivity.class);
                        intent.putExtra("fileName", "uploadFile.pdf");
                        intent.putExtra("downloadPath", ServerApi.TEST_SOLUTION_PATH);
                        intent.putExtra("basePath", getFilesDir() + "/uploadTestFiles/");
                        startActivity(intent);
                    }
                });
            }else {
                Glide.with(this)
                        .load(path)
                        .override((int) (100 * Utils.getMetrics(this).density))
                        .into((ImageView) promptsView.findViewById(R.id.uploadImageView));
                promptsView.findViewById(R.id.uploadImageLayout).setOnClickListener(null);
            }
            promptsView.findViewById(R.id.clearUploadImage).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptsView.findViewById(R.id.uploadImageLayout).setVisibility(View.GONE);
                    admitCardPath = "";
                }
            });
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private void uploadIDFile(String filePath, String type, File file){
        try {
            Utils.uploadAdmitCardImage(TestSeriesDetailsActivity.this, filePath, new CompleteListener() {
                @Override
                public void success(JSONObject response) {
                    admitCardPath = response.optString("Body");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(getApplicationContext(), "Successfully uploaded!");
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if("image".equalsIgnoreCase(type)) {
                                showUploadLayout(filePath, "image");
                            }else{
                                showUploadLayout(filePath, "pdf");
                            }
                        }
                    });
                }

                @Override
                public void error(String error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(getApplicationContext(), "Uploading failed!");
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if(file != null){
                                file.delete();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            });
        }
    }

    private void showFileTypeDialog(){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.filepickertype, null);
            builder.setView(promptsView);
            AlertDialog dialog = builder.create();
            TextView imageType = promptsView.findViewById(R.id.imageType);
            TextView pdfType = promptsView.findViewById(R.id.pdfType);
            imageType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadFileForIDCard();
                    dialog.dismiss();
                }
            });
            pdfType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadFileForIDCardPdf();
                    dialog.dismiss();
                }
            });
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }
}