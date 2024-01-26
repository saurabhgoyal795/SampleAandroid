package com.zonetech.online.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.home.MainActivity;
import com.zonetech.online.R;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class LoginActivity extends ZTAppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private EditText email;
    private EditText password;
    private EditText name;
    private EditText phone;
    private Spinner courseSpinner;
    private Spinner specSpinner;
    private EditText otp;
    private boolean isSignUpScreen;
    private boolean isOtpScreen;
    private int courseSelectedPosition = 1;
    private int specializationSelectedPositon;
    private JSONArray courseList;
    private JSONArray specList;
    private CountDownTimer countDownTimer;
    private DisplayMetrics metrics;
    private ImageView passwordVisibleIcon;
    private String deviceType;
    private int loginStudentId;
    private String loginDeviceType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        metrics = Utils.getMetrics(this);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        courseSpinner = findViewById(R.id.course);
        specSpinner = findViewById(R.id.spec);
        passwordVisibleIcon = findViewById(R.id.passwordVisibleIcon);
        otp = findViewById(R.id.otp);
        findViewById(R.id.newUser).setOnClickListener(this);
        findViewById(R.id.newUserOTP).setOnClickListener(this);
        findViewById(R.id.loginButton).setOnClickListener(this);
        findViewById(R.id.forgetPassword).setOnClickListener(this);
        findViewById(R.id.signIn).setOnClickListener(this);
        findViewById(R.id.progressBar).setOnClickListener(this);
        findViewById(R.id.resendOTP).setOnClickListener(this);
        findViewById(R.id.contentView).setOnClickListener(this);
        passwordVisibleIcon.setOnClickListener(this);
        courseSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                }
                return false;
            }
        });
        specSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                }
                return false;
            }
        });
        deviceType = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onClick(View v) {
        hideKeyboard();
        if  (v.getId() == R.id.passwordVisibleIcon) {
            togglePasswordVisiblity();
        } else if  (v.getId() == R.id.contentView) {
            hideKeyboard();
        }else if  (v.getId() == R.id.resendOTP) {
            stopTimer();
            otpGenerateRequest();
        } else if  (v.getId() == R.id.progressBar) {

        } else if  (v.getId() == R.id.signIn) {
            setLoginLayout();
        } else if  (v.getId() == R.id.newUser) {
            setNewUserLayout();
        } else if  (v.getId() == R.id.newUserOTP) {
            setNewUserLayout();
        } else if  (v.getId() == R.id.forgetPassword) {
            forgetPassword();
        } else if  (v.getId() == R.id.loginButton) {
            loginButtonClicked();
        }
    }

    private void setNewUserLayout() {
        isSignUpScreen = true;
        isOtpScreen = false;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)findViewById(R.id.loginView).getLayoutParams();
        params.topMargin = (int)(16 * metrics.density);
        findViewById(R.id.loginView).setLayoutParams(params);
        findViewById(R.id.nameLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.phoneLayout).setVisibility(View.VISIBLE);
