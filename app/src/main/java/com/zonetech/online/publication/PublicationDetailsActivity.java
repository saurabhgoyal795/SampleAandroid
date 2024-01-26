package com.zonetech.online.publication;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.utils.Utils;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class PublicationDetailsActivity extends ZTAppCompatActivity {
    static final String EXTRA_CURRENT_ITEM_POSITION = "extra_current_item_position";
    static final String EXTRA_OLD_ITEM_POSITION = "extra_old_item_position";
    private ViewPager viewPager;
    private JSONObject itemObj;
    private float ratio;
    private int mCurrentPosition;
    private int mOriginalPosition;
    private boolean mIsReturning;
    private DetailsPagerAdapter detailsPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        viewPager = findViewById(R.id.viewPager);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            ratio = bundle.getFloat("ratio");
            String item = bundle.getString("item");
            try {
                itemObj = new JSONObject(item);
                ((TextView)findViewById(R.id.title)).setText(itemObj.optString("OrgPlanName"));
            }catch (Exception e){
                if(Utils.isDebugModeOn){
                    e.printStackTrace();
                }
            }
            mCurrentPosition = bundle.getInt(EXTRA_CURRENT_ITEM_POSITION);
            mOriginalPosition = mCurrentPosition;
        }
        if(Utils.isLollipop()){
            SharedElementCallback mCallback = new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

                    if (mIsReturning) {
                        ImageView sharedView = detailsPagerAdapter.getCurrentDetailsFragment().getSharedElement();
                        if (sharedView == null) {
                            names.clear();
                            sharedElements.clear();
                        } else if (mCurrentPosition != mOriginalPosition) {
                            names.clear();
                            sharedElements.clear();
                            names.add(sharedView.getTransitionName());
                            sharedElements.put(sharedView.getTransitionName(), sharedView);
                        }
                    }
                }
            };

            postponeEnterTransition();
            setEnterSharedElementCallback(mCallback);
        }
        detailsPagerAdapter = new DetailsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(detailsPagerAdapter);
        viewPager.addOnPageChangeListener(detailsPagerAdapter);
        viewPager.setCurrentItem(mCurrentPosition);
    }
    public class DetailsPagerAdapter extends FragmentStatePagerAdapter
            implements ViewPager.OnPageChangeListener {

        private FragmentPublicationDetails mCurrentFragment;
        public DetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            FragmentPublicationDetails fragment = new FragmentPublicationDetails();
            Bundle args = new Bundle();
            JSONObject item = PublicationCategoryAdapter.values.optJSONObject(position);
            args.putString("item", item.toString());
            args.putFloat("ratio", ratio);
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return PublicationCategoryAdapter.values.length();
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            mCurrentPosition = position;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentFragment = (FragmentPublicationDetails) object;
        }

        public FragmentPublicationDetails getCurrentDetailsFragment() {
            return mCurrentFragment;
        }
    }
    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_OLD_ITEM_POSITION, mOriginalPosition);
        data.putExtra(EXTRA_CURRENT_ITEM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }
}

