package com.zonetech.online.classes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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

class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.NewsViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView planText;
        private TextView newsDate;
        private TextView viewMore;
        public NewsViewHolder(View v) {
            super(v);
            planText = v.findViewById(R.id.planText);
            newsDate = v.findViewById(R.id.newsDate);
            viewMore = v.findViewById(R.id.viewMore);
        }
    }

    public SubjectsAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public SubjectsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new SubjectsAdapter.NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SubjectsAdapter.NewsViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.setText(planItem.optJSONObject(position).optString("TopicName"));
        SpannableString content = new SpannableString("View More");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.viewMore.setText(content);
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        if (!planItem.optJSONObject(position).has("URL")){
            holder.viewMore.setVisibility(View.GONE);
        } else {
            holder.viewMore.setVisibility(View.VISIBLE);
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
