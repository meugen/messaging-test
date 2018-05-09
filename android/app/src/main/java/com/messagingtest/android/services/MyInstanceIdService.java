package com.messagingtest.android.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.messagingtest.android.MyApp;
import com.messagingtest.android.api.ServiceApi;
import com.messagingtest.android.api.TokenRequest;
import com.messagingtest.android.api.TokenResponse;

import java.io.IOException;

import retrofit2.Response;

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
        final ServiceApi api = app.getServiceApi();
        try {
            final Response<TokenResponse> response = api
                    .putToken(new TokenRequest(app.getUuid(), token))
                    .execute();
            if (response.isSuccessful()) {
                app.storeUuid(response.body().uuid);
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage(), e);
        }
    }
}
