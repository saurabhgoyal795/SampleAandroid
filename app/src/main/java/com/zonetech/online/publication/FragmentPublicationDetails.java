package com.zonetech.online.publication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

public class FragmentPublicationDetails extends Fragment {
    private ImageView bannerImage;
    private DisplayMetrics metrics;
    private int imageWidth;
    private int imageHeight;
    private JSONObject itemObj;
    private float ratio;
    private View rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_publication_details, container, false);
        bannerImage = rootView.findViewById(R.id.bannerImage);
        if(!isAdded()){
            return rootView;
        }
        metrics = Utils.getMetrics(getActivity());
        Bundle bundle = getArguments();
        if(bundle != null){
            ratio = bundle.getFloat("ratio");
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                ((TextView)rootView.findViewById(R.id.title)).setText(itemObj.optString("OrgPlanName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
            if(Utils.isLollipop()){
                int position = bundle.getInt("position");
                bannerImage.setTransitionName("publish_"+position);
                rootView.findViewById(R.id.parentView).getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        rootView.findViewById(R.id.parentView).getViewTreeObserver().removeOnPreDrawListener(this);
                        if(!isAdded())
                            return false;
                        getActivity().startPostponedEnterTransition();
                        return true;
                    }
                });
            }
        }
        setImageViewSize();
        setUI(itemObj);
        return rootView;
    }

    private void setUI(JSONObject data){
        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        setViews(data);
        rootView.findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
    }

    private void setViews(JSONObject data){
        String currency = getString(R.string.currency);
        String description  = data.optString("Descriptions");
        TextView descriptionView = rootView.findViewById(R.id.description);
        descriptionView.setText(Html.fromHtml(description));
        ((TextView)rootView.findViewById(R.id.title)).setText(data.optString("BookTitle"));
        ((TextView)rootView.findViewById(R.id.sellingPrice)).setText(currency+ data.optString("SellingPrice"));
        ((TextView)rootView.findViewById(R.id.mrp)).setText(currency+ data.optString("MRP"));
        ((TextView)rootView.findViewById(R.id.isbn)).setText(data.optString("ISBN"));
        setBannerImage();
        rootView.findViewById(R.id.flipkart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.optString("FlipkartURL")));
                startActivity(browserIntent);

            }
        });
        rootView.findViewById(R.id.amazon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data.optString("AmazonURL")));
                startActivity(browserIntent);

            }
        });
    }


    private void setImageViewSize(){
        imageHeight = (int)(240*metrics.density);
        imageWidth = (int)((imageHeight*1f)/ratio);
        rootView.findViewById(R.id.imageLayout).getLayoutParams().width = imageWidth;
        rootView.findViewById(R.id.imageLayout).getLayoutParams().height = imageHeight;
    }
    private void setBannerImage(){
        String imagePath = itemObj.optString("ImagePath");
        if(Utils.isValidString(imagePath)) {
            imagePath = ServerApi.BOOK_BASE_PATH + imagePath;
            if(Utils.isActivityDestroyed(getActivity())){
                return;
            }
            Glide.with(this)
                    .load(imagePath)
                    .override(imageWidth, imageHeight)
                    .into(bannerImage);
        }
    }
    @Nullable
    public ImageView getSharedElement() {
        return bannerImage;
    }
}
