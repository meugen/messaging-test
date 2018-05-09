package modules;

import com.google.inject.AbstractModule;
import services.PushService;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PushService.class).asEagerSingleton();
    }
}
