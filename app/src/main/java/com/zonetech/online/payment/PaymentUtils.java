package com.zonetech.online.payment;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class PaymentUtils {
    public static void getCart(Context context, final CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID",  Utils.getStudentId(context));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "GetCart", params, new ServerApi.CompleteListener() {
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
    public static void addToCart(Context context, JSONObject params, final CompleteListener completeListener){
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "AddToCart", params, new ServerApi.CompleteListener() {
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

    public static void removeCart(Context context, int cartId, final CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(context));
            if(cartId >= 0) {
                params.put("CartID", cartId);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "RemoveToCart", params, new ServerApi.CompleteListener() {
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

    public static void buyPackage(Context context, long amount, long discount, String promoCode, long gst, final CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(context));
            params.put("Email", Utils.getEmail(context));
            params.put("Name", Utils.getName(context));
            params.put("Mobile", Utils.getPhone(context));
            params.put("StudentCode", Utils.getStudentCode(context));
            params.put("Amount", amount);
            params.put("IPAddress", "Android");
            params.put("Discount", discount);
            params.put("PromoCode", promoCode);
            params.put("Gst", gst);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "buypackage", params, new ServerApi.CompleteListener() {
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

    public static void buyRenewPackage(Context context, long amount, long discount, String promoCode, long gst, int planId, int subjectId, int duration ,final CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(context));
            params.put("Email", Utils.getEmail(context));
            params.put("Name", Utils.getName(context));
            params.put("Mobile", Utils.getPhone(context));
            params.put("StudentCode", Utils.getStudentCode(context));
            params.put("Amount", amount);
            params.put("IPAddress", "Android");
            params.put("Discount", discount);
            params.put("PromoCode", promoCode);
            params.put("Gst", gst);
            params.put("IsRenewal", true);
            params.put("SubjectID", subjectId);
            params.put("PlanID", planId);
            params.put("Duration", duration);

        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "buypackage", params, new ServerApi.CompleteListener() {
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

    public static void UpdateTransactionStatus(Context context, PaymentStatus paymentStatus, final CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("PayStatus", paymentStatus.payStatus);
            params.put("PGtype", paymentStatus.pGtype);
            params.put("PaymentID", paymentStatus.paymentID);
            params.put("Txnid", paymentStatus.txnid);
            params.put("BankRefNo", paymentStatus.bankRefNo);
            params.put("PaymentMode", paymentStatus.paymentMode);
            params.put("Remarks", paymentStatus.remarks);
            params.put("Status", paymentStatus.status);
            params.put("Discount", paymentStatus.discount);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "UpdateTransactionStatus", params, new ServerApi.CompleteListener() {
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

    public static void applypromo(Context context, String promoCode, double totalAmount, final CompleteListener completeListener){
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID", Utils.getStudentId(context));
            params.put("Promocode", promoCode);
            params.put("Fees", totalAmount);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(context, ServerApi.TESTING_BASE_URL, "applypromo", params, new ServerApi.CompleteListener() {
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