//        findViewById(R.id.courseLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.branchLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.emailLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.passwordLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.otpLayout).setVisibility(View.GONE);
        findViewById(R.id.forgetPassword).setVisibility(View.GONE);
        findViewById(R.id.newUserOTP).setVisibility(View.GONE);
        findViewById(R.id.newUser).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.loginButton)).setText(R.string.singup);
        ((TextView) findViewById(R.id.loginMode)).setText(R.string.singup);
        findViewById(R.id.signIn).setVisibility(View.VISIBLE);
        stopTimer();
        if(courseList == null || courseList.length() == 0) {
            getCourseSpec();
        }
    }

    private void setLoginLayout() {
        isSignUpScreen = false;
        isOtpScreen = false;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)findViewById(R.id.loginView).getLayoutParams();
        params.topMargin = (int)getResources().getDimension(R.dimen.login_form_top);
        findViewById(R.id.loginView).setLayoutParams(params);
        findViewById(R.id.nameLayout).setVisibility(View.GONE);
        findViewById(R.id.phoneLayout).setVisibility(View.GONE);
        findViewById(R.id.courseLayout).setVisibility(View.GONE);
        findViewById(R.id.branchLayout).setVisibility(View.GONE);
        findViewById(R.id.otpLayout).setVisibility(View.GONE);
        findViewById(R.id.emailLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.passwordLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.forgetPassword).setVisibility(View.VISIBLE);
        findViewById(R.id.newUserOTP).setVisibility(View.GONE);
        findViewById(R.id.newUser).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.loginButton)).setText(R.string.singin);
        ((TextView) findViewById(R.id.loginMode)).setText(R.string.singin);
        findViewById(R.id.signIn).setVisibility(View.GONE);
        stopTimer();
    }

    private void setOtpLayout() {
        isOtpScreen = true;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)findViewById(R.id.loginView).getLayoutParams();
        params.topMargin = (int)getResources().getDimension(R.dimen.login_form_top);
        findViewById(R.id.loginView).setLayoutParams(params);
        String msg = String.format(getString(R.string.otp_message), phone.getText().toString().trim());
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        findViewById(R.id.nameLayout).setVisibility(View.GONE);
        findViewById(R.id.phoneLayout).setVisibility(View.GONE);
        findViewById(R.id.courseLayout).setVisibility(View.GONE);
        findViewById(R.id.branchLayout).setVisibility(View.GONE);
        findViewById(R.id.forgetPassword).setVisibility(View.GONE);
        findViewById(R.id.newUserOTP).setVisibility(View.VISIBLE);
        findViewById(R.id.emailLayout).setVisibility(View.GONE);
        findViewById(R.id.passwordLayout).setVisibility(View.GONE);
        findViewById(R.id.otpLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.newUser).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.loginButton)).setText(R.string.otp_submit);
        ((TextView) findViewById(R.id.loginMode)).setText(R.string.singup);
        findViewById(R.id.signIn).setVisibility(View.VISIBLE);
        startTimer();
    }

    private void loginButtonClicked() {
        if (checkValidity()) {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            if(isOtpScreen){
                if(Utils.isValidString(otp.getText().toString().trim())) {
                    otpValidateRequest(otp.getText().toString().trim());
                }else{
                    Toast.makeText(getApplicationContext(), "Enter the one time password that you have received in the SMS.", Toast.LENGTH_SHORT).show();
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                }
            }else if (isSignUpScreen) {
                checkEmailMobileRegistration();
            } else {
                signIn();
            }
        }
    }

    private void signUp() {
        JSONObject params = new JSONObject();
        try{
            params.put("EmailID", email.getText().toString().trim());
            params.put("Passwords", password.getText().toString().trim());
            try {
                params.put("CourseID", courseList.optJSONObject(courseSelectedPosition).optString("CourseID"));
            } catch (Exception e){
                params.put("CourseID", "");
            }
            params.put("SpecializationID", specList.optJSONObject(specializationSelectedPositon).optString("SpecializationID"));
            params.put("MobileNo", phone.getText().toString().trim());
            params.put("StudentName", name.getText().toString().trim());
            params.put("DeviceInfo", Utils.getPhoneDetail());
            params.put("DeviceType", deviceType);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "register", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(LoginActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                int statusCode = response.optInt("StatusCode");
                if(200 == statusCode) {
                    Preferences.put(getApplicationContext(), Preferences.KEY_IS_NEW_USER, true);
                    setLoginLayout();
                }
                String message = response.optString("Message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(LoginActivity.this)){
                    return;
                }
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    private void signIn() {
        JSONObject params = new JSONObject();
        try{
            params.put("EmailID", email.getText().toString().trim());
            params.put("Passwords", password.getText().toString().trim());
            params.put("DeviceType", deviceType);
            params.put("DeviceInfo", Utils.getPhoneDetail());
            params.put("BrowserName", "Android App");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.TESTING_BASE_URL, "login", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(LoginActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                int statusCode = response.optInt("StatusCode");
                if(200  == statusCode) {
                    JSONObject data = response.optJSONObject("Body");
                    if (data != null) {
                        Utils.setUserProperties(LoginActivity.this, data);
                    }
                    Preferences.put(getApplicationContext(), Preferences.KEY_IS_LOGIN_COMPLTED, true);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else if(statusCode == 501){
                    String message = response.optString("Message");
                    JSONObject data = response.optJSONObject("Body");
                    loginStudentId = data.optInt("StudentID");
                    loginDeviceType = data.optString("DeviceType");
                    showDialog(message);
                }else{
                    String message = response.optString("Message");
                    if(Utils.isValidString(message)) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(LoginActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkValidity() {
        boolean isAllValid = true;
        StringBuffer errorMsg = new StringBuffer();
        if (!Utils.isValidEmailAddress(email.getText().toString())) {
            errorMsg.append("Invalid email id");
            isAllValid = false;
        }
        if (!Utils.isValidPassword(password.getText().toString())) {
            if (Utils.isValidString(errorMsg.toString())) {
                errorMsg.append("\n");
            }
            errorMsg.append("password should be minimum 5 characters");
            isAllValid = false;
        }
        if(isSignUpScreen) {
            if (!Utils.isValidMobileNumber(phone.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid mobile number");
                isAllValid = false;
            }
            if (!Utils.isValidString(name.getText().toString())) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid name");
                isAllValid = false;
            }
            courseSelectedPosition = 1;
            if (courseSelectedPosition == 0) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid course");
                isAllValid = false;
            }
            if (specializationSelectedPositon == 0) {
                if (Utils.isValidString(errorMsg.toString())) {
                    errorMsg.append("\n");
                }
                errorMsg.append("Invalid branch");
                isAllValid = false;
            }
        }
        if (!isAllValid) {
            Toast.makeText(getApplicationContext(), errorMsg.toString(), Toast.LENGTH_SHORT).show();
        }
        return isAllValid;
    }

    private void getCourseSpec() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "GetCourseSpec", null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(LoginActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONObject data = response.optJSONObject("Body");
                JSONArray courseData = data.optJSONArray("Course");
                JSONArray specData = data.optJSONArray("Specialization");
                setCourseData(courseData);
                setSpecializationData(specData);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCourseData(JSONArray courseData) {
        ArrayList<String> items = new ArrayList<>();
        try {
            JSONObject object = new JSONObject();
            object.put("CourseName", "Select Course");
            items.add("Select Course");
            courseList = new JSONArray();
            courseList.put(object);
            for (int i = 0 ; i < courseData.length() ; i++){
                courseList.put(courseData.optJSONObject(i));
                items.add(courseData.optJSONObject(i).optString("CourseName"));
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, items);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(dataAdapter);
        courseSpinner.setOnItemSelectedListener(this);
    }

    private void setSpecializationData(JSONArray specializationData) {
        ArrayList<String> items = new ArrayList<>();
        try {
            JSONObject object = new JSONObject();
            object.put("SpecName", "Select Branch");
            items.add("Select Branch");
            specList = new JSONArray();
            specList.put(object);
            for (int i = 0 ; i < specializationData.length() ; i++){
                specList.put(specializationData.optJSONObject(i));
                items.add(specializationData.optJSONObject(i).optString("SpecName"));
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, items);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specSpinner.setAdapter(dataAdapter);
        specSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent == specSpinner){
            specializationSelectedPositon = position;
        }else{
           // courseSelectedPosition = position;
        }
        hideKeyboard();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void hideKeyboard() {
        try {
            View view = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch(Throwable e) {}
    }

    private void forgetPassword(){
        if(!Utils.isValidEmailAddress(email.getText().toString())){
            Toast.makeText(getApplicationContext(), "Enter valid email id", Toast.LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
            params.put("EmailID", email.getText().toString());
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "forgetpassword", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(LoginActivity.this)){
                    return;
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                String message = response.optString("Message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void otpGenerateRequest(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.loginButton)).setText(R.string.otp_send);
        JSONObject params = new JSONObject();
        try{
            params.put("MobileNo", phone.getText().toString().trim());
            params.put("OtpType", 0);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "GenerateOtp", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(response.optInt("StatusCode") == 200){
                    setOtpLayout();
                }else{
                    Toast.makeText(getApplicationContext(), response.optString("ErrMsg"), Toast.LENGTH_SHORT).show();
                    ((TextView) findViewById(R.id.loginButton)).setText(R.string.singup);
                }
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.loginButton)).setText(R.string.singup);
            }
        });
    }
    private void otpValidateRequest(String otp){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
            params.put("MobileNo", phone.getText().toString().trim());
            params.put("OtpType", 0);
            params.put("OTP", otp);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "ValidateOtp", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(response.optInt("StatusCode") == 200){
                    signUp();
                }else{
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), response.optString("ErrMsg"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(isSignUpScreen){
            setLoginLayout();
            isSignUpScreen = false;
        }else{
            super.onBackPressed();
        }
    }

    private void startTimer(){
        stopTimer();
        countDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ((TextView)findViewById(R.id.timer)).setText(Utils.getTime(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                findViewById(R.id.timer).setVisibility(View.GONE);
                findViewById(R.id.resendOTP).setVisibility(View.VISIBLE);
            }
        };
        findViewById(R.id.timer).setVisibility(View.VISIBLE);
        countDownTimer.start();
    }

    private void stopTimer(){
        findViewById(R.id.resendOTP).setVisibility(View.GONE);
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void togglePasswordVisiblity(){
        if(password.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
            passwordVisibleIcon.setImageResource(R.drawable.baseline_visibility_off_black_18);
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        else{
            passwordVisibleIcon.setImageResource(R.drawable.baseline_visibility_black_18);
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    AlertDialog dialog;
    private void showDialog(String msg) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.alert_dialog, null);
            TextView message = promptsView.findViewById(R.id.message);
            message.setText(msg);
            TextView logout = promptsView.findViewById(R.id.ok);
            logout.setText("Logout");
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    ServerApi.logoutDevice(getApplicationContext(), loginStudentId, loginDeviceType, new ServerApi.CompleteListener() {
                        @Override
                        public void response(JSONObject response) {
                            Toast.makeText(getApplicationContext(), "Successfully logout from other device", Toast.LENGTH_SHORT).show();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }

                        @Override
                        public void error(String error) {
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    });
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

    private void checkEmailMobileRegistration(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        JSONObject params = new JSONObject();
        try{
            params.put("EmailID", email.getText().toString());
            params.put("MobileNo", phone.getText().toString());
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(this, ServerApi.BASE_URL, "checkEmailMobileRegistration", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {

                //findViewById(R.id.progressBar).setVisibility(View.GONE); //when otp issue resolve un-comment this line
                signUp(); //when otp issue resolve remove this line
                //otpGenerateRequest(); comment due to otp issue
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
