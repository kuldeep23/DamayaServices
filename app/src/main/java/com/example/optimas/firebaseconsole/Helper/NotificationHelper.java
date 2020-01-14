package com.example.optimas.firebaseconsole.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.optimas.firebaseconsole.R;

public class NotificationHelper extends ContextWrapper {

    private  static  final  String HAZIR_CHANEL_ID= "com.example.optimas.firebaseconsole.Hazir";
    private  static  final  String HAZIR_CHANEL_NAME= "Hazir";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            createChannel();
    }

    private void createChannel() {
        NotificationChannel hazirChanel= new NotificationChannel(HAZIR_CHANEL_ID,
                HAZIR_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        hazirChanel.enableLights(false);
        hazirChanel.enableVibration(true);
        hazirChanel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(hazirChanel);

    }

    public NotificationManager getManager() {
        if(manager==null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public  android.app.Notification.Builder getHazirChannelNotification(String title, String body, PendingIntent contentIntent,
                                                                         Uri soundUri)
    {
        return  new android.app.Notification.Builder(getApplicationContext(),HAZIR_CHANEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public  android.app.Notification.Builder getHazirChannelNotification(String title, String body,
                                                                         Uri soundUri)
    {
        return  new android.app.Notification.Builder(getApplicationContext(),HAZIR_CHANEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri)
                .setAutoCancel(false);
    }
}
