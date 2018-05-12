package com.messagingtest.android.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.messagingtest.android.MyApp;

public class MyInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = MyInstanceIdService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "New token: " + token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(final String token) {
        final MyApp app = MyApp.from(this);
        JobInstanceIdService.launch(app.getDispatcher(), token);
    }
}
