package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;


public class TopperListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    JSONArray values;
    Activity activity;

    public TopperListAdapter(Activity activity, JSONArray values){
        this.values = values;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.test_topper_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.optJSONObject(holder.getAdapterPosition());
        int rank = viewHolder.item.optInt("Ranking");
        viewHolder.rank.setText(rank+"");
        viewHolder.name.setText(viewHolder.item.optString("StudentName"));
        double markObtained = viewHolder.item.optDouble("RightMarks") - viewHolder.item.optDouble("WrongMark");
        String marks = String.format(Locale.US, "%.2f", (float)markObtained);
        viewHolder.marks.setText(marks);
        viewHolder.duration.setText("");
        if(1 == rank){
            viewHolder.rank.setBackgroundResource(R.drawable.circle_gold);
            viewHolder.rank.setTextColor(ContextCompat.getColor(activity, R.color.white));
            viewHolder.rank.setAlpha(1f);
        }else if(2 == rank){
            viewHolder.rank.setBackgroundResource(R.drawable.circle_silver);
            viewHolder.rank.setTextColor(ContextCompat.getColor(activity, R.color.white));
            viewHolder.rank.setAlpha(1f);
        }else if(3 == rank){
            viewHolder.rank.setBackgroundResource(R.drawable.circle_branze);
            viewHolder.rank.setTextColor(ContextCompat.getColor(activity, R.color.white));
            viewHolder.rank.setAlpha(1f);
        }else{
            viewHolder.rank.setBackgroundResource(R.drawable.circle_grey);
            viewHolder.rank.setTextColor(ContextCompat.getColor(activity, R.color.ca_blue));
            viewHolder.rank.setAlpha(.87f);
        }
    }
    public void notifyItemChangedByPosition(JSONArray values, int position){
        this.values = values;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return values.length();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public JSONObject item;
        public TextView rank;
        public TextView name;
        public TextView marks;
        public TextView duration;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            rank = view.findViewById(R.id.rank);
            name = view.findViewById(R.id.name);
            marks = view.findViewById(R.id.marks);
            duration = view.findViewById(R.id.duration);
        }
    }
}
