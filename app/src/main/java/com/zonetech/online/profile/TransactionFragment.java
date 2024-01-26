package com.zonetech.online.profile;

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
public class TransactionFragment extends Fragment {
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private TransactionAdapter adapter = null;

    public TransactionFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_transaction, container, false);
        mRecyclerView = rootView.findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        if(!isAdded()){
            return rootView;
        }
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        JSONObject params = new JSONObject();

        try{
            if(!isAdded()){
                return rootView;
            }
            params.put("StudentID",  Utils.getStudentId(getActivity()));
            params.put("PageSize",  20);
            params.put("PageNo",  1);

        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        if(!isAdded()){
            return rootView;
        }
        ServerApi.callServerApi(getActivity(), ServerApi.TESTING_BASE_URL,"transactions",params,  new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if (Utils.isActivityDestroyed(getActivity())) {
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                if (response.optJSONArray("Body") != null) {
                    setList(response.optJSONArray("Body"));
                }
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
                adapter = new TransactionAdapter(data,R.layout.transaction_item,getActivity());
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }

}