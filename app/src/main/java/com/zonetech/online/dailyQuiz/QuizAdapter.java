package com.zonetech.online.dailyQuiz;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestActivity;
import com.zonetech.online.mypackage.onlineTestSeries.test.TestResultActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuizAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    JSONArray values;
    Activity activity;
    public QuizAdapter(Activity activity, JSONArray values){
        this.values = values;
        this.activity = activity;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_daily_quiz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.item = values.optJSONObject(holder.getAdapterPosition());
        viewHolder.title.setText(viewHolder.item.optString("ExamName"));
        String fromDate = viewHolder.item.optString("FromDate");
        fromDate = fromDate.replaceFirst(" ", "#");
        fromDate = fromDate.replace(" ", "\n");
        fromDate = fromDate.replace("#", " ");
        viewHolder.date.setText(fromDate);
        viewHolder.totalQuestions.setText(viewHolder.item.optInt("TotalQuestion")+"");
        viewHolder.totalMarks.setText(viewHolder.item.optInt("TotalMarks")+"");
        viewHolder.duration.setText(viewHolder.item.optInt("ExamDuration")+" min");
        int examStatus = viewHolder.item.optInt("ExamStatus");

        String status = "";
        switch (examStatus){
            case 0:
                status = "Upcoming";
                viewHolder.date.setBackgroundResource(R.drawable.button_green_rounded);
                viewHolder.status.setTextColor(ContextCompat.getColor(activity, R.color.ca_green));
                break;
            case 1:
                status = "Active";
                viewHolder.date.setBackgroundResource(R.drawable.button_green_rounded);
                viewHolder.status.setTextColor(ContextCompat.getColor(activity, R.color.ca_green));
                break;
            case 2:
                status = "Partial";
                viewHolder.date.setBackgroundResource(R.drawable.button_yellow_rounded);
                viewHolder.status.setTextColor(ContextCompat.getColor(activity, R.color.ca_yellow));
                break;
            case 3:
                status = "Completed";
                viewHolder.date.setBackgroundResource(R.drawable.button_blue_rounded_10dp);
                viewHolder.status.setTextColor(ContextCompat.getColor(activity, R.color.blue));
                break;
            case 4:
                status = "Missed";
                viewHolder.date.setBackgroundResource(R.drawable.button_red_rounded_10dp);
                viewHolder.status.setTextColor(ContextCompat.getColor(activity, R.color.red));
                break;
        }
        viewHolder.status.setText(status);
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (examStatus){
                    case 1:
                    case 2:
                        Intent intent = new Intent(activity, TestActivity.class);
                        intent.putExtra("item", viewHolder.item.toString());
                        intent.putExtra("testType", TestActivity.TYPE_QUIZ);
                        activity.startActivity(intent);
                        break;
                    case 3:
                        Intent resultIntent = new Intent(activity, TestResultActivity.class);
                        resultIntent.putExtra("studentExamID", viewHolder.item.optInt("StudentExamID"));
                        resultIntent.putExtra("title", viewHolder.item.optString("ExamName"));
                        resultIntent.putExtra("examDuration", viewHolder.item.optInt("ExamDuration"));
                        activity.startActivity(resultIntent);
                        break;
                    case 4:
                        break;
                }
            }
        });
    }

    public void refreshValues(JSONArray values){
        this.values = values;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return values.length();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public JSONObject item;
        public TextView date;
        public TextView title;
        public TextView totalQuestions;
        public TextView totalMarks;
        public TextView duration;
        public TextView status;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            date = view.findViewById(R.id.date);
            title = view.findViewById(R.id.title);
            totalQuestions = view.findViewById(R.id.totalQuestions);
            totalMarks = view.findViewById(R.id.totalMarks);
            duration = view.findViewById(R.id.duration);
            status = view.findViewById(R.id.status);
        }
    }
}
