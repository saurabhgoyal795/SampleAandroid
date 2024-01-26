package com.zonetech.online.notification;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
public class NotificationFragment extends Fragment {
    RecyclerView mRecyclerView;
    RelativeLayout noItemLayout;
    private GridLayoutManager mLayoutManager;
    private NotificationAdapter adapter = null;

    public NotificationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_notification, container, false);
        mRecyclerView = rootView.findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        if(!isAdded()){
            return rootView;
        }
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        noItemLayout = rootView.findViewById(R.id.noItemLayout);
        JSONObject params = new JSONObject();
        try{
            params.put("StudentId",  Utils.getStudentId(getActivity()));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        checkCacheValues();
        if(!isAdded()){
            return rootView;
        }
        ServerApi.callServerApi(getActivity(), ServerApi.BASE_URL,"notifications", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                Utils.saveObject(getActivity(), response.toString(), "notifications");
                setList(response.optJSONArray("Body"));
            }

            @Override
            public void error(String error) {
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                if(!isAdded()){
                    return;
                }
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    private void setList(JSONArray data){
        if (data != null) {
            if (data.length() == 0){
                noItemLayout.setVisibility(View.VISIBLE);
            } else {
                noItemLayout.setVisibility(View.GONE);
            }
            if(adapter == null) {
                if(!isAdded()){
                    return;
                }
                adapter = new NotificationAdapter(data,R.layout.notification_item,getActivity());
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }
    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getActivity(), "notifications");
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