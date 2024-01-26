package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;

import java.util.ArrayList;

public class QuestionsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    ArrayList<QuestionItem> values;
    Activity activity;

    public interface ClickListener{
        void questionClick(QuestionItem item);
    }
    private ClickListener clickListener;
    public QuestionsListAdapter(Activity activity, ArrayList<QuestionItem> values, ClickListener clickListener){
        this.values = values;
        this.activity = activity;
        this.clickListener = clickListener;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_number_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.get(holder.getAdapterPosition());
        viewHolder.number.setText((holder.getAdapterPosition() + 1)+"");
        if(viewHolder.item.isReviewed){
            viewHolder.background.setColorFilter(Color.parseColor("#8762be"));
        }else if(viewHolder.item.isAttempted){
            viewHolder.background.setColorFilter(Color.parseColor("#72bc1f"));
        }else if(viewHolder.item.isVisited){
            viewHolder.background.setColorFilter(ContextCompat.getColor(activity, R.color.red));
        }else{
            viewHolder.background.setColorFilter(Color.parseColor("#A9A9A9"));
        }
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(activity instanceof TestActivity) {
                        viewHolder.item.isVisited = true;
                    }
                    if(clickListener != null){
                        clickListener.questionClick(viewHolder.item);
                    }
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public void notifyItemChangedByPosition(ArrayList<QuestionItem> values, int position){
        this.values = values;
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public QuestionItem item;
        public TextView number;
        public ImageView background;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            number = view.findViewById(R.id.number);
            background = view.findViewById(R.id.background);
        }
    }
}
