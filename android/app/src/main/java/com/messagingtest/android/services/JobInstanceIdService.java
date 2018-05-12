package com.messagingtest.android.services;

import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.messagingtest.android.MyApp;
import com.messagingtest.android.api.ServiceApi;
import com.messagingtest.android.api.TokenRequest;
import com.messagingtest.android.api.TokenResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobInstanceIdService extends JobService {

    private static final String TAG = JobInstanceIdService.class.getSimpleName();

    private static final String PARAM_TOKEN = "token";

    public static void launch(
            final FirebaseJobDispatcher dispatcher,
            final String token) {
        final Bundle extras = new Bundle();
        extras.putString(PARAM_TOKEN, token);
        final Job job = dispatcher.newJobBuilder()
                .setConstraints(
                        Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.NOW)
                .setExtras(extras)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setRecurring(false)
                .setReplaceCurrent(true)
                .setTag("send-instance-id")
                .setService(JobInstanceIdService.class)
                .build();
        dispatcher.mustSchedule(job);
    }

    private Call<TokenResponse> call;

    @Override
    public boolean onStartJob(final JobParameters job) {
        final MyApp app = MyApp.from(this);
        final ServiceApi api = app.getServiceApi();

        Bundle extras = job.getExtras();
        if (extras == null) {
            extras = Bundle.EMPTY;
        }
        final String token = extras.getString(PARAM_TOKEN);
        this.call = api.putToken(new TokenRequest(app.getUuid(), token));
        this.call.enqueue(new CallbackImpl(job));
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters job) {
        if (call != null) {
            call.cancel();
            call = null;
            return true;
        }
        return false;
    }

    private class CallbackImpl implements Callback<TokenResponse> {

        private final JobParameters job;

        public CallbackImpl(final JobParameters job) {
            this.job = job;
        }

        @Override
        public void onResponse(
                final Call<TokenResponse> call,
                final Response<TokenResponse> response) {
            if (response.isSuccessful()) {
                final MyApp app = MyApp.from(JobInstanceIdService.this);
                app.storeUuid(response.body().uuid);
                jobFinished(job, false);
            } else {
                jobFinished(job, true);
            }
            JobInstanceIdService.this.call = null;
        }

        @Override
        public void onFailure(
                final Call<TokenResponse> call,
                final Throwable t) {
            Log.d(TAG, t.getMessage(), t);
            jobFinished(job, true);
            JobInstanceIdService.this.call = null;
        }
    }
}
