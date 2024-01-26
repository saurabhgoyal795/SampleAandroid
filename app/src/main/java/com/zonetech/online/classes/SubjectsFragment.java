package com.zonetech.online.classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;

public class SubjectsFragment extends Fragment {
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private SubjectsAdapter adapter = null;
    JSONArray subjectList = new JSONArray();
    public SubjectsFragment(JSONArray subjectList) {
        this.subjectList = subjectList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_subjects, container, false);
        mRecyclerView = rootView.findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        if(!isAdded()){
            return rootView;
        }
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        setList(subjectList);
        return rootView;
    }

    private void setList(JSONArray data){
        if (data != null) {
            if(adapter == null) {
                if(!isAdded()){
                    return;
                }
                adapter = new SubjectsAdapter(data,R.layout.subjects_item,getActivity());
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
        }
    }
    private void checkCacheValues(){
        String value = null;
        try{
            if(!isAdded()){
                return;
            }
            value = (String)Utils.getObject(getActivity(), "homenews");
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