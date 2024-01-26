package com.zonetech.online.mypackage.onlineClass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.BuildConfig;
import com.zonetech.online.R;
import com.zonetech.online.mypackage.MyPackageAdapter;
import com.zonetech.online.mypackage.PageViewModel;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */
public class MyPackageClassFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private MyPackageAdapter adapter = null;
    private View root;
    private JSONArray packageList;

    public static MyPackageClassFragment newInstance(int index) {
        MyPackageClassFragment fragment = new MyPackageClassFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_myclasspackage, container, false);
        mRecyclerView = root.findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        if(!isAdded()){
            return root;
        }
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //studentId is taken 4 here it is of harish
        if(!isAdded()){
            return root;
        }
        ServerApi.callServerApi(getActivity(), ServerApi.TESTING_BASE_URL,"Onlineclassstudentpackage?id="+ Utils.getStudentId(getActivity())+"&v="+BuildConfig.VERSION_CODE, null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray data = response.optJSONArray("Body");
                packageList = new JSONArray();
                if (data.length() == 0){
                    root.findViewById(R.id.noItemLayout).setVisibility(View.VISIBLE);
                } else {
                    root.findViewById(R.id.noItemLayout).setVisibility(View.GONE);
                }
                for (int i =0; i<data.length(); i++){
                    if (data.optJSONObject(i).optInt("TypeID") == 1 ){
                        try {
                            JSONObject item = data.optJSONObject(i);
                            item.put("itemType", 0);
                            packageList.put(item);
                        }catch (Exception e){
                            if(Utils.isDebugModeOn){
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (packageList.length() > 0) {
                    if(adapter == null) {
                        if(!isAdded()){
                            return;
                        }
                        adapter = new MyPackageAdapter(packageList,R.layout.mypackage_item,getActivity());
                        mRecyclerView.setAdapter(adapter);
                    }else{
                        adapter.refreshAdapter(packageList);
                    }
                    getRenewalPackages();
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }

    private void getRenewalPackages(){
        ServerApi.callServerApi(getActivity(), ServerApi.TESTING_BASE_URL,"StudentRenewal?id="+ Utils.getStudentId(getActivity())+"&v="+BuildConfig.VERSION_CODE, null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                JSONArray data = response.optJSONArray("Body");
                if (data != null && data.length() > 0) {
                    for(int i = 0 ; i < data.length() ; i++){
                        try {
                            data.optJSONObject(i).put("itemType", 1);
                        }catch (Exception e){
                            if(Utils.isDebugModeOn){
                                e.printStackTrace();
                            }
                        }
                    }
                    for(int i = 0 ; i < packageList.length() ; i++){
                        data.put(packageList.optJSONObject(i));
                    }
                    if(adapter == null) {
                        if(!isAdded()){
                            return;
                        }
                        adapter = new MyPackageAdapter(data,R.layout.mypackage_item,getActivity());
                        mRecyclerView.setAdapter(adapter);
                    }else{
                        adapter.refreshAdapter(data);
                    }
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
            }
        });
    }
}