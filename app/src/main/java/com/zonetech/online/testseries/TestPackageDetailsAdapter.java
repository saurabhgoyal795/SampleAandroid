package com.zonetech.online.testseries;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.preferences.Preferences;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONObject;


public class TestPackageDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private JSONArray array;
    private Activity context;
    private DisplayMetrics metrics;

    protected class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ZTWebView descriptionView;
        private TextView startDate,endDate,totalTests,schedule;
        private LinearLayout featuresLayout;
        View view;
        public HeaderViewHolder(View v) {
            super(v);
            view = v;
            descriptionView = v.findViewById(R.id.description);
            startDate = v.findViewById(R.id.startDate);
            endDate = v.findViewById(R.id.endDate);
            totalTests = v.findViewById(R.id.totalTests);
            schedule = v.findViewById(R.id.schedule);
            featuresLayout = v.findViewById(R.id.featuresLayout);
        }
    }



    protected class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView examName, text1, text2, text3, startTest;
        private LinearLayout offlineLayout;
        private View view;
        private LinearLayout lockButton;
        public ItemViewHolder(View v) {
            super(v);
            view = v;
            examName = v.findViewById(R.id.examName);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            text3 = v.findViewById(R.id.text3);
            offlineLayout = v.findViewById(R.id.offlineLayout);
            lockButton = v.findViewById(R.id.lockButton);
            startTest = v.findViewById(R.id.startTest);
        }
    }

    public TestPackageDetailsAdapter(JSONArray array, Activity context) {
        this.array = array;
        this.context = context;
        metrics = Utils.getMetrics(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_test_details_header, parent, false);
            viewHolder = new TestPackageDetailsAdapter.HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_package_details_item, parent, false);
            viewHolder = new TestPackageDetailsAdapter.ItemViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return array.optJSONObject(position).optInt("itemType", 0);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if(viewHolder.getItemViewType() == 0){
            HeaderViewHolder holder = (HeaderViewHolder)viewHolder;
            final JSONObject item = array.optJSONObject(holder.getAdapterPosition());
            setViews(item, holder);
        }else{
            final JSONObject item = array.optJSONObject(position);
            ItemViewHolder holder = (ItemViewHolder)viewHolder;
            holder.examName.setText(item.optString("ExamName"));
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
            if(holder.getAdapterPosition() == array.length() - 1){
                params.bottomMargin = (int)(10*metrics.density);
            }else{
                params.bottomMargin = 0;
            }
            holder.view.setLayoutParams(params);
            int examStatus = item.optInt("ExamStatus");
            if(examStatus == 0){
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
                holder.text3.setVisibility(View.VISIBLE);
                holder.text1.setText(item.optInt("TotalQuestion")+" Ques");
                holder.text3.setText(item.optString("ExamDuration")+" Min");
            }
            if(item.optBoolean("isDemo")){
                if(item.optInt("ExamStatus") == 3){
                    holder.startTest.setText("View result");
                }else{
                    holder.startTest.setText("Free test");
                }
                holder.startTest.setVisibility(View.VISIBLE);
                holder.lockButton.setVisibility(View.GONE);
            }else{
                holder.startTest.setVisibility(View.GONE);
                holder.lockButton.setVisibility(View.VISIBLE);
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(item.optBoolean("isDemo")){
                        ((TestPackageDetailsActivity)context).startDemoTest();
                    }else{
                        ((TestPackageDetailsActivity)context).startPayment();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return array.length();
    }
    public void refreshAdapter(JSONArray items) {
        try {
            array = new JSONArray(items.toString());
            notifyDataSetChanged();
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private void setViews(JSONObject data, HeaderViewHolder view){
        JSONObject planDetails = data.optJSONObject("PlanDetails");
        String featureVideo = planDetails.optString("FeatureVideo");
        if(Utils.isValidString(featureVideo)){
            view.featuresLayout.setVisibility(View.VISIBLE);
            view.featuresLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(featureVideo));
                    context.startActivity(i);
                }
            });
        }


        String description  = planDetails.optString("Highlights");
        view.descriptionView.loadData(description, "text/html", "UTF-8");
        view.startDate.setText(planDetails.optString("DurationText"));
        view.endDate.setText(planDetails.optString("EndDate"));
        view.totalTests.setText(planDetails.optString("TotalExam"));
        JSONArray schedule = data.optJSONArray("Schedule");
        setScheduleView(schedule, view);
    }

    private void setScheduleView(JSONArray array, HeaderViewHolder holder){
        for (int i = 0 ; i < array.length() ; i++){
            if(array.optJSONObject(i).optInt("SpecializationId") == Preferences.get(context, Preferences.KEY_SPEC_ID, 0)){
                String pdfPath = array.optJSONObject(i).optString("FilePath");
                if(Utils.isValidString(pdfPath)){
                    holder.schedule.setVisibility(View.VISIBLE);
                    holder.schedule.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((TestPackageDetailsActivity)context).openPdf(pdfPath);
                        }
                    });
                }
                break;
            }
        }
    }
}
