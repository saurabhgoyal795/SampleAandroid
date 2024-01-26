package com.zonetech.online.downloads;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineClass.ClassVideoListActivity;
import com.zonetech.online.player.PlayerActivity;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;


import java.io.File;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    File[] values;
    Activity activity;
    String folderName;
    public DownloadAdapter(Activity activity, File[] values, String folderName){
        this.values = values;
        this.activity = activity;
        this.folderName = folderName;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values[holder.getAdapterPosition()];
        String title = viewHolder.item.getName();
        title = title.replaceAll("_", " ");
        title = title.substring(0, title.indexOf(".pdf"));
        viewHolder.title.setText(title);
        viewHolder.share.setVisibility(View.GONE);
        viewHolder.fileImage.setVisibility(View.GONE);
        if("SubjectPdf".equalsIgnoreCase(folderName)){
            viewHolder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    share(viewHolder.item);
                }
            });
            viewHolder.share.setVisibility(View.VISIBLE);
        }
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if("SubjectPdf".equalsIgnoreCase(folderName)){
                    Intent intent = new Intent(activity, PdfOpenActivity.class);
                    intent.putExtra("fileName", viewHolder.item.getName());
                    intent.putExtra("downloadPath", ServerApi.TEST_SOLUTION_PATH);
                    intent.putExtra("basePath", activity.getFilesDir() + "/SubjectPdf/");
                    activity.startActivity(intent);
                }
            }
        });
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = viewHolder.item.getAbsolutePath();
                new File(filePath).delete();
                ((DownloadFileListActivity)activity).getDownloadFiles(folderName);
                LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(MyDownloadActivity.REFRESH_DOWNLOAD_LIST));
            }
        });
    }

    public void refreshValues(File[] values){
        this.values = values;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return values.length;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public File item;
        public TextView title;
        public ImageView share, fileImage, delete;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.title);
            share = view.findViewById(R.id.share);
            delete = view.findViewById(R.id.delete);
            fileImage = view.findViewById(R.id.fileImage);
        }
    }

    private void share(File file){
        String emailChooserHeading = "Choose an option to share";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName(), file);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            activity.startActivity(Intent.createChooser(shareIntent, emailChooserHeading));
        } catch (Exception e) {
            if(Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
    }
}
