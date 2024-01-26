package com.zonetech.online.mypackage.onlineClass;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.downloads.DownloadFileListActivity;
import com.zonetech.online.downloads.MyDownloadActivity;
import com.zonetech.online.player.PlayerActivity;
import com.zonetech.online.player.VimeoPlayer;
import com.zonetech.online.server.DownloadContent;
import com.zonetech.online.server.DownloadVimeoContent;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;


public class ClassVideoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    JSONArray values;
    Activity activity;
    int imageWidth,imageHeight,type;

    public ClassVideoListAdapter(Activity activity, JSONArray values, int type) {
        this.values = values;
        this.activity = activity;
        this.type = type;
        DisplayMetrics metrics = Utils.getMetrics(activity);
        if (metrics != null) {
            imageWidth = metrics.widthPixels - (int) (30 * metrics.density);
            imageWidth = imageWidth / 2;
            imageHeight = (int) ((imageWidth * 1f) / 1.77f);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.item = values.optJSONObject(holder.getAdapterPosition());
        viewHolder.title.setText(viewHolder.item.optString("VideoTitle"));
        if (viewHolder.item.optString("VideoDescription").trim().equalsIgnoreCase("")) {
            viewHolder.description.setVisibility(View.GONE);
        } else {
            viewHolder.description.setVisibility(View.VISIBLE);
            viewHolder.description.setText(viewHolder.item.optString("VideoDescription"));
        }
        viewHolder.image.getLayoutParams().height = imageHeight;
        String videoUrl = viewHolder.item.optString("VideoURL");
        String imagePath = viewHolder.item.optString("ImagePath");
        boolean isYoutubeLink = false;
        if (videoUrl.contains("youtube.com")) {
            if (videoUrl.contains("https://www.youtube.com/watch?v=")) {
                videoUrl = videoUrl.replace("https://www.youtube.com/watch?v=", "");
                isYoutubeLink = true;
            } else if (videoUrl.contains("https://www.youtube.com/embed/")) {
                videoUrl = videoUrl.replace("https://www.youtube.com/embed/", "");
                isYoutubeLink = true;
            }
            if(isYoutubeLink){
                int index = videoUrl.indexOf("&");
                if (index > 0) {
                    videoUrl = videoUrl.substring(0, index);
                }
                imagePath = "https://img.youtube.com/vi/" + videoUrl +"/mqdefault.jpg";
            }
        } else {
            imagePath = VimeoPlayer.getVideoImagePath(activity, videoUrl);
        }
        if (Utils.isValidString(imagePath)) {
            if(!imagePath.startsWith("http")){
                imagePath = ServerApi.BASE_IMAGE_PATH +"Upload/Subject/" + imagePath;
            }
            if(Utils.isActivityDestroyed(activity)){
                return;
            }
            Glide.with(activity)
                    .load(imagePath)
                    .override(imageWidth, imageHeight)
                    .thumbnail(0.2f)
                    .into(viewHolder.image);
        }else{
            Glide.with(activity).clear(viewHolder.image);
        }
        String finalVideoUrl = videoUrl;
        boolean finalIsYoutubeLink = isYoutubeLink;
        String finalImagePath = imagePath;
        viewHolder.query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, QueryActivity.class);
                String playUrl = viewHolder.item.optString("PlayURL");
                if(Utils.isValidString(playUrl) && !playUrl.contains("youtube.com")){
                    intent.putExtra("videoUrl", playUrl);
                }else{
                    intent.putExtra("videoUrl", finalVideoUrl);
                }
                intent.putExtra("id", MyPackageClassDetailsActivity.planId);
                intent.putExtra("topicId", viewHolder.item.optInt("TopicID"));
                intent.putExtra("title", viewHolder.item.optString("VideoTitle"));
                intent.putExtra("queryType", QueryActivity.VIDEO_QUERY);
                activity.startActivity(intent);
            }
        });
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PlayerActivity.class);
                String playUrl = viewHolder.item.optString("PlayURL");
                if(Utils.isValidString(playUrl) && !playUrl.contains("youtube.com")){
                    intent.putExtra("videoUrl", playUrl);
                    String channelName = viewHolder.item.optString("ChanelName");
                    intent.putExtra("channelName", channelName);
                    intent.putExtra("isYoutubeVideo", false);
                }else{
                    intent.putExtra("videoUrl", finalVideoUrl);
                    intent.putExtra("isYoutubeVideo", finalIsYoutubeLink);
                }
                intent.putExtra("isOfflineEnabled", type == 1);
                intent.putExtra("imagePath", finalImagePath);
                intent.putExtra("title", viewHolder.item.optString("VideoTitle"));
                intent.putExtra("position", position);
                if (Utils.isLollipop()) {
                    viewHolder.image.setTransitionName("video_" + position);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, viewHolder.image, "video_" + position);
                    activity.startActivity(intent, options.toBundle());
                }else{
                    activity.startActivity(intent);
                }
                savePlayVideoHistory(viewHolder.item.optInt("PackageID"), viewHolder.item.optInt("VideoID"));
            }
        });
        if (viewHolder.item.optBoolean("IsNew")) {
            viewHolder.newVideos.setVisibility(View.VISIBLE);
        } else {
            viewHolder.newVideos.setVisibility(View.GONE);
        }
        viewHolder.downloaded.setVisibility(View.GONE);
        viewHolder.delete.setVisibility(View.GONE);
        viewHolder.downloadButton.setVisibility(View.GONE);
        viewHolder.downloading.setVisibility(View.GONE);
        viewHolder.downloadCancel.setVisibility(View.GONE);
        if(type == 0){
            if(viewHolder.item.optBoolean("isFileDownload")){
                viewHolder.downloaded.setVisibility(View.VISIBLE);
            }else{
                if(viewHolder.item.optBoolean("isDownloading")){
                    viewHolder.downloading.setVisibility(View.VISIBLE);
                    if(!"Downloading...".equalsIgnoreCase(viewHolder.item.optString("downloadingText"))) {
                        viewHolder.downloadCancel.setVisibility(View.VISIBLE);
                    }
                    viewHolder.downloading.setText(viewHolder.item.optString("downloadingText"));
                }else {
                    viewHolder.downloadButton.setVisibility(View.VISIBLE);
                }
            }
        }else{
            viewHolder.delete.setVisibility(View.VISIBLE);
        }
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String urlString = finalVideoUrl;
                    String playUrl = viewHolder.item.optString("PlayURL");
                    if(Utils.isValidString(playUrl) && !playUrl.contains("youtube.com")){
                        urlString = playUrl;
                    }
                    if (activity instanceof DownloadFileListActivity) {
                        ((DownloadFileListActivity) activity).deleteVideo(urlString, false);
                        LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(MyDownloadActivity.REFRESH_DOWNLOAD_LIST));
                    } else {
                        ((ClassVideoListActivity) activity).showDialog(urlString);
                    }
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }
        });
        viewHolder.downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Utils.isValidString(viewHolder.item.optString("DownloadURL")) && !viewHolder.item.optString("DownloadURL").contains("youtube.com")){
                        if(!DownloadVimeoContent.isVimeoDownloading && Utils.isConnectedToInternet(activity)){
                            viewHolder.item.put("downloadingText", "Downloading...");
                            viewHolder.item.put("isDownloading", true);
                            notifyItemChanged(position);
                        }
                        ((ClassVideoListActivity) activity).downloadVimeoFiles(viewHolder.item.optString("DownloadURL"), viewHolder.item.optString("ChanelName"),viewHolder.item.toString());
                    }else{
                        if(!DownloadContent.isDownloading && Utils.isConnectedToInternet(activity)){
                            viewHolder.item.put("downloadingText", "Downloading...");
                            viewHolder.item.put("isDownloading", true);
                            notifyItemChanged(position);
                        }
                        ((ClassVideoListActivity) activity).downloadFiles(finalVideoUrl, viewHolder.item.toString());
                    }
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }
        });
        viewHolder.downloadCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(Utils.isValidString(viewHolder.item.optString("DownloadURL")) && !viewHolder.item.optString("DownloadURL").contains("youtube.com")) {
                        ((ClassVideoListActivity) activity).cancelVimeoDownload();
                    }else {
                        ((ClassVideoListActivity) activity).cancelDownload();
                    }
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return values.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public JSONObject item;
        public TextView title, description, newVideos, downloadButton, downloaded, delete, downloading, downloadCancel;
        public ImageView image;
        private LinearLayout query;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.title);
            image = view.findViewById(R.id.image);
            query = view.findViewById(R.id.query);
            description = view.findViewById(R.id.description);
            newVideos = view.findViewById(R.id.newVideos);
            delete = view.findViewById(R.id.delete);
            downloaded = view.findViewById(R.id.downloaded);
            downloadButton = view.findViewById(R.id.downloadButton);
            downloading = view.findViewById(R.id.downloading);
            downloadCancel = view.findViewById(R.id.downloadCancel);
        }
    }

    public void refreshValues(JSONArray values) {
        this.values = values;
        DisplayMetrics metrics = Utils.getMetrics(activity);
        if (metrics != null) {
            imageWidth = metrics.widthPixels - (int) (30 * metrics.density);
            imageWidth = imageWidth / 2;
            imageHeight = (int) ((imageWidth * 1f) / 1.77f);
        }
        notifyDataSetChanged();
    }

    private void savePlayVideoHistory(int packageId, int videoId) {
        JSONObject params = new JSONObject();
        try {
            params.put("StudentID", Utils.getStudentId(activity));
            params.put("PackageID", packageId);
            params.put("VideoID", videoId);
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
        ServerApi.callServerApi(activity, ServerApi.BASE_URL, "SavePlayVideoHistory", params, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {

            }

            @Override
            public void error(String error) {

            }
        });
    }
}
