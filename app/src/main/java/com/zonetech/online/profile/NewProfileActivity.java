package com.zonetech.online.profile;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zonetech.online.R;
import com.zonetech.online.cropper.CropImage;
import com.zonetech.online.cropper.CropImageView;
import com.zonetech.online.home.LogoutActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class NewProfileActivity extends AppCompatActivity {
    public static final String PROFILE_IMAGE_PATH = "/ProfileImage/profileImage.png";
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 190;
    private int RESULT_LOAD_IMG = 200;
    ImageView profile;
    TextView studentName;
    TextView branchName;
    TextView studentCourse;
    TextView mobileNumber;
    TextView emailId;
    TextView nameText;
    TextView gender;
    TextView dateOfbirth;
    TextView address;
    ScrollView profileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        profile = findViewById(R.id.profile);
        studentName = findViewById(R.id.studentName);
        branchName = findViewById(R.id.branchName);
        studentCourse = findViewById(R.id.studentCourse);
        mobileNumber = findViewById(R.id.mobileNumber);
        emailId = findViewById(R.id.emailId);
        nameText = findViewById(R.id.nameText);
        gender = findViewById(R.id.gender);
        dateOfbirth = findViewById(R.id.dateOfbirth);
        address = findViewById(R.id.address);
        profileLayout = findViewById(R.id.profileLayout);
        findViewById(R.id.logoutbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        findViewById(R.id.changepassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        JSONObject params = new JSONObject();
        try {
            params.put("StudentId", Utils.getStudentId(getApplicationContext()));
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "studentprofile", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);

                if (Utils.isActivityDestroyed(NewProfileActivity.this)) {
                    return;
                }
                Utils.saveObject(getApplicationContext(), response.toString(), "profile");
                JSONObject profileJSONNew = response.optJSONObject("Body");
                if (profileJSONNew == null) {
                    profileJSONNew = new JSONObject();
                }
                profileLayout.setVisibility(View.VISIBLE);
                JSONObject profileData = profileJSONNew.optJSONObject("profile");
                setProfileJSON(profileData);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                if (Utils.isActivityDestroyed(NewProfileActivity.this)) {
                    return;
                }
            }
        });
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.getMenu().getItem(3).setChecked(true);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    onBackPressed();
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(NewProfileActivity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(NewProfileActivity.this, 1);
                }

                return false;
            }
        });

        findViewById(R.id.pencilIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewProfileActivity.this, ProfileActivity.class));
            }
        });

        findViewById(R.id.card_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewProfileActivity.this, TransactionActivity.class));
            }
        });
        profile = findViewById(R.id.profileImage);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isConnectedToInternet(getApplicationContext())) {
                    openPhotos();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.network_error_1), Toast.LENGTH_SHORT).show();
                }
            }
        });
        setProfileImage();
    }

    private void setProfileImage() {
        DisplayMetrics metrics = Utils.getMetrics(this);
        String fileName = Preferences.get(getApplicationContext(), Preferences.KEY_STUDENT_PROFILE_PIC, "");
        if(Utils.isValidString(fileName)){
            Glide.with(this)
                    .load(ServerApi.PROFILE_BASE_PATH + fileName)
                    .override((int)(60*metrics.density), (int)(60*metrics.density))
                    .circleCrop()
                    .into(profile);
        }
    }

    private void setProfileJSON(JSONObject data) {
        try {
            emailId.setText(data.optString("EmailID"));
            studentName.setText(data.optString("StudentName"));
            nameText.setText(data.optString("StudentName"));
            mobileNumber.setText(data.optString("MobileNo"));
            studentCourse.setText(data.optString("CourseName"));
            branchName.setText(data.optString("SpecName"));
            if (!data.optString("CAddress").trim().equalsIgnoreCase("")) {
                address.setText(data.optString("CAddress") + ", " + data.optString("CCity") + ", " + data.optString("CPinCode"));
            }
            if (data.optInt("Gender") == 0) {
                gender.setText("-");
            } else if (data.optInt("Gender") == 1) {
                gender.setText("Male");
            } else if (data.optInt("Gender") == 2) {
                gender.setText("Female");
            } else if (data.optInt("Gender") == 3) {
                gender.setText("None");
            }
            dateOfbirth.setText(data.optString("DOB"));
        } catch (Exception e) {

        }
    }

    private void logout() {
        startActivity(new Intent(this, LogoutActivity.class));
    }

    private void openPhotos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForPermissions();
        } else {
            Intent galleryIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            try {
                startActivityForResult(galleryIntent,
                        RESULT_LOAD_IMG);
            } catch (ActivityNotFoundException e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasGetAccountsPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasGetAccountsPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                try {
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                } catch (ActivityNotFoundException e) {
                    if (Utils.isDebugModeOn) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        try {
            switch (requestCode) {
                case REQUEST_CODE_ASK_PERMISSIONS: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Intent galleryIntent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        try {
                            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                        } catch (ActivityNotFoundException e) {
                            if (Utils.isDebugModeOn) {
                                e.printStackTrace();
                            }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG) {
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
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                String imagePath = result.getUri().getPath();
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            uploadProfileImage(imagePath);
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
                }).start();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.picked_no_image), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean uploadProfileImage(String filePath) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            String mime = getMimeType(filePath);

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("StudentID", Utils.getStudentId(NewProfileActivity.this)+"")
                    .addFormDataPart("file", new File(filePath).getName(), RequestBody.create(MediaType.parse(mime), new File(filePath)))
                    .build();
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ServerApi.BASE_URL+"UpdateProfilePic")
                    .method("POST", body)
                    .header("accept", "application/json")
                    .build();
            okhttp3.Response response = client.newCall(request).execute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (response.isSuccessful()) {
                            String res = response.body().string();
                            JSONObject object = new JSONObject(res);
                            if(object.optInt("StatusCode") == 200){
                                object = object.optJSONObject("Body");
                                String profilePic = object.optString("ProfilePic");
                                Preferences.put(getApplicationContext(), Preferences.KEY_STUDENT_PROFILE_PIC, profilePic);
                                Toast.makeText(getApplicationContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                                setProfileImage();
                            }else{
                                Toast.makeText(getApplicationContext(), "Profile image not updated, try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }catch (Exception e){
                        if(Utils.isDebugModeOn){
                            e.printStackTrace();
                        }
                    }
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            });
            return true;
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Profile image not updated, try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }
    private String getMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null)
        {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }


    androidx.appcompat.app.AlertDialog dialog;
    private void showDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(NewProfileActivity.this);
            LayoutInflater li = LayoutInflater.from(NewProfileActivity.this);
            View promptsView = li.inflate(R.layout.changepassword_alert, null);
            EditText oldPass = promptsView.findViewById(R.id.oldPass);
            EditText newPass = promptsView.findViewById(R.id.newPass);
            TextView ok = promptsView.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject params = new JSONObject();
                    try {
                        params.put("StudentId", Utils.getStudentId(getApplicationContext()));
                        params.put("Passwords", oldPass.getText().toString().trim());
                        params.put("NewPassword", newPass.getText().toString().trim());
                    } catch (Exception e) {
                        if (Utils.isDebugModeOn) {
                            e.printStackTrace();
                        }
                    }
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "ChangePassword", params, new ServerApi.CompleteListener() {
                        @Override
                        public void response(JSONObject response) {
                            dialog.dismiss();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Successfully Changed Password", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void error(String error) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                            if (Utils.isActivityDestroyed(NewProfileActivity.this)) {
                                return;
                            }
                        }
                    });
                }
            });
            builder.setView(promptsView);
            dialog = builder.create();
            if (!Utils.isActivityDestroyed(NewProfileActivity.this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }
}