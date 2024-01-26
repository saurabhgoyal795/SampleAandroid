

package com.zonetech.online.classes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zonetech.online.R;
import com.zonetech.online.common.CommonWebViewActivity2;
import com.zonetech.online.player.PlayerActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class ClassProductItemAdapter extends RecyclerView.Adapter<ClassProductItemAdapter.ClassPlanProductItemHolder> {
    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;
    Boolean isfree;
    public interface ClickListener{
        void buy(int subjectId, int mrp, int fees, int duration);
    }
    private ClickListener clickListener;

    protected class ClassPlanProductItemHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        private TextView totalVideoLecture;
        private TextView priceView;
        private TextView buyButton;
        private TextView demoLink;
        private RelativeLayout demoLayout;
        private ImageView showTopic;
        private RecyclerView subjectrecylerView;
        public ClassPlanProductItemHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            totalVideoLecture = v.findViewById(R.id.totalVideoLecture);
            priceView = v.findViewById(R.id.priceView);
            buyButton = v.findViewById(R.id.buyButton);
            demoLink = v.findViewById(R.id.demoLink);
            demoLayout = v.findViewById(R.id.demoLayout);
            showTopic = v.findViewById(R.id.showTopic);
            subjectrecylerView = v.findViewById(R.id.subjectrecylerView);
        }
    }

    public ClassProductItemAdapter(JSONArray planItem, int rowLayout, Activity context, ClickListener clickListener, Boolean isfree) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int) (32 * metrics.density)) / 2;
        this.clickListener = clickListener;
        this.isfree = isfree;
    }

    @Override
    public ClassProductItemAdapter.ClassPlanProductItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new ClassProductItemAdapter.ClassPlanProductItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final ClassProductItemAdapter.ClassPlanProductItemHolder holder, final int position) {
        final boolean[] isOpened = {false};
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("ImagePath");
        String Videos = item.optString("Videos");
        try{
            String demoVideoURL = item.optString("DemoVideoURL");
             if(demoVideoURL != null && !demoVideoURL.trim().equalsIgnoreCase("")){
               holder.demoLink.setVisibility(View.VISIBLE);
             }else {
                 holder.demoLink.setVisibility(View.GONE);
                 holder.demoLayout.setVisibility(View.GONE);
             }
        }catch (Exception e){
            holder.demoLink.setVisibility(View.GONE);
            holder.demoLayout.setVisibility(View.GONE);
        }
        String videoUrl = item.optString("DemoVideoURL").trim();
        boolean isYoutubeLink = false;
        if(videoUrl.contains("youtube.com")){
            if(videoUrl.contains("https://www.youtube.com/watch?v=")){
                videoUrl = videoUrl.replace("https://www.youtube.com/watch?v=", "");
                isYoutubeLink = true;
            }else if(videoUrl.contains("https://www.youtube.com/embed/")){
                videoUrl = videoUrl.replace("https://www.youtube.com/embed/", "");
                isYoutubeLink = true;
            }
        }
        if(isYoutubeLink){
            int index = videoUrl.indexOf("&");
            if(index > 0){
                videoUrl = videoUrl.substring(0, index);
            }
            imagePath = "https://img.youtube.com/vi/" + videoUrl +"/mqdefault.jpg";
        }
        boolean finalIsYoutubeLink = isYoutubeLink;
        final String videoId = videoUrl;
        String finalImagePath = imagePath;
        holder.demoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalIsYoutubeLink){
                    Intent intent = new Intent(context, PlayerActivity.class);
                    intent.putExtra("videoUrl", videoId);
                    intent.putExtra("isYoutubeVideo", finalIsYoutubeLink);
                    intent.putExtra("imagePath", finalImagePath);
                    context.startActivity(intent);
                }else{
                    Intent browserIntent = new Intent(context, CommonWebViewActivity2.class);
                    String demoLink = item.optString("DemoVideoURL").trim();
                    browserIntent.putExtra("url", demoLink);
                    context.startActivity(browserIntent);
                }
            }
        });
        if (Utils.isValidString(imagePath)) {
            if(!imagePath.startsWith("http")) {
                imagePath = ServerApi.BASE_IMAGE_PATH + "Upload/Subject/" + imagePath;
            }
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .asBitmap()
                    .load(imagePath)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if(resource != null){
                                int width = resource.getWidth();
                                int height = resource.getHeight();
                                holder.planImage.getLayoutParams().height = (int)((height * 80 * metrics.density)/width*1f);
                                holder.planImage.setImageBitmap(resource);
                            }
                        }
                    });
        } else {
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .clear(holder.planImage);
        }
        holder.totalVideoLecture.setText(planItem.optJSONObject(position).optString("Videos")+" Video Lectures");
        holder.planText.setText(planItem.optJSONObject(position).optString("SubjectName"));
        try{
            double mrp = planItem.optJSONObject(position).optDouble("MRP");
            double price = planItem.optJSONObject(position).optDouble("Price");
            String currency = context.getString(R.string.currency);
            long discount = Math.round(((mrp - price)/mrp)*100);
            long planPrice = Math.round(price);
            String mrpString = currency + Math.round(mrp);
            String priceString = currency + Math.round(price);
            String text = String.format(context.getResources().getString(R.string.price_value), mrpString, priceString, discount+"%");
            int index = text.indexOf(mrpString);
            SpannableString strNew = new SpannableString(text);
            StrikethroughSpan span = new StrikethroughSpan();
            strNew.setSpan(span, index, index + String.valueOf(mrpString).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            holder.priceView.setText(strNew);
        }catch(Throwable e){
            e.printStackTrace();
        }
        holder.priceView.setVisibility(View.INVISIBLE);
        holder.buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.buy(item.optInt("SubjectID"),planItem.optJSONObject(position).optInt("MRP"),planItem.optJSONObject(position).optInt("Price"),planItem.optJSONObject(position).optInt("Duration", 90));
                }
            }
        });
        final GridLayoutManager[] mLayoutManager = new GridLayoutManager[1];
        mLayoutManager[0] = new GridLayoutManager(context,1);
        holder.subjectrecylerView.setLayoutManager(mLayoutManager[0]);
        if (holder.subjectrecylerView != null) {
            holder.subjectrecylerView.setHasFixedSize(true);
        }
        holder.showTopic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpened[0]){
                    isOpened[0] = true;
                    holder.showTopic.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_remove_black_48dp));
                    holder.subjectrecylerView.setVisibility(View.VISIBLE);
                    SubjectsAdapter adapter = new SubjectsAdapter(planItem.optJSONObject(position).optJSONArray("SubjectTopicList"),R.layout.subjects_item,context);
                    holder.subjectrecylerView.setAdapter(adapter);
                } else {
                    holder.showTopic.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_add_black_48dp));
                    isOpened[0] = false;
                    holder.subjectrecylerView.setVisibility(View.GONE);
                }
            }
        });
        holder.planImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpened[0]){
                    isOpened[0] = true;
                    holder.showTopic.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_remove_black_48dp));
                    holder.subjectrecylerView.setVisibility(View.VISIBLE);
                    SubjectsAdapter adapter = new SubjectsAdapter(planItem.optJSONObject(position).optJSONArray("SubjectTopicList"),R.layout.subjects_item,context);
                    holder.subjectrecylerView.setAdapter(adapter);
                } else {
                    holder.showTopic.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_add_black_48dp));
                    isOpened[0] = false;
                    holder.subjectrecylerView.setVisibility(View.GONE);
                }
            }
        });

        holder.planText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isOpened[0]){
                    isOpened[0] = true;
                    holder.showTopic.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_remove_black_48dp));
                    holder.subjectrecylerView.setVisibility(View.VISIBLE);
                    SubjectsAdapter adapter = new SubjectsAdapter(planItem.optJSONObject(position).optJSONArray("SubjectTopicList"),R.layout.subjects_item,context);
                    holder.subjectrecylerView.setAdapter(adapter);
                } else {
                    holder.showTopic.setImageDrawable(context.getResources().getDrawable(R.drawable.baseline_add_black_48dp));
                    isOpened[0] = false;
                    holder.subjectrecylerView.setVisibility(View.GONE);
                }
            }
        });
        if(isfree) {
           holder.priceView.setVisibility(View.GONE);
           holder.buyButton.setVisibility(View.GONE);
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

