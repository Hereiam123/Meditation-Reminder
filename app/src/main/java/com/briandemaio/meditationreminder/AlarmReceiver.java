package com.briandemaio.meditationreminder;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private NotificationManager mNotificationManager;
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    private int mId;
    private String mMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
         mId = intent.getIntExtra(NOTIFICATION_ID, 1);
         mMessage = intent.getStringExtra(NOTIFICATION);
        deliverNotification(context);
    }

    private void deliverNotification(Context context) {
        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, mId, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.mindfulness_practice)
                .setContentText(mMessage)
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        mNotificationManager.notify(mId, builder.build());
    }
}
