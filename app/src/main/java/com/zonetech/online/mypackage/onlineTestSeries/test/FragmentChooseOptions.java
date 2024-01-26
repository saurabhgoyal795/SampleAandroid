package com.zonetech.online.mypackage.onlineTestSeries.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.plattysoft.leonids.ParticleSystem;
import com.plattysoft.leonids.modifiers.ScaleModifier;
import com.zonetech.online.R;
import com.zonetech.online.common.CASoundPlayer;
import com.zonetech.online.mypackage.onlineClass.MyPackageClassDetailsActivity;
import com.zonetech.online.mypackage.onlineClass.QueryActivity;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONObject;

public class FragmentChooseOptions extends Fragment {
    View rootView;
    QuestionItem item;
    JSONObject itemObj;
    int testType = TestActivity.TYPE_TEST;
    public interface OptionClickListener{
        void optionClicked(QuestionItem item);
        void markQuestion(QuestionItem item);
        void clearResponse(QuestionItem item);
    }
    OptionClickListener optionClickListener;
    private CASoundPlayer mSoundPlayer;
    private Bundle mSoundIds;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            optionClickListener = (OptionClickListener) context;
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_choose_options, container, false);
        Bundle bundle = getArguments();
        try {
            item = bundle.getParcelable("item");
            testType = bundle.getInt("testType", testType);
            itemObj = new JSONObject(item.data);
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
        ZTWebView questionText = rootView.findViewById(R.id.questionText);
        questionText.getSettings().setDefaultTextEncodingName("utf-8");
        questionText.getSettings().setJavaScriptEnabled(true);
        String question = itemObj.optString("QuestionEng");
        question = question.replaceAll("<img ", "<img style=\"max-width:100%\"");
        String questionHindi = itemObj.optString("QuestionHindi");
        questionHindi = questionHindi.replaceAll("<img ", "<img style=\"max-width:100%\"");
        question = question + questionHindi;

        String head1 = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
        String newHtml = head1 + question + "</body></html>";
        questionText.loadDataWithBaseURL(null, newHtml, "text/html", "UTF-8", null);

        setOptions(testType);

        if(TestActivity.TYPE_REVIEW == testType) {
            String quesSolution = itemObj.optString("QuesSolution");
            if (Utils.isValidString(quesSolution) && !quesSolution.equalsIgnoreCase("<p><br></p>")) {
                ZTWebView soluWeb = rootView.findViewById(R.id.quesSolution);
                soluWeb.setVisibility(View.VISIBLE);
                quesSolution = quesSolution.replaceAll("<img ", "<img style=\"max-width:100%\"");
                String soluHead = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
                String soluHtml = soluHead + quesSolution + "</body></html>";
                soluWeb.loadDataWithBaseURL(null, soluHtml, "text/html", "UTF-8", null);
                rootView.findViewById(R.id.quesSolutionHeading).setVisibility(View.VISIBLE);
            }
        }
        if(TestActivity.TYPE_QUIZ == testType || TestActivity.TYPE_REVIEW == testType || TestActivity.TYPE_REATTEMPT == testType){
            rootView.findViewById(R.id.reviewLayout).setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.markButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optionClickListener != null){
                    optionClickListener.markQuestion(item);
                }
            }
        });
        rootView.findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout optionLayout = rootView.findViewById(R.id.optionLayout);
                for (int j = 0; j < optionLayout.getChildCount(); j++) {
                    RelativeLayout child = (RelativeLayout) (optionLayout.getChildAt(j));
                    ImageView button = (ImageView) child.findViewById(R.id.radioButton);
                    if(!isAdded()){
                        return;
                    }
                    button.setColorFilter(ContextCompat.getColor(getActivity(), R.color.ca_blue));
                    button.setImageResource(R.drawable.baseline_radio_button_unchecked_black_24);
                    button.setAlpha(.7f);
                }
                if(optionClickListener != null){
                    optionClickListener.clearResponse(item);
                }
            }
        });
        loadSounds();
        rootView.findViewById(R.id.query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isAdded()){
                    return;
                }
                String title = "";
                if(getActivity() instanceof TestActivity){
                    title = ((TestActivity)getActivity()).currentTitle();
                }else{
                    title = ((TestSolutionActivity)getActivity()).currentTitle();
                }
                if(!isAdded()){
                    return;
                }
                Intent intent = new Intent(getActivity(), QueryActivity.class);
                intent.putExtra("id", itemObj.optInt("ExamQuesID"));
                intent.putExtra("title", title);
                intent.putExtra("queryType", QueryActivity.TEST_QUERY);
                startActivity(intent);
            }
        });
        return rootView;
    }


    private void setOptions(int testType){
        LinearLayout optionLayout = rootView.findViewById(R.id.optionLayout);
        optionLayout.removeAllViews();
        JSONArray options = itemObj.optJSONArray("Options");
        for (int i = 0 ; i < options.length() ; i++){
            JSONObject object = options.optJSONObject(i);
            View optionItem = LayoutInflater.from(optionLayout.getContext()).inflate(R.layout.option_item, optionLayout, false);
            ImageView radioButton = optionItem.findViewById(R.id.radioButton);
            ZTWebView optionText = optionItem.findViewById(R.id.optionText);
            String option = object.optString("Options");
            if(Utils.isValidString(option) && !option.equalsIgnoreCase("<p><br></p>")){
                option = option.replaceAll("<p>", " ");
                option = option.replaceAll("<\\/p>", " ");
                option = option.trim();
                option = option.replaceAll("<img ", "<img style=\"max-width:100%\"");
            }else{
                option = "";
            }
            String optionHindi = object.optString("OptionHindi");

            if(Utils.isValidString(optionHindi) && !optionHindi.equalsIgnoreCase("<p><br></p>")) {
                optionHindi = optionHindi.replaceAll("<p>", " ");
                optionHindi = optionHindi.replaceAll("<\\/p>", " ");
                optionHindi = optionHindi.trim();
                optionHindi = optionHindi.replaceAll("<img ", "<img style=\"max-width:100%\"");
                option = option  +"<br>" + optionHindi;
            }
            String head1 = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
            String newHtml = head1 + option + "</body></html>";

            optionText.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    radioButton.setVisibility(View.VISIBLE);
                }
            });
            optionText.loadDataWithBaseURL(null, newHtml, "text/html", "UTF-8", null);

            final int quesOptionId = object.optInt("QuesOptionID");
            int finalI = i;
            final boolean isRight = object.optBoolean("IsRight");
            final boolean isSelected = object.optBoolean("IsSelected");
            TextView answerStatus = optionItem.findViewById(R.id.answerStatus);
            if(TestActivity.TYPE_REVIEW == testType){
                optionItem.setEnabled(false);
                optionItem.setAlpha(.7f);
                if(isSelected){
                    radioButton.setImageResource(R.drawable.baseline_radio_button_checked_black_24);
                    if(isRight){
                        optionItem.setBackgroundResource(R.drawable.button_round_green);
                        radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.ca_green));
                        answerStatus.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
                    }else{
                        optionItem.setBackgroundResource(R.drawable.button_round_red);
                        radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                        answerStatus.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
                    }
                    answerStatus.setVisibility(View.VISIBLE);
                }else{
                    if(isRight){
                        answerStatus.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ca_green));
                        radioButton.setImageResource(R.drawable.baseline_radio_button_checked_black_24);
                        optionItem.setBackgroundResource(R.drawable.button_round_green);
                        radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.ca_green));
                        answerStatus.setText("Right Answer");
                        answerStatus.setVisibility(View.VISIBLE);
                    }
                }
            }else{
                if((item.quesOptionID == quesOptionId || isSelected) && TestActivity.TYPE_REATTEMPT != testType){
                    radioButton.setImageResource(R.drawable.baseline_radio_button_checked_black_24);
                    radioButton.findViewById(R.id.radioButton).setAlpha(1f);
                    if(TestActivity.TYPE_QUIZ == testType){
                        if(isRight){
                            radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.ca_green));
                        }else{
                            radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                        }
                    }else {
                        radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                    }
                }
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        radioButton.setImageResource(R.drawable.baseline_radio_button_checked_black_24);
                        radioButton.findViewById(R.id.radioButton).setAlpha(1f);
                        if(TestActivity.TYPE_QUIZ == testType || TestActivity.TYPE_REATTEMPT == testType) {
                            if (isRight) {
                                playRightSound();
                                optionItem.setBackgroundResource(R.drawable.button_round_green);
                                radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.ca_green));
                                confettiAnimation();
                            }else{
                                playwrongSound();
                                optionItem.setBackgroundResource(R.drawable.button_round_red);
                                radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.red));
                            }
                            for (int i = 0 ; i < optionLayout.getChildCount(); i++){
                                optionLayout.getChildAt(i).setEnabled(false);
                                optionLayout.getChildAt(i).setAlpha(.7f);
                                ZTWebView optionText = optionLayout.getChildAt(i).findViewById(R.id.optionText);
                                ImageView radioButton = optionLayout.getChildAt(i).findViewById(R.id.radioButton);
                                optionText.setOnTouchListener(null);
                                radioButton.setOnClickListener(null);
                            }

                            try {
                                if (TestActivity.TYPE_REATTEMPT == testType) {
                                    JSONArray options = itemObj.optJSONArray("Options");
                                    for (int k = 0; k < options.length(); k++) {
                                        if (k == finalI) {
                                            options.optJSONObject(k).put("IsSelected", true);
                                        } else {
                                            options.optJSONObject(k).put("IsSelected", false);
                                        }
                                    }
                                }
                            }catch (Exception e){
                                if(Utils.isDebugModeOn){
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            radioButton.setColorFilter(ContextCompat.getColor(getActivity(), R.color.orange));
                        }
                        item.isAttempted = true;
                        item.quesOptionID = quesOptionId;
                        for (int j = 0; j < optionLayout.getChildCount(); j++) {
                            if (finalI == j) {
                                continue;
                            }
                            RelativeLayout child = (RelativeLayout) (optionLayout.getChildAt(j));
                            ImageView button = child.findViewById(R.id.radioButton);
                            button.setColorFilter(ContextCompat.getColor(getActivity(), R.color.ca_blue));
                            button.setImageResource(R.drawable.baseline_radio_button_unchecked_black_24);
                            button.setAlpha(.7f);
                        }
                        if(TestActivity.TYPE_REATTEMPT == testType){
                            setOptions(TestActivity.TYPE_REVIEW);
                        }else{
                            if (optionClickListener != null) {
                                optionClickListener.optionClicked(item);
                            }
                        }
                    }
                });
                optionText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_UP){
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                                radioButton.callOnClick();
                            } else {
                                radioButton.performClick();
                            }
                        }
                        return false;
                    }
                });
            }
            optionLayout.addView(optionItem);
        }
    }
    private void confettiAnimation(){
        float density = Utils.getMetrics(getActivity()).density;
        try {
            Bitmap starBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.yellow_star_new);
            if(starBitmap == null){
                return;
            }
            Bitmap resultBitmap = Bitmap.createBitmap(starBitmap, 0, 0,
                    starBitmap.getWidth()-1, starBitmap.getHeight()-1);
            if(resultBitmap == null){
                return;
            }
            resultBitmap = Bitmap.createScaledBitmap(
                    resultBitmap, (int)(15*density), (int)(15*density), false);
            if(resultBitmap == null){
                return;
            }
            Paint paint = new Paint();
            Canvas canvas = new Canvas(resultBitmap);
            canvas.drawBitmap(resultBitmap, 0, 0, paint);
            new ParticleSystem(getActivity(), 50, resultBitmap, R.id.particleContainer)
                    .setSpeedRange(0.2f, 0.5f)
                    .setRotationSpeedRange(0,360)
                    .setScaleRange(.7f,1)
                    .setFadeOut(1000,new AccelerateInterpolator())
                    .addModifier(new ScaleModifier(1,0f,1500,2000))
                    .oneShot(rootView.findViewById(R.id.optionLayout), 50);
        }catch (Exception e){}
    }
    private void loadSounds() {
        mSoundPlayer = new CASoundPlayer(getActivity(), 3);
        mSoundIds = new Bundle();
        mSoundIds.putInt("right", mSoundPlayer.load(R.raw.quiz_right, 1));
        mSoundIds.putInt("wrong", mSoundPlayer.load(R.raw.quiz_wrong, 1));
    }

    private void playRightSound(){
        mSoundPlayer.play(mSoundIds.getInt("right"));
    }
    private void playwrongSound(){
        mSoundPlayer.play(mSoundIds.getInt("wrong"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mSoundIds.clear();
            mSoundPlayer.release();
        } catch (Exception e) {}
    }
}
