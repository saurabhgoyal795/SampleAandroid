package com.zonetech.online.dailyQuiz;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.profile.NewProfileActivity;
import com.zonetech.online.profile.TransactionActivity;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class DailyQuizActvity extends ZTAppCompatActivity {
    RecyclerView quizList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_quiz);
        quizList = findViewById(R.id.quizList);
        BottomNavigationView navView = findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navigation_home) {
                    onBackPressed();
                } else if (item.getItemId() == R.id.navigation_classes) {
                    Utils.openMyPackages(DailyQuizActvity.this, 0);
                } else if (item.getItemId() == R.id.navigation_test) {
                    Utils.openMyPackages(DailyQuizActvity.this, 1);
                } else if (item.getItemId() == R.id.navigation_profile) {
                    Utils.openDownloadsNewTask(DailyQuizActvity.this);
                }

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDailyQuiz();
    }

    private void getDailyQuiz(){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(this));
            params.put("SpecializationID", Utils.getStudentSpecID(this));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "QuizExam", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray values = response.optJSONArray("Body");
                if(values == null || values.length() == 0){
                    findViewById(R.id.noQuiz).setVisibility(View.VISIBLE);
                }else {
                    setList(values);
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setList(JSONArray values){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        quizList.setLayoutManager(linearLayoutManager);
        QuizAdapter quizAdapter = new QuizAdapter(this, values);
        quizList.setAdapter(quizAdapter);
    }
}
