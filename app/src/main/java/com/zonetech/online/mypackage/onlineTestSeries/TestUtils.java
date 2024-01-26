package com.zonetech.online.mypackage.onlineTestSeries;

import android.content.Context;

import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class TestUtils {
    public static void examResult(Context context, int studentExamId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentExamID", studentExamId);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "ExamResult", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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

    public static void examDetails(Context context, int examId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("ExamID", examId);
            params.put("StudentID", Utils.getStudentId(context));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "ExamDetails", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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

    public static void startExam(Context context, int examId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("ExamID", examId);
            params.put("StudentID", Utils.getStudentId(context));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "StartExam", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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
    public static void saveAnswer(Context context, int studentExamId, int quesOptionId, int quesId, long quesTime, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentExamID", studentExamId);
            params.put("QuesOptionID", quesOptionId);
            params.put("QuestionID", quesId);
            params.put("QuesTime", quesTime);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "SaveAnswer", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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
    public static void submitExam(Context context, int studentExamId, int isSubmited, int lastAttempTime, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentExamID", studentExamId);
            params.put("StudentID", Utils.getStudentId(context));
            params.put("IsSubmited", isSubmited);
            params.put("LastAttempTime", lastAttempTime);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "SubmitExam", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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
    public static void examQuestions(Context context, int examId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("ExamID", examId);
            params.put("StudentID", Utils.getStudentId(context));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "ExamQuestions", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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
    public static void examSolutions(Context context, int studentExamId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentExamID", studentExamId);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "ExamResultQuestion", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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

    public static void practiceExamByCourse(Context context, int courseId, int planId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("CourseID", courseId);
            params.put("OrgPlanID", planId);
            params.put("StudentID", Utils.getStudentId(context));
            params.put("SpecializationID", Utils.getStudentSpecID(context));
            params.put("ExamName", "");
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "PracticeExamByCourse", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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

    public static void getTopperList(Context context, int studentExamId, CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentExamID", studentExamId);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "ExamTopperList", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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

    public static void getPromotions(Context context, CompleteListener completeListener){
        ServerApi.callServerApi(context, ServerApi.BASE_URL, "GetPromotions", null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(completeListener != null){
                    completeListener.success(response);
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
