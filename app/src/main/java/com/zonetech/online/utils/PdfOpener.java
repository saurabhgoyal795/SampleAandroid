package com.zonetech.online.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;

import java.io.File;

public class PdfOpener {
    public static int PDF_SELECTION_CODE = 99;
    public interface CompleteListener{
        void success();
        void error(String error);
    }
    public static void init(Context context){
        PRDownloader.initialize(context);
    }
    public static void showPdfFromAssets(PDFView pdfView, String pdfName) {
        pdfView.fromAsset(pdfName)
                .password(null)
                .defaultPage(0)
                .onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                    }
                }).load();
    }
    public static void selectPdfFromStorage(Activity activity) {
        Intent browseStorage = new Intent(Intent.ACTION_GET_CONTENT);
        browseStorage.setType("application/pdf");
        browseStorage.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(browseStorage, "Select PDF"), PDF_SELECTION_CODE);
    }
    public static void showPdfFromUri(PDFView pdfView, Uri uri) {
        pdfView.fromUri(uri)
                .defaultPage(0)
                .spacing(10)
                .load();
    }
    public static void  downloadPdfFromInternet(final Activity activity, final  PDFView pdfView, String url,
                                                final String dirPath, final String fileName, final CompleteListener completeListener) {
        PRDownloader.download(url, dirPath, fileName).build().start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                File downloadedFile = new File(dirPath, fileName);
                if(completeListener != null){
                    completeListener.success();
                }
                showPdfFromFile(activity, pdfView, downloadedFile);
            }

            @Override
            public void onError(Error error) {
                if(completeListener != null){
                    completeListener.error(error.getServerErrorMessage());
                }
                Toast.makeText(activity,"Error in downloading", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showPdfFromFile(final Activity activity, PDFView pdfView, File file) {
        pdfView.fromFile(file)
                .password(null)
                .defaultPage(0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onPageError(new OnPageErrorListener() {
                    @Override
                    public void onPageError(int page, Throwable t) {
                        Toast.makeText(activity,"Error in pdf", Toast.LENGTH_LONG).show();
                    }
                }).load();
    }
}
