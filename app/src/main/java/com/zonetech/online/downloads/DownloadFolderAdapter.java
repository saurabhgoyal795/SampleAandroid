package com.zonetech.online.downloads;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import java.util.ArrayList;
import java.util.HashMap;

public class DownloadFolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<HashMap<String, Object>> values;
    Activity activity;
    public DownloadFolderAdapter(Activity activity, ArrayList<HashMap<String, Object>> values){
        this.values = values;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download_folder_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.get(holder.getAdapterPosition());
        viewHolder.title.setText((String)viewHolder.item.get("title"));
        viewHolder.size.setText(viewHolder.item.get("size")+"");
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, DownloadFileListActivity.class);
                intent.putExtra("folderName", (String)viewHolder.item.get("folderName"));
                intent.putExtra("title", (String)viewHolder.item.get("title"));
                activity.startActivity(intent);
            }
        });
    }

    public void refreshValues(ArrayList<HashMap<String, Object>> values){
        this.values = values;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public HashMap<String, Object> item;
        public TextView title, size;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            title = view.findViewById(R.id.title);
            size = view.findViewById(R.id.size);
        }
    }
}
