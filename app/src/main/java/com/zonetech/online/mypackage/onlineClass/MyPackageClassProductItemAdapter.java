package com.zonetech.online.mypackage.onlineClass;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.testseries.TestPackageDetailsActivity;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.CAFlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class MyPackageClassProductItemAdapter extends RecyclerView.Adapter<MyPackageClassProductItemAdapter.MyPackageClassPlanProductItemHolder> {
    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;


    protected class MyPackageClassPlanProductItemHolder extends RecyclerView.ViewHolder {
        private ImageView planImage;
        private TextView planText;
        private TextView totalVideoLecture;
        private CAFlowLayout pdfLayout;
        private View view;
        public MyPackageClassPlanProductItemHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            pdfLayout = v.findViewById(R.id.pdfLayout);
            view = v;
            totalVideoLecture = v.findViewById(R.id.totalVideoLecture);
        }
    }

    public MyPackageClassProductItemAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int) (32 * metrics.density)) / 2;
    }

    @Override
    public MyPackageClassProductItemAdapter.MyPackageClassPlanProductItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new MyPackageClassProductItemAdapter.MyPackageClassPlanProductItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyPackageClassProductItemAdapter.MyPackageClassPlanProductItemHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("ImagePath");
        if (Utils.isValidString(imagePath)) {
            imagePath = ServerApi.BASE_IMAGE_PATH +"Upload/Subject/" + imagePath;
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
        holder.pdfLayout.setVisibility(View.GONE);
        holder.pdfLayout.removeAllViews();
        JSONArray docs = item.optJSONArray("SubjectDocumentList");
        if(docs != null && docs.length() > 0){
            String basePath = context.getFilesDir() + "/SubjectPdf/";
            for (int i = 0 ; i < docs.length() ; i++){
                View view = LayoutInflater.from(holder.pdfLayout.getContext()).inflate(R.layout.myclass_button_item, holder.pdfLayout, false);
                TextView button = view.findViewById(R.id.button);
                button.setText(docs.optJSONObject(i).optString("DisplayName"));
                final String pdfPath = docs.optJSONObject(i).optString("DocPath");
                if(new File(basePath+pdfPath).exists()){
                    button.setBackgroundResource(R.drawable.button_green_rounded_4dp);
                    button.setTextColor(ContextCompat.getColor(context, R.color.white));
                }else{
                    button.setBackgroundResource(R.drawable.button_blue_rounded);
                    button.setTextColor(ContextCompat.getColor(context, R.color.white));
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PdfOpenActivity.class);
                        intent.putExtra("fileName", pdfPath);
                        intent.putExtra("downloadPath", ServerApi.SUBJECT_PDF_PATH);
                        intent.putExtra("basePath", basePath);
                        context.startActivity(intent);
                    }
                });
                holder.pdfLayout.addView(view);
            }
            holder.pdfLayout.setVisibility(View.VISIBLE);
        }
        String videoText = planItem.optJSONObject(position).optString("Videos")+" Video Lectures";
        if(item.optInt("NewVideos") > 0){
            int newVideos = item.optInt("NewVideos");
            int startIndex = videoText.length() - 1;
            videoText = videoText + " ("+(newVideos + (newVideos > 1 ? " new videos" : " new video"))+")";
            Spannable wordtoSpan = new SpannableString(videoText);
            wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.ca_green)), startIndex + 3,  videoText.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.totalVideoLecture.setText(wordtoSpan);
        }else{
            holder.totalVideoLecture.setText(videoText);
        }
        holder.planText.setText(planItem.optJSONObject(position).optString("SubjectName"));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!planItem.optJSONObject(position).optString("Videos").trim().equalsIgnoreCase("0")){
                    Intent intent = new Intent(context, MyPackageClassVideoActivity.class);
                    intent.putExtra("item", item.toString());
                    context.startActivity(intent);
                }
            }
        });
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)holder.view.getLayoutParams();
        if(holder.getAdapterPosition() == planItem.length() - 1){
            params.bottomMargin = (int)(10*metrics.density);
        }else{
            params.bottomMargin = 0;
        }
        holder.view.setLayoutParams(params);
        holder.pdfLayout.setVisibility(View.VISIBLE);
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

