package com.zonetech.online.mypackage.onlineClass;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyPackageClassVideoItemAdapter extends RecyclerView.Adapter<MyPackageClassVideoItemAdapter.MyPackageClassPlanProductItemHolder> {
    private JSONArray planItem;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;


    protected class MyPackageClassPlanProductItemHolder extends RecyclerView.ViewHolder {
        private TextView planText;
        private View view;
        private TextView newVideos;
        public MyPackageClassPlanProductItemHolder(View v) {
            super(v);
            planText = v.findViewById(R.id.planText);
            newVideos = v.findViewById(R.id.newVideos);
            view = v;
        }
    }

    public MyPackageClassVideoItemAdapter(JSONArray planItem, Activity context) {
        this.planItem = planItem;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int) (32 * metrics.density)) / 2;
    }

    @Override
    public MyPackageClassVideoItemAdapter.MyPackageClassPlanProductItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.class_product_subject_video_item, parent, false);
        return new MyPackageClassVideoItemAdapter.MyPackageClassPlanProductItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyPackageClassVideoItemAdapter.MyPackageClassPlanProductItemHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.setText("Chapter "+(position+1)+" :"+planItem.optJSONObject(position).optString("TopicName"));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ClassVideoListActivity.class);
                intent.putExtra("videoList", item.optJSONArray("PackageTopicVideoList").toString());
                intent.putExtra("title", item.optString("TopicName"));
                context.startActivity(intent);
            }
        });
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
        if(holder.getAdapterPosition() == planItem.length() - 1){
            params.bottomMargin = (int)(10*metrics.density);
        }else{
            params.bottomMargin = 0;
        }
        holder.view.setLayoutParams(params);
        if(item.optInt("NewVideos") > 0){
            int newVideos = item.optInt("NewVideos");
            holder.newVideos.setText(newVideos + (newVideos > 1 ? " new videos" : " new video"));
            holder.newVideos.setVisibility(View.VISIBLE);
        }else{
            holder.newVideos.setVisibility(View.GONE);
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
}

