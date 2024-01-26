package com.zonetech.online.mypackage.onlineClass;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.downloads.DownloadFileListActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

public class FragmentVideoList extends Fragment {
    private View rootView;
    private RecyclerView videoListView;
    private JSONArray videoList;
    private ClassVideoListAdapter classVideoListAdapter;
    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.video_list_fragment, container, false);
        videoListView = rootView.findViewById(R.id.videoList);
        Bundle bundle = getArguments();
        int type = bundle.getInt("type");
        setList(type);
        return rootView;
    }
    public void setList(int type){
        if(!isAdded()){
            return;
        }
        if(type == 0){
            videoList = ((ClassVideoListActivity)getActivity()).videoList;
        }else{
            if(getActivity() instanceof DownloadFileListActivity){
                videoList = ((DownloadFileListActivity)getActivity()).videoListDownload;
            }else{
                videoList = ((ClassVideoListActivity)getActivity()).videoListDownload;
            }
        }
        Log.i("VideoFTesting", "type = "+type);
        Log.i("VideoFTesting", "videoList = "+videoList);
        if(videoList == null || videoList.length() == 0){
            videoListView.setVisibility(View.GONE);
            rootView.findViewById(R.id.noVideos).setVisibility(View.VISIBLE);
        }else{
            videoListView.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.noVideos).setVisibility(View.GONE);
            if(classVideoListAdapter == null) {
                if(!isAdded()){
                    return;
                }
                videoListView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                classVideoListAdapter = new ClassVideoListAdapter(getActivity(), videoList, type);
                videoListView.setAdapter(classVideoListAdapter);
            }else{
                classVideoListAdapter.refreshValues(videoList);
            }
        }
    }
}
