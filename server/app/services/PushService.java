package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class PushService {

    private static final long MIN_PERIOD = TimeUnit.MINUTES.toMillis(1);
    private static final long MAX_PERIOD = TimeUnit.MINUTES.toMillis(10);

    private static final String SERVICE_ACCOUNT_PATH = "/opt/messagingtest-fe1d5-firebase-adminsdk-eka0y-f556f53a77.json";

    private final WSClient client;
    private final Map<UUID, String> tokens;
    private final ScheduledExecutorService executor;
    private final AtomicInteger number;
    private final DateFormat dateFormat;

    @Inject
    public PushService(final WSClient client) {
        this.client = client;
        this.tokens = new HashMap<>();
        this.executor = Executors.newScheduledThreadPool(2);
        this.dateFormat = new SimpleDateFormat("dd MMMM, HH:mm:ss.SSS", Locale.ENGLISH);
        this.number = new AtomicInteger(0);

        scheduleResetNumber();
        initApp();
        runNext();
    }

    private void scheduleResetNumber() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        executor.scheduleAtFixedRate(() -> number.set(0),
                calendar.getTimeInMillis() - System.currentTimeMillis(),
                TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    public synchronized UUID setToken(final UUID uuid, final String token) {
        if (uuid != null) {
            tokens.put(uuid, token);
            return uuid;
        }
        UUID newUuid = null;
        while (newUuid == null || tokens.containsKey(newUuid)) {
            newUuid = UUID.randomUUID();
        }
        tokens.put(newUuid, token);
        Logger.debug(String.format("Got new token:\n  token: %s\n  uuid: %s",
                token, newUuid));
        return newUuid;
    }

    private void initApp() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream(SERVICE_ACCOUNT_PATH);

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://messagingtest-fe1d5.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getAccessToken() throws IOException {
        GoogleCredential googleCredential = GoogleCredential
                .fromStream(new FileInputStream(SERVICE_ACCOUNT_PATH))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredential.refreshToken();
        return googleCredential.getAccessToken();
    }

    private void runNext() {
        final long period = ThreadLocalRandom.current().nextLong(
                MAX_PERIOD - MIN_PERIOD) + MIN_PERIOD;
        Logger.debug("Next run in ~" + TimeUnit.MILLISECONDS.toMinutes(period) + " min");
        executor.schedule(this::internalRun, period, TimeUnit.MILLISECONDS);
    }

    private void internalRun() {
        final List<String> tokensCopy;
        synchronized (this) {
            tokensCopy = new ArrayList<>(tokens.values());
        }
        Logger.debug(String.format("Tokens count: %d", tokensCopy.size()));
        final int nextNumber = number.incrementAndGet();
        for (String token : tokensCopy) {
            sendPush(token, nextNumber);
        }
        runNext();
    }

    private void sendPush(final String token, final int nextNumber) {
        Logger.debug(String.format("Sending push for token: %s", token));
        try {
            final JsonNode body = buildBody(token, nextNumber);
            Logger.debug(String.format("Body: %s", body));
            final String accessToken = getAccessToken();
            client.url("https://fcm.googleapis.com/v1/projects/messagingtest-fe1d5/messages:send")
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .post(body)
                    .whenComplete(this::onPushSent);
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        }
    }

    private JsonNode buildBody(final String token, final int next) {
        final ObjectNode data = Json.newObject();
        data.put("message", String.format(Locale.ENGLISH,
                "%1$s - %2$04d", dateFormat.format(new Date()), next));

        final ObjectNode message = Json.newObject();
        message.put("token", token);
        message.set("data", data);

        final ObjectNode body = Json.newObject();
        body.set("message", message);
        return body;
    }

    private void onPushSent(final WSResponse response, final Throwable th) {
        if (th != null) {
            Logger.error(th.getMessage(), th);
        }
        Logger.debug(response.getStatusText());
        Logger.debug(response.getBody());
    }
}
