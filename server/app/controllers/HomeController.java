package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;

import services.PushService;
import views.html.*;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Singleton
public class HomeController extends Controller {

    private final HttpExecutionContext context;
    private final PushService pushService;

    @Inject
    public HomeController(
            final HttpExecutionContext context,
            final PushService pushService) {
        this.context = context;
        this.pushService = pushService;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public CompletionStage<Result> index() {
        return CompletableFuture.supplyAsync(this::_index, context.current());
    }

    private Result _index() {
        return ok(index.render("Your new application is ready."));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> putToken() {
        return CompletableFuture.supplyAsync(() -> _putToken(request().body().asJson()), context.current());
    }

    private Result _putToken(final JsonNode body) {
        final TokenRequest request = Json.fromJson(body, TokenRequest.class);
        final UUID newUuid = pushService.setToken(request.uuid, request.token);
        return Results.ok(Json.toJson(new TokenResponse(newUuid)));
    }

    private static class TokenRequest {

        public UUID uuid;
        public String token;
    }

    private static class TokenResponse {

        public final UUID uuid;

        public TokenResponse(UUID uuid) {
            this.uuid = uuid;
        }
    }

}
