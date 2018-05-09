package com.messagingtest.android.api;

import java.util.UUID;

public class TokenRequest {

    public final UUID uuid;
    public final String token;

    public TokenRequest(final UUID uuid, final String token) {
        this.uuid = uuid;
        this.token = token;
    }
}
