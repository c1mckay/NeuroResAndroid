package edu.sdsc.neurores.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import edu.sdsc.neurores.activities.MainActivity;
import edu.sdsc.neurores.R;

public class NotificationHandler extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // This gets called every time a message comes in
    @Override
    public void handleIntent(Intent intent) {
        super.handleIntent(intent);
    }

    // Called when:
    //  A message is received and the app is in the foreground
    //  A message is received that is not explicitly a notification
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();

            if(map.containsKey("conv_id")){
                handleConversationNotification(remoteMessage);
            }else if(map.containsKey("event_id")){
                handleEventNotification(remoteMessage);
            }

        }
    }

    private void handleEventNotification(RemoteMessage remoteMessage){
        Intent intent = new Intent(this, MainActivity.class);

        Map map = remoteMessage.getData();

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        intent.putExtra(MainActivity.CALENDAR_FLAG, "");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.CALENDAR_FLAG, map.get("start_time").toString());
        int notificationID = (title + message).hashCode();
        sendNotification(title,message,intent, notificationID);
    }

    private void handleConversationNotification(RemoteMessage remoteMessage){
        final String senderName = remoteMessage.getNotification().getTitle();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "New message from " + senderName, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, Intent intent, int notificationID) {
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT /*| PendingIntent.FLAG_UPDATE_CURRENT*/ );

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationID, notificationBuilder.build());
        wakeScreen();
    }

    private void wakeScreen() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if(! pm.isScreenOn())
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE, "Notification");
            wl.acquire(1000);
        }
    }
}