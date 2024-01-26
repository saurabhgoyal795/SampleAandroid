package com.zonetech.online;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.news.NewsActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContactUsActivity extends ZTAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        EditText name =findViewById(R.id.name);
        EditText mobileNumber =findViewById(R.id.mobileNumber);
        EditText subject =findViewById(R.id.subject);
        EditText comment =findViewById(R.id.comment);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:
                        onBackPressed();
                        break;
                    case R.id.navigation_classes:
                        Utils.openMyPackages(ContactUsActivity.this, 0);
                        break;
                    case R.id.navigation_test:
                        Utils.openMyPackages(ContactUsActivity.this, 1);
                        break;
                    case R.id.navigation_profile:
                        Utils.openDownloadsNewTask(ContactUsActivity.this);
                        break;
                }
                return false;
            }
        });

        findViewById(R.id.emailcard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intent.setData(Uri.parse("mailto:info@zonetech.in")); // or just "mailto:" for blank
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                startActivity(intent);
            }
        });

        findViewById(R.id.updateButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                if(name.getText().toString().trim().equalsIgnoreCase("") || mobileNumber.getText().toString().trim().equalsIgnoreCase("") || subject.getText().toString().trim().equalsIgnoreCase("") || name.getText().toString().trim().equalsIgnoreCase("")){
                    Toast.makeText(getApplicationContext(), "Please enter all the fields", Toast.LENGTH_LONG).show();
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                JSONObject params = new JSONObject();
                try{
                    params.put("StudentID",  Utils.getStudentId(getApplicationContext()));
                    params.put("Name",  name.getText().toString().trim());
                    params.put("MobileNo",  mobileNumber.getText().toString().trim());
                    params.put("Comments",  comment.getText().toString().trim());
                    params.put("Subjects",   subject.getText().toString().trim());
                    params.put("ContactType",   0);
                    params.put("EmailID",    Utils.getEmail(getApplicationContext()));
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }

                ServerApi.callServerApi(getApplicationContext(), ServerApi.WEB_URL,"saveEnquiry", params, new ServerApi.CompleteListener() {
                    @Override
                    public void response(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Thank you for conact with us, we will contact you as soon as possible.", Toast.LENGTH_SHORT).show();

                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }

                    @Override
                    public void error(String error) {

                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Thank you for conact with us, we will contact you as soon as possible.", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

    }
    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Throwable e) {}
    }
}