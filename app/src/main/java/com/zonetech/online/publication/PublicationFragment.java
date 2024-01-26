package com.zonetech.online.publication;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
public class PublicationFragment extends Fragment {
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private PublicationAdapter adapter = null;

    public PublicationFragment() {
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
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        checkCacheValues();
        if(!isAdded()){
            return rootView;
        }
        ServerApi.callServerApiJsonArray(getActivity(), ServerApi.WEB_URL,"books",  new ServerApi.CompleteListenerArray() {
            @Override
            public void response(JSONArray response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                Utils.saveObject(getActivity(), response.toString(), "books");
                setList(response);
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void setList(JSONArray data){
        if (data != null) {
            if(adapter == null) {
                if(!isAdded()){
                    return;
                }
                adapter = new PublicationAdapter(data,R.layout.publication_item,getActivity());
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }
    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getActivity(), "books");
            if(Utils.isValidString(value)){
                JSONArray response = new JSONArray(value);
                setList(response);
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
}