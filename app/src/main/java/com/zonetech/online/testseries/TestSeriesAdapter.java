package com.zonetech.online.testseries;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.classes.ClassPackageDetailsActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestSeriesAdapter extends RecyclerView.Adapter<TestSeriesAdapter.TestPlanViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class TestPlanViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        private TextView totalExams;
        public TestPlanViewHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            totalExams = v.findViewById(R.id.extraDetails);
        }
    }

    public TestSeriesAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public TestSeriesAdapter.TestPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TestSeriesAdapter.TestPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestSeriesAdapter.TestPlanViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("ImageURL");
        if(Utils.isValidString(imagePath)) {
            imagePath = ServerApi.BASE_IMAGE_PATH + "Upload/Package/"+imagePath;
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .load(imagePath)
                    .override(imageWidth, (int)(150*metrics.density))
                    .error(R.drawable.samplepackage)
                    .placeholder(R.drawable.samplepackage)
                    .into(holder.planImage);
        }else{
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .clear(holder.planImage);
        }
        holder.planText.setText(planItem.optJSONObject(position).optString("OrgPlanName"));
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        holder.totalExams.setText("Total Exams: "+item.optString("ExamLimitText"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isLollipop()){
                    Intent intent = new Intent(context, TestPackageDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    intent.putExtra("position", position);
                    holder.planImage.setTransitionName("test_"+position);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, holder.planImage, "test_"+position);
                    context.startActivity(intent, options.toBundle());
                }else {
                    Intent intent = new Intent(context, TestPackageDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(planItem != null) {
            return planItem.length();
        }else{
            return 0;
        }
    }
    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }
}
