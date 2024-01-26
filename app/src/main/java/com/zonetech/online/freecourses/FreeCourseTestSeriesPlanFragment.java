package com.zonetech.online.freecourses;

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
import com.zonetech.online.testseries.TestSeriesAdapter;
import com.zonetech.online.testseries.TestSeriesPlanActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FreeCourseTestSeriesPlanFragment extends Fragment {
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private FreeCourseTestSeriesAdapter adapter = null;
    private HashMap<Integer, JSONArray> listData = new HashMap<>();
    public ArrayList<Integer> courseIds = new ArrayList<>();
    public FreeCourseTestSeriesPlanFragment() {
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
        checkCacheValues();
        if(!isAdded()){
            return rootView;
        }
        ServerApi.callServerApi(getActivity(), ServerApi.BASE_URL,"FreeTestSeriesPackages/0", null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray data = response.optJSONArray("Body");
                if(!isAdded()){
                    return;
                }
                Utils.saveObject(getActivity(), data.toString(), "FreeTestSeriesPackages_0");
                setList(data);
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
            listData.clear();
            for (int i = 0 ; i < data.length() ; i++){
                JSONObject object = data.optJSONObject(i);
                int courseId = object.optInt("CourseID");
                JSONArray array = listData.get(courseId);
                if(array == null){
                    array = new JSONArray();
                }
                array.put(object);
                listData.put(courseId, array);
                JSONArray all = listData.get(-1);
                if(all == null){
                    all = new JSONArray();
                }
                all.put(object);
                listData.put(-1, all);
                if(!courseIds.contains(courseId)){
                    courseIds.add(courseId);
                }
            }
            if(adapter == null) {
                if(!isAdded()){
                    return;
                }
                adapter = new FreeCourseTestSeriesAdapter(listData.get(-1),R.layout.test_plan_item,getActivity());
                mRecyclerView.setAdapter(adapter);
            }else{
                adapter.refreshAdapter(data);
            }
            if(!isAdded()){
                return;
            }
        }
    }
    private void checkCacheValues(){
        String value = null;
        try{
            value = (String)Utils.getObject(getActivity(), "FreeTestSeriesPackages_0");
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
    public void setFliter(int courseId){
        adapter.refreshAdapter(listData.get(courseId));
    }
}