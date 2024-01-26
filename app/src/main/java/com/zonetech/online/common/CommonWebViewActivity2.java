package com.zonetech.online.common;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;


public class CommonWebViewActivity2 extends ZTAppCompatActivity {

	private RelativeLayout loadingLayout;
	public WebView myWebView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_webview2);

		loadingLayout = findViewById(R.id.loadingLayout);
		myWebView = findViewById(R.id.myWebView);
		String url = "";
		findViewById(R.id.pullToRefreshInLoading)
				.post(new Runnable() {
					@Override
					public void run() {
						((SwipeRefreshLayout) findViewById(R.id.pullToRefreshInLoading))
								.setRefreshing(true);
					}
				});

		Bundle extra = getIntent().getExtras();
		if (extra != null) {
			url = extra.getString("url");
		}
		myWebView.loadUrl(url);
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

		try {
			myWebView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
		}catch (Exception e){
			if(Utils.isDebugModeOn){
				e.printStackTrace();
			}
		}

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		myWebView.setWebChromeClient(new WebChromeClient(){});
		myWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				loadingLayout.setVisibility(View.GONE);
				myWebView.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (myWebView.canGoBack()) {
			myWebView.goBack();
		} else {
			super.onBackPressed();
			overridePendingTransition(R.anim.left_in,
					R.anim.right_out);
		}

	}
}

