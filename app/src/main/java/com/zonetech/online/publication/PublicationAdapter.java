package com.zonetech.online.publication;

import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

class PublicationAdapter extends RecyclerView.Adapter<PublicationAdapter.PublicationViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class PublicationViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        public PublicationViewHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
        }
    }

    public PublicationAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public PublicationAdapter.PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new PublicationAdapter.PublicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PublicationAdapter.PublicationViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.setText(planItem.optJSONObject(position).optString("categoryName"));
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublicationCategoryActivity.class);
                intent.putExtra("item", item.toString());
                context.startActivity(intent);
            }
        });
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
