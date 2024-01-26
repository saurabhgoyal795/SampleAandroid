package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zonetech.online.R;

public class FragmentSubmit extends Fragment {
    View rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_test_submit, container, false);
        Bundle bundle = getArguments();
        String title = bundle.getString("title");
        String msg = String.format(getString(R.string.exam_submitted_msg), title);
        ((TextView)rootView.findViewById(R.id.submitted_msg)).setText(msg);
        return rootView;
    }
    public void submitted(){
        if(rootView != null) {
            rootView.findViewById(R.id.submitLayout).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.submitProgress).setVisibility(View.GONE);
        }
    }

    public void failedLayout(){
        if(rootView != null){
            rootView.findViewById(R.id.submitProgress).setVisibility(View.GONE);
            rootView.findViewById(R.id.failedLayout).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.tryAgain).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootView.findViewById(R.id.failedLayout).setVisibility(View.GONE);
                    rootView.findViewById(R.id.submitProgress).setVisibility(View.VISIBLE);
                    if(isAdded() && getActivity() instanceof TestActivity){
                        ((TestActivity)getActivity()).submitTestResponse();
                    }
                }
            });
        }
    }
}
