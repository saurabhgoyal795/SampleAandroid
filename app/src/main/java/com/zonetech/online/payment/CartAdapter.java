package com.zonetech.online.payment;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.telecom.CallAudioState;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.zonetech.online.R;
import com.zonetech.online.classes.ClassPackageDetailsActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.testseries.TestPackageDetailsActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public interface ClickListener{
        void removeCart(int cartId);
    }
    JSONArray values;
    Activity activity;
    private ClickListener clickListener;
    private DisplayMetrics metrics;
    public CartAdapter(Activity activity, JSONArray values, ClickListener listener){
        this.values = values;
        this.activity = activity;
        this.clickListener = listener;
        metrics = Utils.getMetrics(activity);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.optJSONObject(holder.getAdapterPosition());
        if(Utils.isValidString(viewHolder.item.optString("SubjectName"))){
            viewHolder.title.setText(viewHolder.item.optString("SubjectName"));
        }else{
            viewHolder.title.setText(viewHolder.item.optString("OrgPlanName"));
        }
        viewHolder.type.setText(viewHolder.item.optInt("TypeID") == 1 ? activity.getString(R.string.title_activity_class_series_pan) : activity.getString(R.string.title_activity_test_series_pan));
        setPriceText(viewHolder.priceView, viewHolder.item);
        if(Utils.isActivityDestroyed(activity)){
            return;
        }
        String imagePath = viewHolder.item.optString("ImageURL");
        String imageUrl = ServerApi.IMAGE_URL + imagePath;
        if (viewHolder.item.optInt("SubjectID" ) > 0 ) {
            imageUrl = ServerApi.SUBJECT_URL + imagePath;
        }
        if(Utils.isValidString(imagePath)){
            Glide.with(activity)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if(resource != null){
                                int width = resource.getWidth();
                                int height = resource.getHeight();
                                viewHolder.cartImage.getLayoutParams().width = (int)(80 * metrics.density);
                                int imageHeight = (int)((height * 80 * metrics.density)/width*1f);;
                                viewHolder.cartImage.getLayoutParams().height = imageHeight;
                                viewHolder.cartImage.setImageBitmap(resource);
                            }
                        }
                    });
        }else{
            Glide.with(activity)
                    .clear(viewHolder.cartImage);
        }

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    viewHolder.item.put("OrgPlanID", viewHolder.item.optInt("PackageID"));
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
                String transitionName = "class_"+position;
                Intent intent = new Intent(activity, ClassPackageDetailsActivity.class);
                if(viewHolder.item.optInt("TypeID") == 0) {    //test series cart
                    intent = new Intent(activity, TestPackageDetailsActivity.class);
                    transitionName = "test_" + position;
                }
                intent.putExtra("item", viewHolder.item.toString());
                intent.putExtra("position", position);
                intent.putExtra("isCart", true);
                if (Utils.isLollipop()) {
                    ((ViewHolder) holder).cartImage.setTransitionName(transitionName);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, viewHolder.cartImage, transitionName);
                    activity.startActivity(intent, options.toBundle());
                } else {
                    activity.startActivity(intent);
                }
            }
        });
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.removeCart(viewHolder.item.optInt("CartID"));
                }
            }
        });
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
        public TextView title;
        public ImageView cartImage;
        public TextView type;
        public TextView priceView;
        public ImageView deleteButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.title);
            cartImage = view.findViewById(R.id.cartImage);
            type = view.findViewById(R.id.type);
            priceView = view.findViewById(R.id.priceView);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
    private void setPriceText(TextView priceView, JSONObject data){
        try{
            double mrp = data.optDouble("PlanMRP");
            double price = data.optDouble("Fees");
            String currency = activity.getString(R.string.currency);
            long discount = Math.round(((mrp - price)/mrp)*100);
            String mrpString = currency + Math.round(mrp);
            String priceString = currency + Math.round(price);
            String text = String.format(activity.getResources().getString(R.string.cart_price), mrpString, priceString, discount+"%");
            if(data.has("Duration")){
                text = String.format(activity.getResources().getString(R.string.cart_plan_price), mrpString, priceString, discount+"%", data.optInt("Duration"));
            }
            int index = text.indexOf(mrpString);
            SpannableString strNew = new SpannableString(text);
            StrikethroughSpan span = new StrikethroughSpan();
            strNew.setSpan(span, index, index + mrpString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            priceView.setText(strNew);
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
