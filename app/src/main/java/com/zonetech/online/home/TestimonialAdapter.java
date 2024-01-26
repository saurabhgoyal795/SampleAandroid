package com.zonetech.online.home;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestimonialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    JSONArray values;
    Activity activity;
    public TestimonialAdapter(Activity activity, JSONArray values){
        this.values = values;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flipper_topper_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.optJSONObject(holder.getAdapterPosition());
        viewHolder.rollNo.setText("Roll No.: "+viewHolder.item.optString("RollNo"));
        viewHolder.name.setText(viewHolder.item.optString("Name"));
        viewHolder.rank.setText(viewHolder.item.optString("Ranks"));
        viewHolder.exam.setText("Exam: "+viewHolder.item.optString("ExamName"));
        viewHolder.spec.setText(viewHolder.item.optString("SpecName"));
        if(Utils.isActivityDestroyed(activity)){
            return;
        }
        Glide.with(activity)
                .load(ServerApi.TESTIMONIAL_IMAGE_URL + viewHolder.item.optString("ImagePath"))
                .circleCrop()
                .into(viewHolder.image);
    }

    public void refreshValues(JSONArray values){
        this.values = values;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return values.length();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public JSONObject item;
        public TextView name;
        public TextView rank;
        public TextView rollNo;
        public ImageView image;
        public TextView exam;
        public TextView spec;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            name = view.findViewById(R.id.name);
            image = view.findViewById(R.id.image);
            rank = view.findViewById(R.id.rank);
            rollNo = view.findViewById(R.id.rollNo);
            exam = view.findViewById(R.id.exam);
            spec = view.findViewById(R.id.spec);
        }
    }
}
