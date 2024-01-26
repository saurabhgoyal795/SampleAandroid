package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestSeriesDetailsActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestSeriesDetailsFragment;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.publication.FragmentPublicationDetails;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TestActivity extends ZTAppCompatActivity implements View.OnClickListener, FragmentChooseOptions.OptionClickListener , QuestionsListAdapter.ClickListener{
    public static final int TYPE_TEST = 0;
    public static final int TYPE_QUIZ = 1;
    public static final int TYPE_REVIEW = 2;
    public static final int TYPE_REATTEMPT = 3;


    public enum leftButtonState {
        CLOSE,
        PREVIOUS,
        INSTRUCTIONS,
        CLOSE_SUMIT
    }

    public enum rightButtonState {
        RESUME,
        START,
        NEXT,
        SUBMIT,
        RESULT
    }

    private String leftState = leftButtonState.CLOSE.toString();
    private String righState = rightButtonState.START.toString();
    private int examId;
    private JSONObject itemObj;
    private String instructionsText;
    private ArrayList<HashMap<String, Object>> sectionList;
    private int currentPosition;
    private HashMap<Integer, HashMap<String, Object>> responseData;
    private ArrayList<Integer> attemptedQuestionList = new ArrayList<>();
    private int examDuration;
    private int lastAttempTime;
    private CountDownTimer countDownTimer;
    private long passedTime;
    private long questionStartTime;
    private int studentExamId;
    private int testType = TYPE_TEST;
    public ArrayList<String> titleList;
    private RelativeLayout questionLayout;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private FragmentTest mCurrentFragment;
    public int currentSection;
    private boolean isTestSubmitted;
    private LinearLayout questionSectionLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        viewPager = findViewById(R.id.viewPager);
        questionLayout = findViewById(R.id.questionLayout);
        questionSectionLayout = findViewById(R.id.questionSectionLayout);
        try {
            Bundle bundle = getIntent().getExtras();
            itemObj = new JSONObject(bundle.getString("item"));
            String title = itemObj.optString("ExamName");
            setTitle(title);
            examId = itemObj.optInt("ExamID");
            testType = bundle.getInt("testType", testType);
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
        getExamDetails();
        findViewById(R.id.leftButton).setOnClickListener(this);
        findViewById(R.id.rightButton).setOnClickListener(this);
        findViewById(R.id.progressBar).setOnClickListener(this);

    }

    private void showInstructionScreen() {
        findViewById(R.id.fragmentLayout).setVisibility(View.VISIBLE);
        questionLayout.setVisibility(View.GONE);
        findViewById(R.id.topHeader).setVisibility(View.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        TestInstructions instructions = new TestInstructions();
        Bundle bundle = new Bundle();
        bundle.putString("instructions", instructionsText);
        instructions.setArguments(bundle);
        fragmentTransaction.replace(R.id.testSlideContainer, instructions).commitAllowingStateLoss();
    }
    FragmentSubmit fragmentSubmit;
    private void submitTest() {
        try{
            stopQuestionTimer();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentSubmit = new FragmentSubmit();
            Bundle bundle = new Bundle();
            bundle.putString("title", itemObj.optString("ExamName"));
            fragmentSubmit.setArguments(bundle);
            fragmentTransaction.replace(R.id.testSlideContainer, fragmentSubmit).commitAllowingStateLoss();
            setSubmitHeader();
            submitTestResponse();
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
            submitExam(1);
        }
    }
    boolean isSaveResponseFailed = false;
    public void submitTestResponse(){
        isSaveResponseFailed = false;
        if(responseData != null && responseData.size() > 0){
            Set set = responseData.entrySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                if(isSaveResponseFailed){
                    isSaveResponseFailed = false;
                    break;
                }
                Map.Entry mEntry = (Map.Entry) iterator.next();
                int quesId = (Integer) mEntry.getKey();
                HashMap<String, Object> item = (HashMap<String, Object>)mEntry.getValue();
                if(item != null){
                    int quesOptionId = (Integer) item.get("quesOptionID");
                    long quesTime = (Long)item.get("quesTime");
                    saveAnswer(quesId, quesOptionId, quesTime,true);
                }
            }
        }else{
            submitExam(1);
        }
    }

    private void setSubmitHeader(){
        ((TextView)findViewById(R.id.attempted)).setText(String.format(getString(R.string.attempted), attemptedQuestionList.size()));
        ((TextView)findViewById(R.id.missed)).setText(String.format(getString(R.string.missed), itemObj.optInt("TotalQuestion") - attemptedQuestionList.size()));
        double timeSpent = (passedTime * 1f) /(60 * 1000* 1f);
        double perQuestionTimeSpent = 0;
        if(attemptedQuestionList.size() > 0) {
            perQuestionTimeSpent = timeSpent / attemptedQuestionList.size();
        }
        ((TextView)findViewById(R.id.perQuestionTime)).setText(String.format(getString(R.string.per_question_time), String.format(Locale.US, "%.2f", perQuestionTimeSpent)));
        ((TextView)findViewById(R.id.timeSpent)).setText(String.format(getString(R.string.time_spent),  String.format(Locale.US, "%.2f", timeSpent)));
        findViewById(R.id.headerOne).setVisibility(View.GONE);
        findViewById(R.id.headerTwo).setVisibility(View.VISIBLE);
        findViewById(R.id.topHeader).setVisibility(View.VISIBLE);
        questionLayout.setVisibility(View.GONE);
        findViewById(R.id.fragmentLayout).setVisibility(View.VISIBLE);

    }

    private void submitExam(int isSubmited){
        int minutes = (int)(passedTime*1f/(60*1000f)) + lastAttempTime;
        TestUtils.submitExam(getApplicationContext(), studentExamId, isSubmited, minutes, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(TestActivity.this)){
                    return;
                }
                if(isSubmited == 1) {
                    fragmentSubmit.submitted();
                    leftState = leftButtonState.CLOSE_SUMIT.toString();
                    righState = rightButtonState.RESULT.toString();
                    isTestSubmitted = true;
                    defaultMenu.getItem(0).setVisible(false);
                    setButtonText();
                }
            }

            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startTest() {
        setViewPager();
        findViewById(R.id.topHeader).setVisibility(View.GONE);
        findViewById(R.id.fragmentLayout).setVisibility(View.GONE);
        questionLayout.setVisibility(View.VISIBLE);
        responseData = new HashMap<>();
        attemptedQuestionList = new ArrayList<>();
        startQuestionTimer();
        setButtonUi();
        defaultMenu.getItem(0).setVisible(true);
    }

    private void getExamDetails() {
        TestUtils.examDetails(this, examId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(TestActivity.this)){
                    return;
                }
                response = response.optJSONObject("Body");
                setHeaderOneViews(response);
            }

            @Override
            public void error(String error) {

            }
        });
    }

    private void setHeaderOneViews(JSONObject data) {
        try {
            lastAttempTime = data.optInt("LastAttempTime");
            data.put("ExamDuration", data.optInt("ExamDuration") - lastAttempTime);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        examDuration = data.optInt("ExamDuration");
        instructionsText = data.optString("Instructions");
        ((TextView) findViewById(R.id.totalMarks)).setText(data.optInt("TotalMarks") + "");
        ((TextView) findViewById(R.id.examDuration)).setText(String.format(getString(R.string.exam_duration), data.optInt("ExamDuration")));
        ((TextView) findViewById(R.id.totalQuestions)).setText(data.optInt("TotalQuestion") + "");
        ((TextView) findViewById(R.id.negativeMarks)).setText(String.format(getString(R.string.negativeMarks), data.optDouble("NegMarkFormula")+""));
        showInstructionScreen();
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    private void getExamQuestions() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        TestUtils.examQuestions(this, examId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(TestActivity.this)){
                    return;
                }
                response = response.optJSONObject("Body");
                JSONArray subjectArray = response.optJSONArray("SubjectQuestions");
                sectionList = new ArrayList<>();
                titleList = new ArrayList<>();
                for (int i = 0; i < subjectArray.length(); i++) {
                    JSONObject subjectItem = subjectArray.optJSONObject(i);
                    String subjectName = subjectItem.optString("SubjectName");
                    int subjectId = subjectItem.optInt("SubjectID");
                    JSONArray questionArray = subjectItem.optJSONArray("Questions");
                    ArrayList<QuestionItem> questionList = new ArrayList<>();
                    for (int j = 0; j < questionArray.length(); j++) {
                        QuestionItem questionItem = new QuestionItem();
                        JSONObject questionObject = questionArray.optJSONObject(j);
                        questionItem.data = questionObject.toString();
                        questionItem.subjectId = questionObject.optInt("SubjectID");
                        questionItem.questionId = questionObject.optInt("QuestionID");
                        questionItem.examQuesId = questionObject.optInt("ExamQuesID");
                        questionItem.isAttempted = questionItem.isVisited = questionObject.optBoolean("IsAttempted");
                        questionList.add(questionItem);
                        if(questionItem.isAttempted){
                            currentPosition = j;
                        }
                    }
                    HashMap<String, Object> mapItem = new HashMap<>();
                    mapItem.put("name", subjectName);
                    mapItem.put("list", questionList);
                    mapItem.put("subjectId", subjectId);
                    sectionList.add(mapItem);
                    titleList.add(subjectName);
                }
                startTest();
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startExam(){
        TestUtils.startExam(this, examId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(TestActivity.this)){
                    return;
                }
                response = response.optJSONObject("Body");
                studentExamId = response.optInt("StudentExamID");
                getExamQuestions();
            }

            @Override
            public void error(String error) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() ==  R.id.leftButton) {
            if (leftState.equalsIgnoreCase(leftButtonState.PREVIOUS.toString())) {
                if (mCurrentFragment != null) {
                    mCurrentFragment.showQuestion("previous");
                }
            } else if (leftState.equalsIgnoreCase(leftButtonState.INSTRUCTIONS.toString())) {
                leftState = leftButtonState.CLOSE.toString();
                righState = rightButtonState.RESUME.toString();
                setButtonText();
                showInstructionScreen();
            } else {
                onBackPressed();
            }
        }else if (v.getId() == R.id.rightButton) {
            if (righState.equalsIgnoreCase(rightButtonState.RESULT.toString())) {
                showResult();
            } else if (righState.equalsIgnoreCase(rightButtonState.SUBMIT.toString())) {
                showConfirmDialog(true);
            } else if (righState.equalsIgnoreCase(rightButtonState.START.toString())
                    || righState.equalsIgnoreCase(rightButtonState.RESUME.toString())) {
                if (sectionList == null || sectionList.size() == 0) {
                    startExam();
                } else {
                    startTest();
                }
            } else {
                if (mCurrentFragment != null) {
                    mCurrentFragment.showQuestion("next");
                }
            }
    }
    }

    private void startQuestionTimer() {
        countDownTimer = new CountDownTimer(examDuration*60*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                passedTime = examDuration*60*1000 - millisUntilFinished;
                if(mCurrentFragment != null){
                    mCurrentFragment.setTimer(Utils.getTime(millisUntilFinished));
                }
            }

            @Override
            public void onFinish() {
                TimeOverUi();
            }
        };
        countDownTimer.start();
    }

    private void stopQuestionTimer() {
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }

    private void TimeOverUi(){
        Toast.makeText(getApplicationContext(), "Time over, Submitting test...", Toast.LENGTH_SHORT).show();
        righState = rightButtonState.SUBMIT.toString();
        leftState = leftButtonState.CLOSE.toString();
        setButtonText();
        submitTest();
    }
    private void setButtonUi() {
        if(currentSection == sectionList.size() - 1 && currentPosition == ((ArrayList<QuestionItem>)sectionList.get(currentSection).get("list")).size() - 1){
            righState = rightButtonState.SUBMIT.toString();
            leftState = leftButtonState.PREVIOUS.toString();
        }else if(currentSection == 0 && currentPosition == 0){
            leftState = leftButtonState.INSTRUCTIONS.toString();
            righState = rightButtonState.NEXT.toString();
        }else{
            leftState = leftButtonState.PREVIOUS.toString();
            righState = rightButtonState.NEXT.toString();
        }
        setButtonText();
    }

    private void setButtonText(){
        LinearLayout leftButton = findViewById(R.id.leftButton);
        LinearLayout rightButton = findViewById(R.id.rightButton);
        if (leftState.equalsIgnoreCase(leftButtonState.INSTRUCTIONS.toString())
                || leftState.equalsIgnoreCase(leftButtonState.CLOSE_SUMIT.toString())) {
            leftButton.getChildAt(0).setVisibility(View.GONE);
            if(leftState.equalsIgnoreCase(leftButtonState.CLOSE_SUMIT.toString())){
                ((TextView) leftButton.getChildAt(1)).setText(getString(R.string.close));
            }else{
                ((TextView) leftButton.getChildAt(1)).setText(getString(R.string.instruction));
            }
            leftButton.setBackgroundResource(R.drawable.button_round);
            ((TextView) leftButton.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.ca_blue));
            ((TextView) leftButton.getChildAt(1)).setAlpha(.87f);
        } else if (leftState.equalsIgnoreCase(leftButtonState.PREVIOUS.toString())) {
            leftButton.getChildAt(0).setVisibility(View.VISIBLE);
            leftButton.setBackgroundResource(R.drawable.button_round);
            ((TextView) leftButton.getChildAt(1)).setText(getString(R.string.previous));
            ((TextView) leftButton.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.ca_blue));
            ((TextView) leftButton.getChildAt(1)).setAlpha(.87f);
        } else {
            leftButton.getChildAt(0).setVisibility(View.GONE);
            leftButton.setBackgroundResource(R.drawable.button_red_rounded_4dp);
            ((TextView) leftButton.getChildAt(1)).setText(getString(R.string.close));
            ((TextView) leftButton.getChildAt(1)).setTextColor(ContextCompat.getColor(this, R.color.white));
            ((TextView) leftButton.getChildAt(1)).setAlpha(1f);
        }

        if (righState.equalsIgnoreCase(rightButtonState.SUBMIT.toString())
        || righState.equalsIgnoreCase(rightButtonState.RESULT.toString())) {
            rightButton.getChildAt(1).setVisibility(View.GONE);
            if(righState.equalsIgnoreCase(rightButtonState.RESULT.toString())){
                ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.result));
            }else {
                ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.submit));
            }
        } else if (righState.equalsIgnoreCase(rightButtonState.NEXT.toString())) {
            rightButton.getChildAt(1).setVisibility(View.VISIBLE);
            ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.next));
        } else  if(righState.equalsIgnoreCase(rightButtonState.RESUME.toString())){
            rightButton.getChildAt(1).setVisibility(View.GONE);
            ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.resume_exam));
        }else{
            rightButton.getChildAt(1).setVisibility(View.GONE);
            ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.start_exam));
        }
    }

    @Override
    public void optionClicked(QuestionItem item) {
        if(!attemptedQuestionList.contains(item.questionId)){
            attemptedQuestionList.add(item.questionId);
        }
        saveAnswer(item.questionId, item.quesOptionID, Calendar.getInstance().getTime().getTime() - questionStartTime, false);
        if(mCurrentFragment != null){
            mCurrentFragment.optionClicked(item);
        }
    }

    @Override
    public void markQuestion(QuestionItem item) {
        if(mCurrentFragment != null){
            mCurrentFragment.markQuestion(item);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            findViewById(R.id.rightButton).callOnClick();
        } else {
            findViewById(R.id.rightButton).performClick();
        }
    }

    @Override
    public void clearResponse(QuestionItem item) {
        if(mCurrentFragment != null){
            mCurrentFragment.clearResponse(item);
        }
    }

    private void saveAnswer(int questionId, int quesOptionID, long quesTime,boolean isSubmitted){
        HashMap<String, Object> item = new HashMap<>();
        item.put("quesOptionID", quesOptionID);
        item.put("quesTime", quesTime);
        responseData.put(questionId, item);
        TestUtils.saveAnswer(this, studentExamId, quesOptionID, questionId, quesTime, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                try{
                    if(!isSubmitted){
                        if((Integer) responseData.get(questionId).get("quesOptionID") == quesOptionID) {
                            responseData.remove(questionId);
                        }
                    }
                }catch (Exception e){}

                if(isSubmitted && responseData.size() == 0){
                    submitExam(1);
                }
            }

            @Override
            public void error(String error) {
                if(fragmentSubmit != null){
                    fragmentSubmit.failedLayout();
                    isSaveResponseFailed = true;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopQuestionTimer();
    }

    private void showResult(){
        findViewById(R.id.topHeader).setVisibility(View.GONE);
        findViewById(R.id.bottomLayout).setVisibility(View.GONE);
        findViewById(R.id.bottomLayoutShadow).setVisibility(View.GONE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        FragmentTestCompleted testCompletedFragment = new FragmentTestCompleted();
        Bundle bundle = new Bundle();
        bundle.putInt("studentExamID", studentExamId);
        bundle.putInt("examDuration", examDuration);
        testCompletedFragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.testSlideContainer, testCompletedFragment).commitAllowingStateLoss();
    }
    Menu defaultMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test_menu, menu);
        defaultMenu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.more){
//                showMenuOptions(findViewById(R.id.more));
                if(findViewById(R.id.questionNumberLayout).getVisibility() == View.VISIBLE){
                    hideQuestionNumberLayout();
                }else {
                    showQuestionLayout();
                    Drawable drawable = item.getIcon();
                    PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_light), PorterDuff.Mode.MULTIPLY);
                    drawable.setColorFilter(colorFilter);
                }
                return true;
        }
        return (super.onOptionsItemSelected(item));
    }
    private void showMenuOptions(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.test_option_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.submit) {
                    showConfirmDialog(true);
                }
                return true;
            }
        });

        if (!Utils.isActivityDestroyed(this)) {
            popup.show();
        }
    }

    public int getTestType(){
        return testType;
    }

    public ArrayList<QuestionItem> getQuestionList(int position){
        if(sectionList != null){
            return (ArrayList<QuestionItem>)sectionList.get(position).get("list");
        }else{
            return new ArrayList<>();
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        @Override
        public Fragment getItem(int position) {
            Fragment testFragment = new FragmentTest();
            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            testFragment.setArguments(bundle);
            return testFragment;
        }
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mCurrentFragment = (FragmentTest) object;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        @Override
        public int getCount() {
            return titleList.size();
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentSection = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private void setViewPager(){
        if(sectionsPagerAdapter != null){
            sectionsPagerAdapter.notifyDataSetChanged();
        }else {
            sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(sectionsPagerAdapter);
            viewPager.addOnPageChangeListener(sectionsPagerAdapter);
            TabLayout tabs = findViewById(R.id.tabs);
            tabs.setupWithViewPager(viewPager);
        }
    }

    public void showNextSection(){
        viewPager.setCurrentItem(currentSection + 1);
    }
    public void showPreviousSection(){
        viewPager.setCurrentItem(currentSection - 1);
    }

    public void checkForButtonUi(int position){
        currentPosition = position;
        setButtonUi();
        questionStartTime = Calendar.getInstance().getTime().getTime();
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.questionNumberLayout).getVisibility() == View.VISIBLE){
            hideQuestionNumberLayout();
            return;
        }
        if(TestActivity.TYPE_QUIZ == testType && !isTestSubmitted){
            showConfirmDialog(false);
        }else{
            super.onBackPressed();
        }
    }

    AlertDialog dialog;
    private void showConfirmDialog(boolean isSubmit) {
        try {
            if(sectionList == null || sectionList.size() == 0){
                finish();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater li = LayoutInflater.from(this);
            View promptsView = li.inflate(R.layout.submit_popup, null);
            TextView questions = promptsView.findViewById(R.id.questions);
            TextView answered = promptsView.findViewById(R.id.answered);
            TextView notAnswered = promptsView.findViewById(R.id.notAnswered);
            TextView reviewed = promptsView.findViewById(R.id.reviewed);
            TextView notVisited = promptsView.findViewById(R.id.notVisited);
            TextView left = promptsView.findViewById(R.id.close);
            String[] status = questionStatus();
            questions.setText(status[0]);
            answered.setText(status[1]);
            notAnswered.setText(status[2]);
            reviewed.setText(status[3]);
            notVisited.setText(status[4]);
            if(isSubmit){
                left.setText("Cancel");
            }else {
                left.setText("Continue");
            }
            TextView right = promptsView.findViewById(R.id.submit);
            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    submitTest();
                }
            });
            builder.setView(promptsView);
            dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            if (!Utils.isActivityDestroyed(this))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    private String[] questionStatus(){
        int totalQuestions = itemObj.optInt("TotalQuestion");
        int reviewed = 0;
        int visited = 0;
        int answered = 0;
        int notAnswered = 0;
        int notVisited = 0;
        for (int i = 0 ; i < sectionList.size(); i++){
            ArrayList<QuestionItem> list  = getQuestionList(i);
            for (int j = 0 ; j < list.size() ; j++){
                if(list.get(j).isAttempted){
                    answered++;
                    visited++;
                }else if(list.get(j).isVisited){
                    visited++;
                }
                if(list.get(j).isReviewed){
                    reviewed++;
                }
            }
        }
        notAnswered = totalQuestions - answered;
        notVisited = totalQuestions - visited;
        return new String[]{totalQuestions+"", answered+"", notAnswered+"", reviewed+"", notVisited+""};
    }

    private void showQuestionNumberLayout(){
        if(findViewById(R.id.questionNumberLayout).getVisibility() == View.VISIBLE){
            return;
        }
        Animation animationIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in);
        animationIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.questionNumberLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.questionNumberLayout).setBackgroundColor(ContextCompat.getColor(TestActivity.this, R.color.transparent));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.questionNumberLayout).setBackgroundColor(Color.parseColor("#80000000"));
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.questionNumberLayout).startAnimation(animationIn);
    }

    private void hideQuestionNumberLayout(){
        if(findViewById(R.id.questionNumberLayout).getVisibility() == View.GONE){
            return;
        }
        Animation animationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.questionNumberLayout).setBackgroundColor(ContextCompat.getColor(TestActivity.this, R.color.transparent));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.questionNumberLayout).setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.questionNumberLayout).startAnimation(animationOut);
        Drawable drawable = defaultMenu.getItem(0).getIcon();
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.MULTIPLY);
        drawable.setColorFilter(colorFilter);
    }
    private void showQuestionLayout(){
        if(findViewById(R.id.questionNumberLayout).getVisibility() == View.VISIBLE){
            return;
        }
        questionSectionLayout.removeAllViews();
        for (int i = 0 ; i < sectionList.size() ; i++){
            View view = LayoutInflater.from(questionSectionLayout.getContext()).inflate(R.layout.test_question_list_item, questionSectionLayout, false);
            HashMap<String, Object> item = sectionList.get(i);
            String name = (String)item.get("name");
            ((TextView)view.findViewById(R.id.title)).setText(name);
            ArrayList<QuestionItem> list = (ArrayList<QuestionItem>)item.get("list");
            ((TextView)view.findViewById(R.id.questions)).setText(list.size()+"");
            int answered = 0;
            for (int j = 0 ; j < list.size() ; j++){
                QuestionItem listItem = list.get(j);
                if(listItem.isAttempted){
                    answered++;
                }
                listItem.sectionIndex = i;
            }
            RecyclerView questionList = view.findViewById(R.id.questionList);
            questionList.setLayoutManager(new GridLayoutManager(this, 4));
            questionList.setAdapter(new QuestionsListAdapter(this, list, this));
            ((TextView)view.findViewById(R.id.answered)).setText(answered+"");
            questionSectionLayout.addView(view);
            view.findViewById(R.id.expandButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(questionList.getVisibility() == View.VISIBLE){
                        questionList.setVisibility(View.GONE);
                        view.findViewById(R.id.expandButton).setRotation(90);
                    }else{
                        questionList.setVisibility(View.VISIBLE);
                        view.findViewById(R.id.expandButton).setRotation(270);
                    }
                }
            });
        }
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideQuestionNumberLayout();
            }
        });
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideQuestionNumberLayout();
                showConfirmDialog(true);
            }
        });
        findViewById(R.id.questionNumberLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideQuestionNumberLayout();
            }
        });
        findViewById(R.id.innerLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });
        showQuestionNumberLayout();
    }
    @Override
    public void questionClick(QuestionItem item) {
        hideQuestionNumberLayout();
        if(item.sectionIndex == currentSection){
            mCurrentFragment.questionClick(item);
        }else{
            viewPager.setCurrentItem(item.sectionIndex);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCurrentFragment.questionClick(item);
                }
            }, 300);
        }
    }

    public String currentTitle(){
        return titleList.get(currentSection)+"(Q:"+(currentPosition+1)+")";
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isTestSubmitted){
            submitExam(0);
        }
    }
}
