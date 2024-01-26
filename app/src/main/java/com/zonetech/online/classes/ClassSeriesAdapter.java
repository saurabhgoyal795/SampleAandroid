package com.zonetech.online.classes;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.publication.PublicationActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassSeriesAdapter extends RecyclerView.Adapter<ClassSeriesAdapter.ClassPlanViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class ClassPlanViewHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        private TextView priceText;
        public ClassPlanViewHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            priceText = v.findViewById(R.id.extraDetails);
        }
    }

    public ClassSeriesAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(32*metrics.density))/2;
    }

    @Override
    public ClassSeriesAdapter.ClassPlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new ClassSeriesAdapter.ClassPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ClassSeriesAdapter.ClassPlanViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("ImageURL");
        if(Utils.isValidString(imagePath)) {
            imagePath = ServerApi.IMAGE_URL +imagePath;
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
        holder.priceText.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        setPriceText(item, holder.priceText);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isLollipop()){
                    Intent intent = new Intent(context, ClassPackageDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    intent.putExtra("position", position);
                    holder.planImage.setTransitionName("class_"+position);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, holder.planImage, "class_"+position);
                    context.startActivity(intent, options.toBundle());
                }else {
                    Intent intent = new Intent(context, ClassPackageDetailsActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        try {
            return planItem.length();
        }catch (Exception e){
            return  0;
        }
    }
    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }
    private void setPriceText(JSONObject data, TextView priceView){
        try{
            JSONArray organizationPlanValidityList = data.optJSONArray("OrganizationPlanValidityList");
            if(organizationPlanValidityList != null && organizationPlanValidityList.length() > 0){
                data = organizationPlanValidityList.optJSONObject(0);
                data.put("PlanMRP", data.optDouble("MRP"));
            }
            setDefaultPrice(data, priceView);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setDefaultPrice(JSONObject data, TextView priceView){
        double mrp = data.optDouble("PlanMRP");
        double price = data.optDouble("Fees");
        if (mrp < 1 || price < 1) {
            priceView.setVisibility(View.GONE);
        }
        String currency = context.getString(R.string.currency);
        long discount = Math.round(((mrp - price)/mrp)*100);
        String mrpString = currency + Math.round(mrp);
        String priceString = currency + Math.round(price);
        String text = String.format(context.getResources().getString(R.string.price_item_value), mrpString, priceString);
        int index = text.indexOf(mrpString);
        SpannableString strNew = new SpannableString(text);
        StrikethroughSpan span = new StrikethroughSpan();
        strNew.setSpan(span, index, index + String.valueOf(mrpString).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        priceView.setText(strNew);
    }
}
