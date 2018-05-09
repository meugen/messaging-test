package com.messagingtest.android;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.messagingtest.android.api.ServiceApi;
import com.messagingtest.android.db.MyOpenHelper;

import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyApp extends Application {

    private static final String PREF_UUID = "uuid";

    public static MyApp from(final Context context) {
        return (MyApp) context.getApplicationContext();
    }

    private ServiceApi serviceApi;
    private MyOpenHelper openHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }
        final OkHttpClient client = builder.build();

        final Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BuildConfig.SERVER_URL)
                .client(client)
                .build();
        this.serviceApi = retrofit.create(ServiceApi.class);

        this.openHelper = new MyOpenHelper(this);
    }

    public ServiceApi getServiceApi() {
        return serviceApi;
    }

    public MyOpenHelper getOpenHelper() {
        return openHelper;
    }

    public void storeUuid(final UUID uuid) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString(PREF_UUID, uuid.toString()).apply();
    }

    public UUID getUuid() {
        final String value = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREF_UUID, null);
        return value == null ? null : UUID.fromString(value);
    }
}
