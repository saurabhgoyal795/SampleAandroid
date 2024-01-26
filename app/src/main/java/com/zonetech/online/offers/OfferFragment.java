package com.zonetech.online.offers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
public class OfferFragment extends Fragment implements OfferAdapter.ClickListener{
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private OfferAdapter adapter = null;

    public OfferFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_news, container, false);
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
        JSONObject params = new JSONObject();
        try{
            params.put("StudentID",  Utils.getStudentId(getActivity()));
            params.put("EmailID",  Utils.getEmail(getActivity()));
            params.put("SpecializationID", Utils.getStudentSpecID(getActivity()));

        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }

        ServerApi.callServerApi(getActivity(), ServerApi.BASE_URL,"StudentPromoCodes", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                Utils.saveObject(getActivity(), response.toString(), "StudentPromoCodes");
                setList(response.optJSONArray("Body"));
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
                adapter = new OfferAdapter(data,R.layout.news_item,getActivity(), this);
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }
    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getActivity(), "StudentPromoCodes");
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

    @Override
    public void onClick(JSONObject item) {
        String promoCode = item.optString("Promo_Code");
        Intent intent = new Intent();
        intent.putExtra("promoCode", promoCode);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}