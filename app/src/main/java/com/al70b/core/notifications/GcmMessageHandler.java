package com.al70b.core.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.al70b.R;
import com.al70b.core.MyApplication;
import com.al70b.core.activities.MainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Naseem on 10/9/2015.
 */
public class GcmMessageHandler extends IntentService {

    private static final String TAG = "GcmIntentService";
    public static int NOTIFICATION_ID;
    private NotificationManager mNotificationManager;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(extras.getString("Notification"));
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String jsonString) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        MyApplication app = ((MyApplication) getApplication());

        try {
            CustomNotification customNotification = new CustomNotification(new JSONObject(jsonString));
            String[] msg = buildAppropriateString(customNotification);

            // in case the application is visible and the notification is a message
            if (app.isAppVisible() &&
                    (customNotification.notificationType == CustomNotification.NOTIFICATION_MESSAGE)) {
                return;     // just ignore it
            }

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setContentTitle(getString(R.string.title_activity_very_first))
                            .setSmallIcon(R.drawable.ic_stat_notification_icon)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setAutoCancel(true)
                            .setTicker(msg[1])
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg[1]))
                            .setContentText(msg[1]);   // set the message .. if it's the same user maybe combine them

            mBuilder.setContentIntent(contentIntent);

            Notification notification = mBuilder.build();

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
            notification.ledARGB = Color.YELLOW;
            notification.ledOnMS = 500;
            notification.ledOffMS = 500;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;

            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } catch (JSONException ex) {

        }
    }

    private String[] buildAppropriateString(CustomNotification customNotification) {
        String[] arr = new String[2];

        switch (customNotification.notificationType) {
            case CustomNotification.NOTIFICATION_FRIEND_REQUEST:
                arr[0] = getString(R.string.new_friend_request);
                arr[1] = (getString(R.string.person_sent_you_friend_request, customNotification.userName));
                NOTIFICATION_ID = CustomNotification.NOTIFICATION_FRIEND_REQUEST;
                break;
            case CustomNotification.NOTIFICATION_FRIEND_REQUEST_APPROVAL:
                arr[0] = getString(R.string.friend_request_approved);
                arr[1] = (getString(R.string.person_approved_your_friend_request, customNotification.userName));
                NOTIFICATION_ID = CustomNotification.NOTIFICATION_FRIEND_REQUEST_APPROVAL;
                break;
            case CustomNotification.NOTIFICATION_MESSAGE:
                arr[0] = getString(R.string.new_message);
                arr[1] = (getString(R.string.person_sent_you_message, customNotification.userName));
                NOTIFICATION_ID = CustomNotification.NOTIFICATION_MESSAGE;
                break;
        }

        return arr;
    }

    private class CustomNotification {
        public static final int NOTIFICATION_FRIEND_REQUEST_APPROVAL = 0;
        public static final int NOTIFICATION_FRIEND_REQUEST = 1;
        public static final int NOTIFICATION_MESSAGE = 2;
        private long userId;
        private String userName;
        private int notificationType;
        private String value;

        public CustomNotification(long userId, String userName, int notificationType, String value) {
            this.userId = userId;
            this.userName = userName;
            this.notificationType = notificationType;
            this.value = value;
        }

        public CustomNotification(JSONObject json) throws JSONException {
            this.userId = Long.parseLong(json.getString("user_id"));
            this.userName = json.getString("user_name");
            this.notificationType = getNotificationByString(json.getString("notification_type"));
            this.value = json.getString("value");
        }


        private int getNotificationByString(String notification) {
            if (notification.compareTo("friend_request_approval") == 0) {
                return NOTIFICATION_FRIEND_REQUEST_APPROVAL;
            } else if (notification.compareTo("friend_request") == 0) {
                return NOTIFICATION_FRIEND_REQUEST;
            } else if (notification.compareTo("message") == 0) {
                return NOTIFICATION_MESSAGE;
            } else {
                return -1;
            }
        }
    }


}
