package com.zonetech.online.publication;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

class PublicationCategoryAdapter extends RecyclerView.Adapter<PublicationCategoryAdapter.PublicationViewHolder> {

    public static JSONArray values;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeight;
    private float ratio;

    public class PublicationViewHolder extends RecyclerView.ViewHolder {
        public ImageView planImage;
        public TextView planText;
        public RelativeLayout imageLayout;
        public PublicationViewHolder(View v) {
            super(v);
            planImage = v.findViewById(R.id.planImage);
            planText = v.findViewById(R.id.planText);
            imageLayout = v.findViewById(R.id.imageLayout);
        }
    }

    public PublicationCategoryAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.values = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        imageWidth = (metrics.widthPixels - (int)(40*metrics.density))/2;
        imageHeight = (int)(imageWidth * 1.29f);
    }

    @Override
    public PublicationCategoryAdapter.PublicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new PublicationCategoryAdapter.PublicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PublicationCategoryAdapter.PublicationViewHolder holder, final int position) {
        final JSONObject item = values.optJSONObject(holder.getAdapterPosition());
        String imagePath = item.optString("ImagePath");
        holder.imageLayout.getLayoutParams().width = imageWidth;
        holder.imageLayout.getLayoutParams().height = imageHeight;
        if(Utils.isValidString(imagePath)) {
            imagePath = ServerApi.BOOK_BASE_PATH +imagePath;
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .asBitmap()
                    .load(imagePath)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if(resource != null) {
                                int height = resource.getHeight();
                                int width = resource.getWidth();
                                imageHeight = (height * imageWidth)/width;
                                holder.imageLayout.getLayoutParams().width = imageWidth;
                                holder.imageLayout.getLayoutParams().height = imageHeight;
                                holder.planImage.setImageBitmap(resource);
                                holder.imageLayout.getChildAt(0).setVisibility(View.GONE);
                            }
                        }
                    });
        }else{
            if(Utils.isActivityDestroyed(context)){
                return;
            }
            Glide.with(context)
                    .clear(holder.planImage);
        }
        holder.planText.setText(values.optJSONObject(position).optString("BookTitle"));
        if(position == values.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PublicationDetailsActivity.class);
                intent.putExtra("ratio", (imageHeight * 1f) / (imageWidth * 1f));
                intent.putExtra(PublicationDetailsActivity.EXTRA_CURRENT_ITEM_POSITION, position);
                if(Utils.isLollipop()){
                    int finalPostion = holder.getAdapterPosition() == -1 ? position : holder.getAdapterPosition();
                    holder.planImage.setTransitionName("publish_"+finalPostion);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(context, holder.planImage, "publish_"+finalPostion);
                    context.startActivity(intent, options.toBundle());
                }else {
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.length();
    }
    public void refreshAdapter(JSONArray items) {
        values = items;
        notifyDataSetChanged();
    }
}
