package com.zonetech.online.offers;

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

class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.NewsViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    public interface ClickListener{
        void onClick(JSONObject item);
    }

    private ClickListener clickListener;

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

    public OfferAdapter(JSONArray planItem, int rowLayout, Activity context, ClickListener clickListener) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        this.clickListener = clickListener;
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public OfferAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new OfferAdapter.NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final OfferAdapter.NewsViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        if (item.optString("PlanName") == null || item.optString("PlanName") == "null"){
            holder.planText.setText("This promo code valid for all courses");
        } else {
            holder.planText.setText(item.optString("PlanName"));
        }
        String text = "Rs";
        if (item.optInt("DiscountType") == 2){
            text = "%";
        }
        holder.newsDate.setText("Code: "+item.optString("Promo_Code") + "  Discount: " + item.optDouble("Discount") + text);
        SpannableString content = new SpannableString("Apply");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.viewMore.setText(content);
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        if (context instanceof OfferDialogActivity){
            holder.viewMore.setVisibility(View.VISIBLE);
        } else {
            holder.viewMore.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(clickListener != null && context instanceof OfferDialogActivity){
                        clickListener.onClick(item);
                    }
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
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
