package com.zonetech.online.mypackage.onlineTestSeries;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestActivity;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestResultActivity;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.testseries.TestPackageDetailsActivity;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.CAFlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class TestSeriesDetailsAdapter extends RecyclerView.Adapter<TestSeriesDetailsAdapter.ViewHolder> {
    private JSONArray planItem;
    private Activity context;
    private DisplayMetrics metrics;
    private String[] buttonTitle = {"Download Sample Copy", "Download Q. Paper", "Download Solution", "Download Result", "Download Checked Copy", "Upload Answer Sheet", "View Uploaded Copy"};
    private String[] onlineButtonTitle = {"Paper", "Result", "Solution", "Video Solution"};

    public TestSeriesDetailsAdapter(JSONArray planItem, Activity context) {
        this.planItem = planItem;
        this.context = context;
        metrics = Utils.getMetrics(context);
    }

    @Override
    public TestSeriesDetailsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mytest_product_subject_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestSeriesDetailsAdapter.ViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(position);
        holder.examName.setText(item.optString("ExamName"));
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
        if(holder.getAdapterPosition() == planItem.length() - 1){
            params.bottomMargin = (int)(10*metrics.density);
        }else{
            params.bottomMargin = 0;
        }
        holder.view.setLayoutParams(params);
        int studentExamID = item.optInt("StudentExamID");
        int examStatus = item.optInt("ExamStatus");
        Boolean ISOffiline = item.optBoolean("IsOffline"); //means copy upload wala
        boolean requireRollNo = item.optBoolean("RequireRollNo");
        String status = "Upcoming";
        switch (examStatus){
            case 1:
                status = "Active";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.ca_green));
                break;
            case 2:
                status = "Partial";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.ca_yellow));
                break;
            case 3:
                status = "Completed";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.ca_green));
                break;
            case 4:
                status = "Missed";
                holder.text4.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
        }

        if(examStatus == 0){
            holder.text4.setVisibility(View.GONE);
            if(item.optBoolean("ISDateLimit")){
                holder.text1.setText(item.optString("FromDate"));
                if(item.optBoolean("IsTimeBound")){
                    holder.text1.setText(item.optString("FromDate")+" "+item.optString("ExamTime"));
                }
                holder.text2.setText(item.optInt("TotalQuestion")+" Ques");
                holder.text2.setVisibility(View.VISIBLE);
                holder.text3.setText(item.optString("ExamDuration")+" Min");
            }else{
                holder.text3.setVisibility(View.VISIBLE);
                holder.text2.setVisibility(View.GONE);
                holder.text1.setText(item.optInt("TotalQuestion")+" Ques");
                holder.text3.setText(item.optString("ExamDuration")+" Min");
            }
        }else{
            holder.text2.setVisibility(View.GONE);
            if(Utils.isValidString(status)) {
                if(status.equalsIgnoreCase("active")) {
                    holder.text4.setText("Start Test");
                } else if (status.equalsIgnoreCase("completed")) {
                    holder.text4.setText("View Analysis");
                }else {
                    holder.text4.setText(status);
                }
                holder.text4.setVisibility(View.VISIBLE);
            }else{
                holder.text4.setVisibility(View.GONE);
            }
            holder.text3.setVisibility(View.VISIBLE);
            holder.text1.setText(item.optInt("TotalQuestion")+" Ques");
            holder.text3.setText(item.optString("ExamDuration")+" Min");
            holder.text1.setTypeface(holder.text1.getTypeface(), Typeface.NORMAL);
            holder.text3.setTypeface(holder.text3.getTypeface(), Typeface.NORMAL);
            holder.text1.setTextColor(ContextCompat.getColor(context, R.color.ca_blue));
            holder.text3.setTextColor(ContextCompat.getColor(context, R.color.ca_blue));
            holder.text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            holder.text3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            holder.text1.setAlpha(.7f);
            holder.text3.setAlpha(.7f);
            if(examStatus == 3){
                if(Utils.isValidString(item.optString("ObtainMarkText")) && !"null".equalsIgnoreCase(item.optString("ObtainMarkText"))){
                    holder.text1.setText(item.optString("ObtainMarkText"));
                    holder.text1.setTypeface(holder.text1.getTypeface(), Typeface.BOLD);
                    holder.text1.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    holder.text1.setAlpha(1f);
                    holder.text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
                if(Utils.isValidString(item.optString("RankText")) && !"null".equalsIgnoreCase(item.optString("RankText"))){
                    holder.text3.setText(item.optString("RankText"));
                    holder.text3.setTypeface(holder.text3.getTypeface(), Typeface.BOLD);
                    holder.text3.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    holder.text3.setAlpha(1f);
                    holder.text3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                }
            }
        }
        ArrayList<String> buttonList = new ArrayList<>();
        holder.buttonLayout.removeAllViews();
        if (ISOffiline == false ) {
            holder.buttonLayout.setVisibility(View.GONE);
            if(examStatus == 3) {
                if (Utils.isValidString(item.optString("ExamPaper"))) {
                    buttonList.add(onlineButtonTitle[0]);
                }
                if (Utils.isValidString(item.optString("ResultPath"))) {
                    buttonList.add(onlineButtonTitle[1]);
                }
                if (Utils.isValidString(item.optString("SolutionPath"))) {
                    buttonList.add(onlineButtonTitle[2]);
                }
                if (Utils.isValidString(item.optString("YTSolutionURL"))) {
                    buttonList.add(onlineButtonTitle[3]);
                }
                if (buttonList.size() > 0) {
                    holder.buttonLayout.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (examStatus == 1 || examStatus == 2) {
                holder.text4.setVisibility(View.INVISIBLE);
                holder.buttonLayout.setVisibility(View.VISIBLE);
                buttonList.add(buttonTitle[0]);
                buttonList.add(buttonTitle[1]);
                buttonList.add(buttonTitle[5]);
                if (!item.optString("UnCheckedCopyPath").equalsIgnoreCase("")) {
                    buttonList.add(buttonTitle[6]);
                }
            }else if (examStatus == 3) {
                holder.text4.setVisibility(View.INVISIBLE);
                holder.buttonLayout.setVisibility(View.VISIBLE);
                buttonList.add(buttonTitle[1]);
                buttonList.add(buttonTitle[2]);
                buttonList.add(buttonTitle[3]);
                buttonList.add(buttonTitle[4]);
            }else if (examStatus == 4) {
                holder.text4.setVisibility(View.INVISIBLE);
                holder.buttonLayout.setVisibility(View.VISIBLE);
                buttonList.add(buttonTitle[0]);
                buttonList.add(buttonTitle[1]);
                buttonList.add(buttonTitle[2]);
                buttonList.add(buttonTitle[3]);
            }else if (examStatus == 0) {
                holder.text4.setVisibility(View.INVISIBLE);
                holder.buttonLayout.setVisibility(View.VISIBLE);
                buttonList.add(buttonTitle[0]);
            }  else {
                holder.buttonLayout.setVisibility(View.GONE);
            }
        }
        if (ISOffiline) {
            for (int i = 0 ; i < buttonList.size() ; i++){
                View view = LayoutInflater.from(holder.buttonLayout.getContext()).inflate(R.layout.myclass_button_item, holder.buttonLayout, false);
                TextView button = view.findViewById(R.id.button);
                if(buttonList.get(i).toLowerCase().contains("download")){
                    button.setBackgroundResource(R.drawable.button_blue_rounded);
                }else{
                    button.setBackgroundResource(R.drawable.button_green_rounded_4dp);
                }
                button.setTextColor(ContextCompat.getColor(context, R.color.white));
                button.setText(buttonList.get(i));
                int finalI = i;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttonClicked(item, buttonList.get(finalI));
                    }
                });
                holder.buttonLayout.addView(view);
            }
        } else {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(1 == examStatus && requireRollNo && !Preferences.get(context, Preferences.KEY_IS_TEST_FORM_SUBMITTED, false)){
                        if(context instanceof TestSeriesDetailsActivity){
                            ((TestSeriesDetailsActivity)context).showDialog();
                            return;
                        }
                    }
                    if(3 == examStatus){
                        Intent intent = new Intent(context, TestResultActivity.class);
                        intent.putExtra("studentExamID", studentExamID);
                        intent.putExtra("title", item.optString("ExamName"));
                        intent.putExtra("examDuration", item.optInt("ExamDuration"));
                        context.startActivity(intent);
                    }else if(1 == examStatus || 2 == examStatus){
                        Intent intent = new Intent(context, TestActivity.class);
                        intent.putExtra("item", item.toString());
                        context.startActivity(intent);
                    }
                }
            });
            for (int i = 0 ; i < buttonList.size() ; i++){
                View view = LayoutInflater.from(holder.buttonLayout.getContext()).inflate(R.layout.myclass_button_item, holder.buttonLayout, false);
                TextView button = view.findViewById(R.id.button);
                button.setBackgroundResource(R.drawable.button_green_rounded_4dp);
                button.setTextColor(ContextCompat.getColor(context, R.color.white));
                button.setText(buttonList.get(i));
                int finalI = i;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onlineButtonClicked(item, buttonList.get(finalI));
                    }
                });
                holder.buttonLayout.addView(view);
            }
        }
    }

    @Override
    public int getItemCount() {
        return planItem.length();
    }

    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }
    protected class ViewHolder extends RecyclerView.ViewHolder {
        private TextView examName, text1, text2, text3, text4;
        private LinearLayout offlineLayout;
        private View view;
        private CAFlowLayout buttonLayout;
        public ViewHolder(View v) {
            super(v);
            view = v;
            examName = v.findViewById(R.id.examName);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            text3 = v.findViewById(R.id.text3);
            text4 = v.findViewById(R.id.text4);
            offlineLayout = v.findViewById(R.id.offlineLayout);
            buttonLayout = v.findViewById(R.id.buttonLayout);
        }
    }

    private void buttonClicked(JSONObject item, String title){
        String pdfPath = item.optString("CopySamplePath");
        String ExamPaperPath = item.optString("ExamPaper");
        String SolutionPath = item.optString("SolutionPath");
        String ResultPath = item.optString("ResultPath");
        String UnCheckedCopyPath = item.optString("UnCheckedCopyPath");
        String CheckedCopyPath = item.optString("CheckedCopyPath");
        int ExamID = item.optInt("ExamID");
        int orgPlanID = item.optInt("OrgPlanID");
        Intent i = new Intent(Intent.ACTION_VIEW);
        if(title.equalsIgnoreCase(buttonTitle[0])){
            if(!Utils.isValidString(pdfPath)){
                Utils.showLongToast(context,"Sample Copy is not uploaded yet, it will be upload soon");
                return;
            }
            i.setData(Uri.parse(ServerApi.PDF_SAMPLECOPY_PATH+pdfPath));
        }else if(title.equalsIgnoreCase(buttonTitle[1])){
            if(!Utils.isValidString(ExamPaperPath)){
                Utils.showLongToast(context,"Question Paper is not uploaded yet, it will be upload soon");
                return;
            }
            i.setData(Uri.parse(ServerApi.PDF_ExamPaper_PATH+ExamPaperPath));
        }else if(title.equalsIgnoreCase(buttonTitle[2])){
            i = new Intent(context.getApplicationContext(), PdfOpenActivity.class);
            i.putExtra("fileName", SolutionPath);
            i.putExtra("downloadPath", ServerApi.PDF_SolutionPath_PATH);
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
        }else if(title.equalsIgnoreCase(buttonTitle[3])){
            i = new Intent(context.getApplicationContext(), PdfOpenActivity.class);
            i.putExtra("fileName", ResultPath);
            i.putExtra("downloadPath", ServerApi.PDF_ExamResult_PATH);
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
        }else if(title.equalsIgnoreCase(buttonTitle[4])){
//            i = new Intent(context.getApplicationContext(), PdfOpenActivity.class);
//            i.putExtra("fileName", CheckedCopyPath);
//            i.putExtra("downloadPath", ServerApi.PDF_CheckedCopy_PATH+ExamID+"/");
//            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
            i.setData(Uri.parse(ServerApi.PDF_CheckedCopy_PATH+ExamID+"/"+CheckedCopyPath));
        }else if(title.equalsIgnoreCase(buttonTitle[5])){
            ((TestSeriesDetailsActivity)context).uploadFile(orgPlanID,ExamID);
            return;
        }else if(title.equalsIgnoreCase(buttonTitle[6])){
            i = new Intent(context.getApplicationContext(), PdfOpenActivity.class);
            i.putExtra("fileName", UnCheckedCopyPath);
            i.putExtra("downloadPath", ServerApi.PDF_UnCheckedCopy_PATH+ExamID+"/");
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
        }
        context.startActivity(i);
    }
    private void onlineButtonClicked(JSONObject item, String title){
        String paper = item.optString("ExamPaper");
        String result = item.optString("ResultPath");
        String solution = item.optString("SolutionPath");
        String video = item.optString("YTSolutionURL");
        Intent i = new Intent(context.getApplicationContext(), PdfOpenActivity.class);
        if(title.equalsIgnoreCase(onlineButtonTitle[0])){
            i.putExtra("fileName", paper);
            i.putExtra("downloadPath", ServerApi.PDF_ExamPaper_PATH);
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
        }else if(title.equalsIgnoreCase(onlineButtonTitle[1])){
            i.putExtra("fileName", result);
            i.putExtra("downloadPath", ServerApi.PDF_ExamResult_PATH);
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
        }else if(title.equalsIgnoreCase(onlineButtonTitle[2])){
            i.putExtra("fileName", solution);
            i.putExtra("downloadPath", ServerApi.PDF_SolutionPath_PATH);
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
            i.putExtra("basePath", context.getFilesDir() + "/pdf/");
        }else if(title.equalsIgnoreCase(onlineButtonTitle[3])){
            i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(video));
        }
        context.startActivity(i);
    }
}

