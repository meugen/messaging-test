package com.messagingtest.android.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

public interface ServiceApi {

    @PUT("/token")
    Call<TokenResponse> putToken(@Body TokenRequest request);
}
