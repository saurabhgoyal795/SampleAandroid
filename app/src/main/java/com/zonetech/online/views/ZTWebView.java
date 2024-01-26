package com.zonetech.online.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

public class ZTWebView extends WebView {
    public interface SizeListener{
        void onSizeChanged(int w, int h, int ow, int oh);
    }
    public SizeListener sizeListener;
    public ZTWebView(Context context) {
        super(context);
    }

    public ZTWebView(Context context, AttributeSet attrb) {
        super(context, attrb);
    }
    public ZTWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ZTWebView(Context context, AttributeSet attrs, int defStyle,
                     int arg3) {
        super(context, attrs, defStyle, arg3);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        if (outAttrs != null) {
            // remove other IME_ACTION_*
            outAttrs.imeOptions &= ~EditorInfo.IME_ACTION_GO;
            outAttrs.imeOptions &= ~EditorInfo.IME_ACTION_SEARCH;
            outAttrs.imeOptions &= ~EditorInfo.IME_ACTION_SEND;
            outAttrs.imeOptions &= ~EditorInfo.IME_ACTION_DONE;
            outAttrs.imeOptions &= ~EditorInfo.IME_ACTION_NONE;
            // add IME_ACTION_NEXT instead
            outAttrs.imeOptions |= EditorInfo.IME_ACTION_NEXT;
        }
        return inputConnection;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if(sizeListener != null){
            sizeListener.onSizeChanged(w, h, ow, oh);
        }
    }
}
