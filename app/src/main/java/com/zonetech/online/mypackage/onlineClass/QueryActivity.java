package com.zonetech.online.mypackage.onlineClass;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zonetech.online.BuildConfig;
import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class QueryActivity extends AppCompatActivity {
    public static final int VIDEO_QUERY = 1;
    public static final int TEST_QUERY = 2;
    int topicId;
    String videoId;
    EditText queryBox;
    int type = VIDEO_QUERY;
    int id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_popup);
        queryBox = findViewById(R.id.queryBox);
        Bundle bundle = getIntent().getExtras();
        topicId = bundle.getInt("topicId");
        id = bundle.getInt("id");
        videoId = bundle.getString("videoUrl");
        String titleText = bundle.getString("title");
        type = bundle.getInt("queryType", type);
        TextView title = findViewById(R.id.title);
        title.setText(titleText);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.rootView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                finish();
            }
        });
        findViewById(R.id.popupView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                return;
            }
        });
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                saveQuery();
            }
        });
    }

    private void saveQuery(){
        switch (type){
            case VIDEO_QUERY:
                saveVideoQuery();
                break;
            case TEST_QUERY:
                saveTestQuery();
                break;
        }
        Toast.makeText(getApplicationContext(), "Thanks for your feedback", Toast.LENGTH_SHORT).show();
        finish();
    }
    private void saveVideoQuery(){
        if(!Utils.isValidString(queryBox.getText().toString().trim())){
            Toast.makeText(getApplicationContext(), "Invalid query", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(getApplicationContext()));
            params.put("StudentPlanID", id);
            params.put("TopicID", topicId);
            params.put("VideoID", videoId);
            params.put("Comments", queryBox.getText().toString().trim());
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "SaveVideoQuery", params, null);
    }
    private void saveTestQuery(){
        if(!Utils.isValidString(queryBox.getText().toString().trim())){
            Toast.makeText(getApplicationContext(), "Invalid query", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(getApplicationContext()));
            params.put("ExamQuestionID", id);
            params.put("Comments", queryBox.getText().toString().trim());
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(getApplicationContext(), ServerApi.BASE_URL, "SaveQuestionReview", params, null);
    }
    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(queryBox.getWindowToken(), 0);
        } catch(Throwable e) {}
    }
}
