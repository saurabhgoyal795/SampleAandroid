package com.zonetech.online.utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.barteksc.pdfviewer.PDFView;
import com.zonetech.online.R;
import com.zonetech.online.common.ZTAppCompatActivity;
import com.zonetech.online.mypackage.onlineClass.MyPackageClassDetailsActivity;
import com.zonetech.online.server.ServerApi;

import java.io.File;

public class PdfOpenActivity extends ZTAppCompatActivity {
    private PDFView pdfView;
    private String fileName;
    private String basePath;
    private String downloadPath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PdfOpener.init(this);
        setContentView(R.layout.activity_pdf);
        pdfView = findViewById(R.id.pdfView);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            fileName = bundle.getString("fileName");
            downloadPath = bundle.getString("downloadPath");
            basePath = bundle.getString("basePath");
        }
        if(new File(basePath+fileName).exists()){
            File file = new File(basePath, fileName);
            PdfOpener.showPdfFromFile(this, pdfView, file);
        }else{
            downloadPdf();
        }
        findViewById(R.id.tryAgain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isConnectedToInternet(getApplicationContext())){
                    findViewById(R.id.errorLayout).setVisibility(View.GONE);
                    downloadPdf();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.network_error_1), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void downloadPdf(){
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = downloadPath + fileName;
                Utils.downloadFile(url, basePath + fileName, new Utils.DownloadListener() {
                    @Override
                    public void startDownload(final int max) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ProgressBar)findViewById(R.id.downloadProgress)).setMax(max);
                            }
                        });
                    }

                    @Override
                    public void progress(final int value, final int max) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ProgressBar)findViewById(R.id.downloadProgress)).setMax(max);
                                ((ProgressBar)findViewById(R.id.downloadProgress)).setProgress(value);
                                float per = (value*100f)/max*1f;
                                ((TextView)findViewById(R.id.downloadPer)).setText(String.format(getString(R.string.downloading_progress), Math.round(per)+"%"));
                            }
                        });
                    }

                    @Override
                    public void finishDownload() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LocalBroadcastManager.getInstance(PdfOpenActivity.this).sendBroadcast(new Intent(MyPackageClassDetailsActivity.REFRESH_REQUEST));
                                ((TextView)findViewById(R.id.downloadPer)).setText("100%");
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                File file = new File(basePath, fileName);
                                PdfOpener.showPdfFromFile(PdfOpenActivity.this, pdfView, file);
                            }
                        });
                    }

                    @Override
                    public void failedDownload() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.progressBar).setVisibility(View.GONE);
                                findViewById(R.id.errorLayout).setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        }).start();



//        PdfOpener.downloadPdfFromInternet(this, pdfView, url, basePath, fileName, new PdfOpener.CompleteListener() {
//            @Override
//            public void success() {
//                findViewById(R.id.progressBar).setVisibility(View.GONE);
//            }
//
//            @Override
//            public void error(String error) {
//                ((TextView)findViewById(R.id.errorMsg)).setText(error);
//                findViewById(R.id.progressBar).setVisibility(View.GONE);
//                findViewById(R.id.errorLayout).setVisibility(View.VISIBLE);
//            }
//        });
    }
}
