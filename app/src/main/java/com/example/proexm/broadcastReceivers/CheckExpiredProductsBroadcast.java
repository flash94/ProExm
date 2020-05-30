package com.example.proexm.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.proexm.R;

import java.util.Random;

public class CheckExpiredProductsBroadcast extends BroadcastReceiver {

    private Context context;
    //db helperDbHelper dbHelper;

    private final String CHANNEL_ID = "expiring_items";
    Random random = new Random();
    int NOTIFICATION_ID = random.nextInt(9999 - 1000) + 1000;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_item_expiring)
                    .setContentTitle("ProExm Expiry Refresh Alert")
                    .setContentText("Click to update product expiry...")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        }
    }


}
