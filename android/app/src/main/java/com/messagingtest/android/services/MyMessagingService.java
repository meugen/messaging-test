package com.messagingtest.android.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.messagingtest.android.MyApp;

import java.util.Map;

public class MyMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());
        final Map<String, String> data = remoteMessage.getData();
        if (data == null || !data.containsKey("message")) {
            Log.d(TAG, "No message found in push");
        } else {
            final String message = data.get("message");
            Log.d(TAG, String.format("Got message '%s'", message));
            MyApp.from(this).getOpenHelper().addMessage(message);
        }
    }
}
