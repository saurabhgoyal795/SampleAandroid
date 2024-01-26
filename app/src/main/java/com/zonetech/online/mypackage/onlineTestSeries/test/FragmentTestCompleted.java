package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;
import com.zonetech.online.R;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentTestCompleted extends Fragment {
    View rootView;
    int studentExamID;
    int examDuration;
    int studentRank;
    String studentMarks;
    String studentDuration;
    double totalMarks;
    double obtainMarks;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_test_completed, container, false);
        Bundle bundle = getArguments();
        studentExamID = bundle.getInt("studentExamID");
        examDuration = bundle.getInt("examDuration");
        ((TextView)rootView.findViewById(R.id.studentName)).setText(Utils.getName(getActivity()));
        ((TextView)rootView.findViewById(R.id.studentCode)).setText(Utils.getStudentCode(getActivity()));
        getExamResult();
        return rootView;
    }

    private void getExamResult(){
        TestUtils.examResult(getContext(), studentExamID, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                response = response.optJSONObject("Body");
                response = response.optJSONObject("ExamDetails");
                String resultDate = response.optString("ResultDate");
                if(Utils.isValidString(resultDate)){
                    ((TextView)rootView.findViewById(R.id.resultDate)).setText("Hello dear\nYou have Successfully completed this test. Your exam has been submitted. The result of this Exam will be avaialbe on\n"+resultDate);
                    rootView.findViewById(R.id.resultDateLayout).setVisibility(View.VISIBLE);
                }else{
                    setViews(response);
                    rootView.findViewById(R.id.mainLayout).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.progressDivider).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
                    getTopperList();
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }
    private void setViews(JSONObject data){
        studentRank = data.optInt("StudentRank");
        ((TextView)rootView.findViewById(R.id.rank)).setText(studentRank+"");
        ((TextView)rootView.findViewById(R.id.totalRank)).setText(data.optInt("StudentCount")+"");
        totalMarks = data.optDouble("TotalMarks");
        obtainMarks = data.optDouble("ObtainMarks");
        if(obtainMarks % 1 == 0){
            studentMarks = Math.round(obtainMarks)+"";
            ((TextView)rootView.findViewById(R.id.obtainMarks)).setText(Math.round(obtainMarks)+"");
        }else{
            studentMarks = obtainMarks+"";
            ((TextView)rootView.findViewById(R.id.obtainMarks)).setText(obtainMarks+"");
        }
        if(totalMarks % 1 == 0){
            ((TextView)rootView.findViewById(R.id.totalMarks)).setText(Math.round(totalMarks)+"");
        }else{
            ((TextView)rootView.findViewById(R.id.totalMarks)).setText(totalMarks+"");
        }

        int rightQues = data.optInt("RightQues");
        int wrongQues = data.optInt("WrongQues");
        int totalQues = data.optInt("TotalQuestion");
        int totalAttemptedQues = rightQues + wrongQues;
        int unAttemptedQues = totalQues - totalAttemptedQues;

        float per = (rightQues*1f * 100)/(totalAttemptedQues*1f);
        if(per % 1 == 0){
            ((TextView)rootView.findViewById(R.id.accuracyResult)).setText(Math.round(per)+"%");
        }else{
            ((TextView)rootView.findViewById(R.id.accuracyResult)).setText(String.format(Locale.US, "%.2f", per)+"%");
        }

        ProgressBar accuracy = rootView.findViewById(R.id.accuracy);
        accuracy.setMax(totalAttemptedQues);
        accuracy.setProgress(rightQues);

        PieChart pieChart = (PieChart) rootView.findViewById(R.id.pie_chart);
        List<ChartData> chartData = new ArrayList<>();
        float unAttemptedQuesPer = ((unAttemptedQues*1f)/(totalQues*1f))*100;
        float rightQuesPer = ((rightQues*1f)/(totalQues*1f))*100;
        float wrongQuesPer = ((wrongQues*1f)/(totalQues*1f))*100;

        chartData.add(new ChartData("", unAttemptedQuesPer, Color.WHITE, Color.parseColor("#CF5300")));
        if(!isAdded()){
            return;
        }
        chartData.add(new ChartData("", rightQuesPer, Color.WHITE, ContextCompat.getColor(getActivity(), android.R.color.holo_green_light)));
        if(!isAdded()){
            return;
        }
        chartData.add(new ChartData("", wrongQuesPer, Color.WHITE, ContextCompat.getColor(getActivity(), R.color.red)));
        pieChart.setChartData(chartData);
        ((TextView)rootView.findViewById(R.id.totalQuestions)).setText(String.format(getString(R.string.total_questions), totalQues));
        ((TextView)rootView.findViewById(R.id.correct)).setText("CORRECT "+rightQues);
        ((TextView)rootView.findViewById(R.id.inCorrect)).setText("INCORRECT "+wrongQues);
        ((TextView)rootView.findViewById(R.id.unanswered)).setText("UNANSWERED "+unAttemptedQues);

        int lastAttempTime = data.optInt("LastAttempTime");
        ((TextView)rootView.findViewById(R.id.timeTakenResult)).setText(lastAttempTime+" m");
        examDuration = data.optInt("ExamDuration", examDuration);
        if(examDuration > 0){
            ((TextView)rootView.findViewById(R.id.totalTime)).setText(examDuration+" m");
            rootView.findViewById(R.id.timeDivider).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.totalTime).setVisibility(View.VISIBLE);
        }

        rootView.findViewById(R.id.solution).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.solution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAdded()){
                    return;
                }
                Intent intent = new Intent(getActivity(), TestSolutionActivity.class);
                intent.putExtra("studentExamID", studentExamID);
                if(!isAdded()){
                    return;
                }
                intent.putExtra("title", getActivity().getTitle());
                startActivity(intent);
            }
        });
    }

    private void getTopperList(){
        if(!isAdded()){
            return;
        }
        TestUtils.getTopperList(getActivity(), studentExamID, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                try {
                    JSONArray list = response.optJSONArray("Body");
                    setTopperList(list);
                }catch (Exception e){
                    if(Utils.isDebugModeOn){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void error(String error) {

            }
        });
    }

    private void setTopperList(JSONArray values){
        if(!isAdded()){
            return;
        }
        if(values != null && values.length()>0){
            rootView.findViewById(R.id.topperListLayout).setVisibility(View.VISIBLE);
            RecyclerView topperList = rootView.findViewById(R.id.topperList);
            if(!isAdded()){
                return;
            }
            topperList.setLayoutManager(new LinearLayoutManager(getActivity()));
            if(!isAdded()){
                return;
            }
            topperList.setAdapter(new TopperListAdapter(getActivity(), values));
        }
        if(!isAdded()){
            return;
        }
        JSONObject topper = values.optJSONObject(0);
        double topperMarks = topper.optDouble("RightMarks") - topper.optDouble("WrongMark");
        View bar1 = rootView.findViewById(R.id.bar1);
        TextView score1 = rootView.findViewById(R.id.score1);
        View bar2 = rootView.findViewById(R.id.bar2);
        TextView score2 = rootView.findViewById(R.id.score2);

        View color1 = rootView.findViewById(R.id.color1);
        View color2 = rootView.findViewById(R.id.color2);
        TextView name1 = rootView.findViewById(R.id.name1);
        TextView name2 = rootView.findViewById(R.id.name2);
        if(!isAdded()){
            return;
        }
        name1.setText(Utils.getName(getActivity()));
        name2.setText(topper.optString("StudentName"));
        score1.setText(String.format(Locale.US, "%.2f", (float)obtainMarks));
        score2.setText(String.format(Locale.US, "%.2f", (float)topperMarks));
        if(!isAdded()){
            return;
        }
        if(obtainMarks > topperMarks){
            if(!isAdded()){
                return;
            }
            color1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
            if(!isAdded()){
                return;
            }
            color2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
            if(!isAdded()){
                return;
            }
            bar1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
            if(!isAdded()){
                return;
            }
            bar2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
        }else if(obtainMarks < topperMarks){
            if(!isAdded()){
                return;
            }
            color1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
            if(!isAdded()){
                return;
            }
            color2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
            if(!isAdded()){
                return;
            }
            bar1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
            if(!isAdded()){
                return;
            }
            bar2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
        }else{
            if(!isAdded()){
                return;
            }
            color1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
            if(!isAdded()){
                return;
            }
            color2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
            if(!isAdded()){
                return;
            }
            bar1.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
            if(!isAdded()){
                return;
            }
            bar2.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
        }
        if(!isAdded()){
            return;
        }
        DisplayMetrics metric = Utils.getMetrics(getActivity());
        double totalHeight = 200 * metric.density;
        int barHeight = (int)((obtainMarks * totalHeight)/totalMarks);
        int topperBarHeight = (int)((topperMarks * totalHeight)/totalMarks);
        bar1.getLayoutParams().height = barHeight;
        bar2.getLayoutParams().height = topperBarHeight;
        LinearLayout yAxis = rootView.findViewById(R.id.yAxis);
        long totalYAxis = Math.round(totalMarks);
        long blockValue = totalYAxis / 5;
        for (int i = yAxis.getChildCount() ; i > 0  ; i--){
            TextView child = (TextView)yAxis.getChildAt(yAxis.getChildCount() - i);
            child.setText((blockValue * i)+"");
        }
        rootView.findViewById(R.id.compareLayout).setVisibility(View.VISIBLE);
    }
}
