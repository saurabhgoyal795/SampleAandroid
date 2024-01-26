package com.zonetech.online.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.zonetech.online.R;
import com.zonetech.online.server.ServerApi;
import com.zonetech.online.utils.PdfOpenActivity;
import com.zonetech.online.utils.Utils;


public class CommonWebViewActivity extends ZTAppCompatActivity {

	private RelativeLayout loadingLayout;
	public WebView myWebView;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_webview);

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


		myWebView.getSettings().setDomStorageEnabled(true);
		myWebView.getSettings().setSaveFormData(true);
		myWebView.getSettings().setAllowContentAccess(true);
		myWebView.getSettings().setAllowFileAccess(true);
		myWebView.getSettings().setAllowFileAccessFromFileURLs(true);
		myWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
		myWebView.getSettings().setSupportZoom(true);
		myWebView.setClickable(true);

		myWebView.setDownloadListener(new DownloadListener() {
			public void onDownloadStart(String url, String userAgent,
										String contentDisposition, String mimetype,
										long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

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

