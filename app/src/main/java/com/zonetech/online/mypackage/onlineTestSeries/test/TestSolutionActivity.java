package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineTestSeries.TestUtils;
import com.zonetech.online.utils.CompleteListener;
import com.zonetech.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TestSolutionActivity extends ZTAppCompatActivity implements View.OnClickListener, FragmentChooseOptions.OptionClickListener, QuestionsListAdapter.ClickListener{
    public enum leftButtonState {
        CLOSE,
        PREVIOUS
    }

    public enum rightButtonState {
        NEXT,
        CLOSE
    }

    private String leftState = leftButtonState.CLOSE.toString();
    private String righState = rightButtonState.NEXT.toString();
    private int examId;
    private ArrayList<HashMap<String, Object>> sectionList;
    private int currentPosition;
    private int examDuration;
    private int studentExamId;
    private ArrayList<String> titleList;
    private RelativeLayout questionLayout;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private FragmentTest mCurrentFragment;
    private int currentSection;
    private LinearLayout questionSectionLayout;
    private Switch reattemptButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_solution);
        viewPager = findViewById(R.id.viewPager);
        questionLayout = findViewById(R.id.questionLayout);
        questionSectionLayout = findViewById(R.id.questionSectionLayout);
        reattemptButton = findViewById(R.id.reattemptButton);
        try {
            Bundle bundle = getIntent().getExtras();
            String title = bundle.getString("title");
            setTitle(title);
            studentExamId = bundle.getInt("studentExamID");
        } catch (Exception e) {
            if (Utils.isDebugModeOn) {
                e.printStackTrace();
            }
        }
        findViewById(R.id.leftButton).setOnClickListener(this);
        findViewById(R.id.rightButton).setOnClickListener(this);
        findViewById(R.id.progressBar).setOnClickListener(this);
        getExamSoutions();
        reattemptButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mCurrentFragment != null){
                    mCurrentFragment.testType = isChecked ? TestActivity.TYPE_REATTEMPT : TestActivity.TYPE_REVIEW;
                    mCurrentFragment.startTest();
                }
            }
        });
    }

    boolean isTestStarted = false;
    private void startTest() {
        isTestStarted = true;
        questionLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.bottomLayoutShadow).setVisibility(View.VISIBLE);
        findViewById(R.id.bottomLayout).setVisibility(View.VISIBLE);
        setViewPager();
        setButtonUi();
        if(defaultMenu != null){
            defaultMenu.getItem(0).setVisible(true);
        }
    }

    Menu defaultMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test_menu, menu);
        defaultMenu = menu;
        if(isTestStarted && defaultMenu != null){
            defaultMenu.getItem(0).setVisible(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.more) {
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

    private void getExamSoutions() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        TestUtils.examSolutions(this, studentExamId, new CompleteListener() {
            @Override
            public void success(JSONObject response) {
                if(Utils.isActivityDestroyed(TestSolutionActivity.this)){
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
                        JSONArray options = questionObject.optJSONArray("Options");
                        for (int p = 0 ; p < options.length() ; p++){
                            if(options.optJSONObject(p).optBoolean("IsSelected")){
                                if(options.optJSONObject(p).optBoolean("IsRight")){
                                    questionItem.isAttempted = true;
                                }else{
                                    questionItem.isVisited = true;
                                }
                                break;
                            }
                        }
                        questionList.add(questionItem);
                        if(questionItem.isAttempted){
                            currentPosition = questionList.indexOf(questionItem);
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.leftButton) {
            if (leftState.equalsIgnoreCase(leftButtonState.PREVIOUS.toString())) {
                if (mCurrentFragment != null) {
                    mCurrentFragment.showQuestion("previous");
                }
            } else {
                onBackPressed();
            }
        } else if (v.getId() ==R.id.rightButton){
                if(righState.equalsIgnoreCase(rightButtonState.NEXT.toString())) {
                    if (mCurrentFragment != null) {
                        mCurrentFragment.showQuestion("next");
                    }
                } else {
                    onBackPressed();
                }
        }
    }

    private void setButtonUi() {
        if(currentSection == sectionList.size() - 1 && currentPosition == ((ArrayList<QuestionItem>)sectionList.get(currentSection).get("list")).size() - 1){
            righState = rightButtonState.CLOSE.toString();
            leftState = leftButtonState.PREVIOUS.toString();
        }else if(currentSection == 0 && currentPosition == 0){
            leftState = leftButtonState.CLOSE.toString();
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
        if (leftState.equalsIgnoreCase(leftButtonState.PREVIOUS.toString())) {
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

        if (righState.equalsIgnoreCase(rightButtonState.NEXT.toString())) {
            rightButton.getChildAt(1).setVisibility(View.VISIBLE);
            ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.next));
        } else{
            rightButton.getChildAt(1).setVisibility(View.GONE);
            ((TextView) rightButton.getChildAt(0)).setText(getString(R.string.close));
        }
    }

    @Override
    public void optionClicked(QuestionItem item) {
    }

    @Override
    public void markQuestion(QuestionItem item) {
    }

    @Override
    public void clearResponse(QuestionItem item) {
    }

    public int getTestType(){
        if(reattemptButton.isChecked()){
            return TestActivity.TYPE_REATTEMPT;
        }
        return TestActivity.TYPE_REVIEW;
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
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.questionNumberLayout).getVisibility() == View.VISIBLE){
            hideQuestionNumberLayout();
            return;
        }
        super.onBackPressed();
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
                findViewById(R.id.questionNumberLayout).setBackgroundColor(ContextCompat.getColor(TestSolutionActivity.this, R.color.transparent));
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
        if(findViewById(R.id.questionNumberLayout).getVisibility() == View.GONE || defaultMenu == null){
            return;
        }
        Animation animationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                findViewById(R.id.questionNumberLayout).setBackgroundColor(ContextCompat.getColor(TestSolutionActivity.this, R.color.transparent));
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
                if(listItem.isAttempted || listItem.isVisited){
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
}
