package com.example.proexm.broadcastReceivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.proexm.MainActivity;
import com.example.proexm.R;
import com.example.proexm.database.DbHelper;

import java.util.Random;


public class ExpiryBroadcast extends BroadcastReceiver {

    private Context context;
    //db helperDbHelper dbHelper;

    private final String CHANNEL_ID = "expiring_items";
    //private final int NOTIFICATION_ID = 200;
    Random rand = new Random();
    int NOTIFICATION_ID = rand.nextInt(1000)*1000;



    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        String content = intent.getStringExtra("content");
        //int id = intent.getIntExtra("id", 0);
        createNotification(context, "" ,  name + " Coming soon" , name, content);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
//                        .setSmallIcon(R.drawable.ic_item_expiring)
//                        .setContentTitle("ProExm Product Expiry")
//                        .setContentText("Some products will soon expire, check now...")
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//                notificationManager.notify(NOTIFICATION_ID, builder.build());
//
//        }

    }

    private void createNotification(Context context, String msg, String msgText, String name, String content) {
        // Builds a notification
        // Define an Intent and an action to perform with it by another application
        PendingIntent notificIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_item_expiring)
                .setContentTitle(name)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                //.setTicker(msgText)
                //.setSmallIcon(R.mipmap.ic_launcher);

        //the intent when the notification is clicked on
        mBuilder.setContentIntent(notificIntent); //goes to MainActivity

        //how the user will be notified
        mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);

        //stop notification when it's clicked on
        mBuilder.setAutoCancel(true);

        //now to notify the user with NotificationManager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
