package backend.academy.seminar13.hotel.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenTelemetryAppenderInitializer implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    @Override
    public void afterPropertiesSet() throws Exception {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
