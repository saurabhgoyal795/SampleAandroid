package com.zonetech.online.mypackage.onlineTestSeries;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;

public class TestSeriesDetailsFragment extends Fragment {
    private View rootView;
    private RecyclerView testList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_test_series_details, container, false);
        testList = rootView.findViewById(R.id.testList);
        Bundle bundle = getArguments();
        try {
            int position = bundle.getInt("position");
            bundle = ((TestSeriesDetailsActivity)getActivity()).getItemData(position);
            JSONArray items = new JSONArray(bundle.getString("item"));
            if(items.length() == 0){
                TextView noTestText = rootView.findViewById(R.id.noTestText);
                String type = bundle.getString("type");
                noTestText.setText(String.format(getString(R.string.no_test_item), type));
                noTestText.setVisibility(View.VISIBLE);
            }else {
                if(!isAdded()){
                    return rootView;
                }
                testList.setLayoutManager(new LinearLayoutManager(getActivity()));
                if(!isAdded()){
                    return rootView;
                }
                testList.setAdapter(new TestSeriesDetailsAdapter(items, getActivity()));
            }
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        return rootView;
    }
}
