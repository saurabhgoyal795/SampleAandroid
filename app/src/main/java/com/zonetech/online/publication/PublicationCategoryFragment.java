package com.zonetech.online.publication;

import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class PublicationCategoryFragment extends Fragment {
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private PublicationCategoryAdapter adapter = null;
    private Bundle mTmpState;
    private boolean mIsReentering;

    JSONObject jsonObject = new JSONObject();

    public PublicationCategoryFragment(JSONObject dataObject) {
        jsonObject = dataObject;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_test_series_plan, container, false);
        mRecyclerView = rootView.findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        if(!isAdded()){
            return rootView;
        }
        mLayoutManager = new GridLayoutManager(getActivity(),2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        setList(jsonObject.optJSONArray("books"));
        if(Utils.isLollipop()){
            SharedElementCallback mCallback = new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    try {
                        if (mIsReentering) {
                            int oldPosition = mTmpState.getInt(PublicationDetailsActivity.EXTRA_OLD_ITEM_POSITION);
                            int currentPosition = mTmpState.getInt(PublicationDetailsActivity.EXTRA_CURRENT_ITEM_POSITION);
                            if (currentPosition != oldPosition) {
                                String newTransitionName = "publish_" + currentPosition;

                                PublicationCategoryAdapter.PublicationViewHolder holder = (PublicationCategoryAdapter.PublicationViewHolder) mRecyclerView.findViewHolderForAdapterPosition(currentPosition);
                                if (holder != null) {
                                    View newSharedView = holder.planImage;
                                    if (newSharedView != null) {
                                        names.clear();
                                        names.add(newTransitionName);
                                        sharedElements.clear();
                                        sharedElements.put(newTransitionName, newSharedView);
                                    }
                                }
                            }
                            mTmpState = null;
                        }
                    }catch (Exception e){
                        if(Utils.isDebugModeOn){
                            e.printStackTrace();
                        }
                    }
                }
            };
            if(!isAdded()){
                return rootView;
            }
            getActivity().setExitSharedElementCallback(mCallback);
        }
        return rootView;
    }

    private void setList(JSONArray data){
        if (data != null) {
            if(adapter == null) {
                if(!isAdded()){
                    return;
                }
                adapter = new PublicationCategoryAdapter(data,R.layout.publication_category_item,getActivity());
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityReenter(Intent data) {
        mIsReentering = true;
        mTmpState = new Bundle(data.getExtras());
        int oldPosition = mTmpState.getInt(PublicationDetailsActivity.EXTRA_OLD_ITEM_POSITION);
        int currentPosition = mTmpState.getInt(PublicationDetailsActivity.EXTRA_CURRENT_ITEM_POSITION);
        if (oldPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition);
        }
        if(!isAdded()){
            return;
        }
        getActivity().postponeEnterTransition();
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                mRecyclerView.requestLayout();
                if(!isAdded()){
                    return false;
                }
                getActivity().startPostponedEnterTransition();
                return true;
            }
        });
    }
}