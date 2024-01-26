package com.zonetech.online.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zonetech.online.R;
import com.zonetech.online.SplashActivity;
import com.zonetech.online.common.CommonWebViewActivity;
import com.zonetech.online.server.DeepLinkActivity;
import com.zonetech.online.utils.Utils;

import org.json.JSONObject;

import java.net.URL;

public class NotificationScheduledWorker extends Worker {
    public static final String NOTIFICATION_TITLE = "notificationTitle";
    public static final String NOTIFICATION_MESSAGE = "notificationMessage";
    public static final String NOTIFICATION_IMAGE = "notificationImage";
    public static final String NOTIFICATION_BIG_IMAGE = "notificationBigImage";
    public static final String NOTIFICATION_LINK = "notificationLink";
    private WorkerParameters workerParameters;
    public NotificationScheduledWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.workerParameters = workerParams;
    }

    @NonNull
    @Override
    public Result doWork() {
        showNotification(workerParameters);
        return Result.success();
    }

    private void showNotification(WorkerParameters workerParameters) {
        try {
            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            if(Utils.isValidString(workerParameters.getInputData().getString(NOTIFICATION_LINK))){
                intent = new Intent(getApplicationContext(), DeepLinkActivity.class);
                intent.putExtra("url", workerParameters.getInputData().getString(NOTIFICATION_LINK));
                pendingIntent = TaskStackBuilder.create(getApplicationContext())
                        .addParentStack(DeepLinkActivity.class)
                        .addNextIntent(intent)
                        .getPendingIntent(0, Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), ZTFirebaseMessagingService.channel_id)
                    .setContentTitle(workerParameters.getInputData().getString(NOTIFICATION_TITLE))
                    .setContentText(workerParameters.getInputData().getString(NOTIFICATION_MESSAGE))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent)
                    .setContentInfo("Hello")
                    .setLights(Color.RED, 1000, 300)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_notification);

            try {
                String ni = workerParameters.getInputData().getString(NOTIFICATION_IMAGE);
                if(Utils.isValidString(ni)){
                    Bitmap smallImage = BitmapFactory.decodeStream(new URL(ni).openConnection().getInputStream());
                    if(smallImage != null){
                        notificationBuilder.setLargeIcon(smallImage);
                    }
                }
                String nbi = workerParameters.getInputData().getString(NOTIFICATION_BIG_IMAGE);
                if (Utils.isValidString(nbi)) {
                    Bitmap bigPicture = BitmapFactory.decodeStream(new URL(nbi).openConnection().getInputStream());
                    notificationBuilder.setStyle(
                            new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).bigLargeIcon(null)
                    );
                }
            } catch (Exception e) {
                if (Utils.isDebugModeOn) {
                    e.printStackTrace();
                }
            }

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        ZTFirebaseMessagingService.channel_id, ZTFirebaseMessagingService.channel_id, NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription("ZoneTechChannel");
                channel.setShowBadge(true);
                channel.canShowBadge();
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
                assert notificationManager != null;
                notificationManager.createNotificationChannel(channel);
            }
            assert notificationManager != null;
            notificationManager.notify(0, notificationBuilder.build());
        }catch (Exception e){
            if(Utils.isDebugModeOn){
                e.printStackTrace();
            }
        }
    }
}
