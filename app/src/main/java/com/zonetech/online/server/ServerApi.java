package com.zonetech.online.server;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zonetech.online.BuildConfig;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ServerApi {
    public static final int MY_SOCKET_TIMEOUT_MS = 60000;
    public static final String BASE_URL =  "https://onlinezonetech.in/api/app/";
    public static final String WEB_URL = "https://cms.zonetech.in/api/web/";
    public static final String TESTIMONIAL_IMAGE_URL = "https://cms.zonetech.in/upload/AchivementImg/";
    public static final String IMAGE_URL = "https://onlinezonetech.in/Upload/Package/";
    public static final String SUBJECT_URL = "https://onlinezonetech.in/Upload/Subject/";
    public static final String PDF_BASE_PATH = "https://onlinezonetech.in/Upload/TimeTable/";
    public static final String SUBJECT_PDF_PATH = "https://onlinezonetech.in/Upload/SubjectNotes/";
    public static final String TEST_SOLUTION_PATH = "https://onlinezonetech.in/ExamSolution/";
    public static final String BOOK_BASE_PATH = "https://cms.zonetech.in/Upload/Books/";
    public static final String PROFILE_BASE_PATH = "https://onlinezonetech.in/Upload/StudentProfile/";
    public static final String BASE_IMAGE_PATH = "https://onlinezonetech.in/";
//    public static final String TESTING_BASE_URL = "https://app.onlinezonetech.in/api/app/";
    public static final String TESTING_BASE_URL = "https://onlinezonetech.in/api/app/";
    public static final String PDF_SAMPLECOPY_PATH = "https://onlinezonetech.in/SampleCopy/";
    public static final String PDF_SolutionPath_PATH = "https://onlinezonetech.in/ExamSolution/";
    public static final String PDF_ExamPaper_PATH = "https://onlinezonetech.in/ExamPaper/";
    public static final String PDF_ExamResult_PATH = "https://onlinezonetech.in/ExamResult/";
    public static final String PDF_UnCheckedCopy_PATH = "https://onlinezonetech.in/UnCheckedCopy/";
    public static final String PDF_CheckedCopy_PATH = "https://onlinezonetech.in/CheckedCopy/";

    public static final String TAG = "ServerApi";
    public interface CompleteListener{
        void response(JSONObject response);
        void error(String error);
    }
    public interface CompleteListenerArray{
        void response(JSONArray response);
        void error(String error);
    }
    public static void callServerApi(Context context, String baseUrl, String actionName, JSONObject params, final CompleteListener completeListener){
        String request = baseUrl + actionName;
        try {
            if (params != null) {
                params.put("versionCode", BuildConfig.VERSION_CODE);
                params.put("versionName", BuildConfig.VERSION_NAME);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(request, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(response != null){
                    Log.i(TAG, "response = "+response);
                    int statusCode = response.optInt("StatusCode");
                    if(200 == statusCode) {
                        if (completeListener != null) {
                            completeListener.response(response);
                        }
                    }else {
                        if("login".equalsIgnoreCase(actionName) || "register".equalsIgnoreCase(actionName)){
                            if (completeListener != null) {
                                completeListener.response(response);
                            }
                        }else {
                            if (completeListener != null) {
                                if(response.has("Message") && Utils.isValidString(response.optString("Message"))){
                                    completeListener.error(response.optString("Message"));
                                }else {
                                    completeListener.error(response.toString());
                                }
                            }
                        }
                    }
                }else{
                    if(completeListener != null){
                        completeListener.error("null response");
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "error = "+error.getMessage());
                if(completeListener != null){
                    completeListener.error(error.getMessage());
                }
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
    public static void callServerApiJsonArray(Context context, String baseUrl, String actionName, final CompleteListenerArray completeListener){
        String request = baseUrl + actionName;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(request, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if(response != null){
                    if (completeListener != null) {
                        completeListener.response(response);
                    }
                }else{
                    if (completeListener != null) {
                        completeListener.error("no items");
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (completeListener != null) {
                    completeListener.error(error.getMessage());
                }
            }
        });
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestSingleton.getInstance(context).addToRequestQueue(jsonArrayRequest);
    }
    public static void callServerApiJsonObject(Context context, String baseUrl, String actionName, final CompleteListener completeListener){
        String request = baseUrl + actionName;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(request, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(response != null){
                    if (completeListener != null) {
                        completeListener.response(response);
                    }
                }else{
                    if(completeListener != null){
                        completeListener.error("null response");
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(completeListener != null){
                    completeListener.error(error.getMessage());
                }
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public static void logoutDevice(Context context, int studentId, String deviceType, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("DeviceType", deviceType);
            params.put("StudentID", studentId);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, BASE_URL, "logoutDevice", params, new CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.response(response);
                }
            }

            @Override
            public void error(String error) {
                if(completeListener != null){
                    completeListener.error(error);
                }
            }
        });
    }
    public static void welcomeMsg(Context context, CompleteListener completeListener){
        ServerApi.callServerApi(context, BASE_URL, "WelcomeMessage", null, new CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.response(response);
                }
            }

            @Override
            public void error(String error) {
                if(completeListener != null){
                    completeListener.error(error);
                }
            }
        });
    }
    public static void alertMsg(Context context, CompleteListener completeListener){
        ServerApi.callServerApi(context, BASE_URL, "Getalert", null, new CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.response(response);
                }
            }

            @Override
            public void error(String error) {
                if(completeListener != null){
                    completeListener.error(error);
                }
            }
        });
    }
}
