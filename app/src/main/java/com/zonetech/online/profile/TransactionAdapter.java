package com.zonetech.online.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.common.CommonWebViewActivity;
import com.zonetech.online.testseries.TestPackageDetailsActivity;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.NewsViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView planText;
        private TextView newsDate;
        private TextView status;
        private TextView amount;
        private TextView discount;
        private RelativeLayout view;

        public NewsViewHolder(View v) {
            super(v);
            planText = v.findViewById(R.id.planText);
            newsDate = v.findViewById(R.id.newsDate);
            status = v.findViewById(R.id.status);
            amount = v.findViewById(R.id.amount);
            discount = v.findViewById(R.id.discount);
            view = v.findViewById(R.id.view);
        }
    }

    public TransactionAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public TransactionAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TransactionAdapter.NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TransactionAdapter.NewsViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.setText(planItem.optJSONObject(position).optString("PlanName"));
        if (planItem.optJSONObject(position).has("DurationText") && planItem.optJSONObject(position).optString("DurationText") != "") {
            holder.newsDate.setText("Duration: "+planItem.optJSONObject(position).optString("DurationText"));
        } else {
            holder.newsDate.setText("Txnid: "+planItem.optJSONObject(position).optString("Txnid"));
        }
        if (planItem.optJSONObject(position).optInt("Status") == 6){
            holder.status.setText("Successfull");
        } else  if (planItem.optJSONObject(position).optInt("Status") == 3){
            holder.status.setText("Returned Back");
        } else {
            holder.status.setText("Not Completed");
        }
        holder.amount.setText("Amount: "+ planItem.optJSONObject(position).optDouble("Amount") + "");
        if (planItem.optJSONObject(position).has("PayDate") && planItem.optJSONObject(position).optString("PayDate") != "") {
            holder.discount.setText("Paid On: "+planItem.optJSONObject(position).optString("PayDate") + "");
        } else {
            holder.discount.setText("Discount: "+planItem.optJSONObject(position).optDouble("Discount") + "");
        }
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://onlinezonetech.in/student/PrintForm?TransactionID="+planItem.optJSONObject(position).optString("TransactionIDEncrypt")));
                context.startActivity(browserIntent);
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
