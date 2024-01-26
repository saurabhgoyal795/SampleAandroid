package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;

public class TestInstructions extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test_instructions, container, false);
        Bundle bundle = getArguments();
        String instructions = bundle.getString("instructions");
        WebView webView = view.findViewById(R.id.instructions);
        if(!isAdded()){
            return view;
        }
        int padding = (int)(16* Utils.getMetrics(getActivity()).density);
        webView.setPadding(padding, padding, padding, padding);
        webView.loadData(instructions, "text/html", "UTF-8");
        return view;
    }
}
