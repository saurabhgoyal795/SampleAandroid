package com.zonetech.online.mypackage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineClass.MyPackageClassDetailsActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestSeriesDetailsActivity;
import com.zonetech.online.payment.RenewPackageActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class MyPackageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class TestPlanViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        private TextView text1;
        private TextView text2;
        private TextView text3;
        private TextView text4;
        View view;
        public TestPlanViewHolder(View v) {
            super(v);
            view = v;
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            text3 = v.findViewById(R.id.text3);
            text4 = v.findViewById(R.id.text4);
        }
    }

    protected class TestRenewPlanViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText, text1, text2, text3, text4, reNewPriceView, renewButton, offerText;
        View view;
        public TestRenewPlanViewHolder(View v) {
            super(v);
            view = v;
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            text1 = v.findViewById(R.id.text1);
            text2 = v.findViewById(R.id.text2);
            text3 = v.findViewById(R.id.text3);
            text4 = v.findViewById(R.id.text4);
            reNewPriceView = v.findViewById(R.id.reNewPriceView);
            renewButton = v.findViewById(R.id.renewButton);
            offerText = v.findViewById(R.id.offerText);
        }
    }

    public MyPackageAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        if(metrics != null){
            imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
            viewHolder = new MyPackageAdapter.TestPlanViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mypackage_renew_item, parent, false);
            viewHolder = new MyPackageAdapter.TestRenewPlanViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return planItem.optJSONObject(position).optInt("itemType", 0);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        if(viewHolder.getItemViewType() == 0){
            TestPlanViewHolder holder = (TestPlanViewHolder)viewHolder;
            final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
            holder.planText.setText(planItem.optJSONObject(position).optString("PlanName"));
            if (planItem.optJSONObject(position).optBoolean("IsFree")  || planItem.optJSONObject(position).optString("IsFreeText") =="false"){
                holder.text4.setText("Free");
                holder.text4.setVisibility(View.VISIBLE);
            } else {
                holder.text4.setText("Paid");
                holder.text4.setVisibility(View.INVISIBLE);
            }
            if (planItem.optJSONObject(position).optInt("TypeID") == 1){
                if(item.optInt("NewVideos") > 0){
                    int newVideos = item.optInt("NewVideos");
                    holder.text1.setText(newVideos + (newVideos > 1 ? " new videos" : " new video"));
                }else{
                    holder.text1.setText("0 new video");
                }
//            holder.text2.setText(planItem.optJSONObject(position).optInt("TotalExam")+" Subject");
                holder.text3.setText(planItem.optJSONObject(position).optInt("Duration")+" Days left");
            }else{
                holder.text1.setTextColor(ContextCompat.getColor(context, R.color.ca_blue));
                holder.text1.setAlpha(.70f);
                holder.text1.setText("Total "+planItem.optJSONObject(position).optInt("TotalExam")+" Tests");
                if(Utils.isValidString(planItem.optJSONObject(position).optString("DurationText"))){
                    holder.text3.setText(planItem.optJSONObject(position).optString("DurationText"));
                }else{
                    holder.text3.setText("Expire till exam");
                }
//            holder.text2.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (planItem.optJSONObject(position).optInt("TypeID") == 1){
                        //Open Video Details Fragment
                        Intent intent = new Intent(context, MyPackageClassDetailsActivity.class);
                        intent.putExtra("item", item.toString());
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(context, TestSeriesDetailsActivity.class);
                        intent.putExtra("item", item.toString());
                        context.startActivity(intent);
                    }
//                Intent intent = new Intent(context, TestPackageDetailsActivity.class);
//                intent.putExtra("item", item.toString());
//                context.startActivity(intent);
                }
            });
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
            if(holder.getAdapterPosition() == planItem.length() - 1){
                params.bottomMargin = (int)(10*metrics.density);
            }else{
                params.bottomMargin = 0;
            }
            holder.view.setLayoutParams(params);
        }else{
            TestRenewPlanViewHolder holder = (TestRenewPlanViewHolder)viewHolder;
            final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
            holder.planText.setText(planItem.optJSONObject(position).optString("PlanName"));
            holder.text4.setVisibility(View.INVISIBLE);
            holder.text1.setVisibility(View.INVISIBLE);
            holder.text3.setText(planItem.optJSONObject(position).optInt("Duration")+" Days left");
            setPriceView(context, item, holder.reNewPriceView);
            String offerText = String.format(context.getString(R.string.offer_text), planItem.optJSONObject(position).optInt("Duration"));
            holder.offerText.setText(offerText+" 80%");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MyPackageClassDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }
            });
            holder.renewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RenewPackageActivity.class);
                    intent.putExtra("item", item.toString());
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
        }
    }

    @Override
    public int getItemCount() {
        return planItem.length();
    }
    public void refreshAdapter(JSONArray items) {
        try {
            planItem = new JSONArray(items.toString());
            notifyDataSetChanged();
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    private void setPriceView(Context context, JSONObject data, TextView priceView){
        try {
            data = data.optJSONArray("OrganizationPlanValidityList").optJSONObject(0);
            double mrp = data.optDouble("MRP");
            double price = data.optDouble("Fees");
            int planDuration = data.optInt("PlanDuration");
            String currency = context.getString(R.string.currency);
            long discount = Math.round(((mrp - price) / mrp) * 100);
            String mrpString = currency + Math.round(mrp);
            String priceString = currency + Math.round(price);
            String text = String.format(context.getResources().getString(R.string.plan_price_item_value), mrpString, priceString, discount + "%", planDuration);
            int index = text.indexOf(mrpString);
            SpannableString strNew = new SpannableString(text);
            StrikethroughSpan span = new StrikethroughSpan();
            strNew.setSpan(span, index, index + mrpString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            priceView.setText(strNew);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
}
