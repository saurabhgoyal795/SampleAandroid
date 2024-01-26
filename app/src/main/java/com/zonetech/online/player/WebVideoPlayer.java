package com.zonetech.online.player;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.views.ZTWebView;

public class WebVideoPlayer extends ZTAppCompatActivity {
    ZTWebView webView;
    String url = "https://onlinezonetech.in/Home/PlayVideo?videourl=%1$s&videotitle=%2$s";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_video_player);
        webView = findViewById(R.id.playerView);
        Bundle bundle = getIntent().getExtras();
        String videoId = bundle.getString("videoUrl");
        String title = bundle.getString("title");
        if(webView.getSettings() != null) {
//            webView.getSettings().setAppCacheEnabled(false);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
        }
        String videoUrl = String.format(url, videoId, title);
        webView.setWebChromeClient(new WebChromeClient(){});
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                setWebViewHeight();
            }
        });
        webView.loadUrl(videoUrl);
    }
    private void setWebViewHeight(){
        int height = webView.computeVerticalScrollRange();
        webView.getLayoutParams().height = height;
    }
}