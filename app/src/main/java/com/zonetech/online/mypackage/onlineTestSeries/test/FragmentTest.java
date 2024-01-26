package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentTest extends Fragment implements QuestionsListAdapter.ClickListener{
    private RecyclerView questionNumberList;
    private QuestionsListAdapter questionsListAdapter;
    private View rootView;
    private ArrayList<QuestionItem>questionList;
    private int currentPosition;
    public int testType = TestActivity.TYPE_TEST;
    private int position;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_test, container, false);
        questionNumberList = rootView.findViewById(R.id.questionNumberList);
        testType = getTestType();
        Bundle bundle = getArguments();
        position = bundle.getInt("position");
        questionList = getQuestionList();
        startTest();
        if(TestActivity.TYPE_REVIEW == testType){
            ((LinearLayout)rootView.findViewById(R.id.timeLeft).getParent()).setVisibility(View.INVISIBLE);
        }
        return rootView;
    }

    public void startTest() {
        if(!isAdded()){
            return;
        }
        questionsListAdapter = new QuestionsListAdapter(getActivity(), questionList, this);
        if(!isAdded()){
            return;
        }
        questionNumberList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        questionNumberList.setAdapter(questionsListAdapter);
        if(currentPosition < questionList.size()){
            QuestionItem item = questionList.get(currentPosition);
            startQuestion(item);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    questionNumberList.smoothScrollToPosition(currentPosition);
                }
            }, 1000);
        }
    }

    public void showQuestion(String action) {
        if("next".equalsIgnoreCase(action)){
            currentPosition = currentPosition + 1;
        }else{
            currentPosition = currentPosition - 1;
        }
        if(currentPosition >= questionList.size()){
            showNextSection();
        }else if(currentPosition < 0){
            showPreviousSection();
        }else {
            QuestionItem item = questionList.get(currentPosition);
            startQuestion(item);
        }
    }

    private void startQuestion(QuestionItem item){
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentChooseOptions fragmentChooseOptions = new FragmentChooseOptions();
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        bundle.putInt("testType", testType);
        fragmentChooseOptions.setArguments(bundle);
        fragmentTransaction.replace(R.id.questionConatiner, fragmentChooseOptions).commitAllowingStateLoss();
        if(TestActivity.TYPE_REVIEW != testType) {
            item.isVisited = true;
        }
        int position = questionList.indexOf(item);
        questionsListAdapter.notifyItemChangedByPosition(questionList, position);
        checkButtonUi();
        ((TextView)rootView.findViewById(R.id.questionCount)).setText("Q: "+(currentPosition+1)+"/"+questionList.size());
        try{
            JSONObject itemObj = new JSONObject(item.data);
            ((TextView)rootView.findViewById(R.id.pointWin)).setText("+"+itemObj.optString("RightMarks"));
            ((TextView)rootView.findViewById(R.id.negativePoint)).setText("-"+itemObj.optString("NegMarks"));
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        questionNumberList.scrollToPosition(currentPosition);
    }

    public void optionClicked(QuestionItem item) {
        int position = questionList.indexOf(item);
        if(position < 0 || position >= questionList.size()){
            return;
        }
        questionList.set(position, item);
        questionsListAdapter.notifyItemChangedByPosition(questionList, position);
    }

    public void markQuestion(QuestionItem item) {
        int position = questionList.indexOf(item);
        item.isReviewed = true;
        if(position < 0 || position >= questionList.size()){
            return;
        }
        questionList.set(position, item);
        questionsListAdapter.notifyItemChangedByPosition(questionList, position);
    }

    public void clearResponse(QuestionItem item) {
        int position = questionList.indexOf(item);
        item.isReviewed = false;
        item.isAttempted = false;
        item.quesOptionID = 0;
        if(position < 0 || position >= questionList.size()){
            return;
        }
        questionList.set(position, item);
        questionsListAdapter.notifyItemChangedByPosition(questionList, position);
    }

    @Override
    public void questionClick(QuestionItem item) {
        currentPosition = questionList.indexOf(item);
        startQuestion(questionList.get(currentPosition));
    }

    public void setTimer(String value){
        ((TextView)rootView.findViewById(R.id.timeLeft)).setText(value);
    }

    private int getTestType(){
        if(getActivity() instanceof TestSolutionActivity){
            return ((TestSolutionActivity)getActivity()).getTestType();
        }else{
            return ((TestActivity)getActivity()).getTestType();
        }
    }

    private ArrayList<QuestionItem> getQuestionList(){
        if(getActivity() instanceof TestSolutionActivity){
            return ((TestSolutionActivity)getActivity()).getQuestionList(position);
        }else{
            return ((TestActivity)getActivity()).getQuestionList(position);
        }
    }

    private void showNextSection(){
        if(getActivity() instanceof TestSolutionActivity){
            ((TestSolutionActivity)getActivity()).showNextSection();
        }else{
            ((TestActivity)getActivity()).showNextSection();
        }
    }
    private void showPreviousSection(){
        if(getActivity() instanceof TestSolutionActivity){
            ((TestSolutionActivity)getActivity()).showPreviousSection();
        }else{
            ((TestActivity)getActivity()).showPreviousSection();
        }
    }

    private void checkButtonUi(){
        if(getActivity() instanceof TestSolutionActivity){
            ((TestSolutionActivity) getActivity()).checkForButtonUi(currentPosition);
        }else{
            ((TestActivity) getActivity()).checkForButtonUi(currentPosition);
        }
    }
}
