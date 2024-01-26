package com.zonetech.online.notification;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zonetech.online.R;
import com.zonetech.online.utils.Utils;
import com.zonetech.online.views.ZTWebView;

import org.json.JSONArray;
import org.json.JSONObject;

class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NewsViewHolder> {

    private JSONArray planItem;
    private int rowLayout;
    private Activity context;
    private DisplayMetrics metrics;
    private int imageWidth;

    protected class NewsViewHolder extends RecyclerView.ViewHolder {
        private ZTWebView planText;
        private TextView newsDate;
        private TextView viewMore;
        public NewsViewHolder(View v) {
            super(v);
            planText = v.findViewById(R.id.planText);
            newsDate = v.findViewById(R.id.newsDate);
            viewMore = v.findViewById(R.id.viewMore);
        }
    }

    public NotificationAdapter(JSONArray planItem, int rowLayout, Activity context) {
        this.planItem = planItem;
        this.rowLayout = rowLayout;
        this.context = context;
        metrics = Utils.getMetrics(context);
        if(metrics != null) {
            imageWidth = (metrics.widthPixels - (int) (32 * metrics.density)) / 2;
        }
    }

    @Override
    public NotificationAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new NotificationAdapter.NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NotificationAdapter.NewsViewHolder holder, final int position) {
        final JSONObject item = planItem.optJSONObject(holder.getAdapterPosition());
        holder.planText.getSettings().setDefaultTextEncodingName("utf-8");
        holder.planText.getSettings().setJavaScriptEnabled(true);
        String question = planItem.optJSONObject(position).optString("QuestionEng");
        question = question.replaceAll("<img ", "<img style=\"max-width:100%\"");
        String questionHindi = planItem.optJSONObject(position).optString("QuestionHindi");
        questionHindi = questionHindi.replaceAll("<img ", "<img style=\"max-width:100%\"");
        question = question + questionHindi;

        String head1 = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
        String newHtml = head1 + question + "</body></html>";
        holder.planText.loadDataWithBaseURL(null, newHtml, "text/html", "UTF-8", null);
        holder.planText.setVisibility(View.VISIBLE);
        holder.newsDate.setText("Date: "+planItem.optJSONObject(position).optString("CreatedDate"));
        SpannableString content = new SpannableString("View Detail");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.viewMore.setText(content);
        if(position == planItem.length() - 1){
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), (int)(16 * metrics.density));
        }else{
            holder.itemView.setPadding((int)(8 * metrics.density), (int)(16 * metrics.density), (int)(8 * metrics.density), 0);
        }
        holder.viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    showDialog(newHtml, item);
                }catch (Exception e){

                }
            }
        });
    }

    AlertDialog dialog;
    private void showDialog(String newHtml, JSONObject item) {
        try {
            String adminCommentString = item.optString("AdminComment");
            String Commnets = item.optString("Commnets");
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.notification_alert, null);
            ZTWebView title = promptsView.findViewById(R.id.title);
            ZTWebView adminWebview = promptsView.findViewById(R.id.adminWebview);
            TextView adminComment = promptsView.findViewById(R.id.adminComment);
            ZTWebView commentWebview = promptsView.findViewById(R.id.commentWebview);
            TextView comment = promptsView.findViewById(R.id.comment);

            TextView ok = promptsView.findViewById(R.id.ok);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            title.loadDataWithBaseURL(null, newHtml, "text/html", "UTF-8", null);
            if(adminCommentString != null && !adminCommentString.trim().equalsIgnoreCase("")) {
                adminWebview.setVisibility(View.VISIBLE);
                adminComment.setVisibility(View.VISIBLE);
                adminCommentString = adminCommentString.replaceAll("<img ", "<img style=\"max-width:100%\"");
                String head1 = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
                String newHtml2 = head1 + adminCommentString + "</body></html>";
                adminWebview.loadDataWithBaseURL(null, newHtml2, "text/html", "UTF-8", null);
            } else {
                adminWebview.setVisibility(View.GONE);
                adminComment.setVisibility(View.GONE);
            }

            if(Commnets != null && !Commnets.trim().equalsIgnoreCase("")) {
                commentWebview.setVisibility(View.VISIBLE);
                comment.setVisibility(View.VISIBLE);
                Commnets = Commnets.replaceAll("<img ", "<img style=\"max-width:100%\"");
                String head1 = "<html><head><style type=\"text/css\">@font-face {font-family: k010;src: url(\"file:///android_asset/Kruti.ttf\")}</style></head><body>";
                String newHtml2 = head1 + Commnets + "</body></html>";
                commentWebview.loadDataWithBaseURL(null, newHtml2, "text/html", "UTF-8", null);
            } else {
                commentWebview.setVisibility(View.GONE);
                comment.setVisibility(View.GONE);
            }
            builder.setView(promptsView);
            dialog = builder.create();
            if (!Utils.isActivityDestroyed(context))
                dialog.show();
        } catch (Exception e) {
            if (Utils.isDebugModeOn)
                e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return planItem.length();
    }
    public void refreshAdapter(JSONArray items) {
        planItem = items;
        notifyDataSetChanged();
    }
}
