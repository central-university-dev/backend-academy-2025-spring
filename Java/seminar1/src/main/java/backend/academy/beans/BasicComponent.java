package backend.academy.beans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BasicComponent {
    private final String message;
    private String debugMessage;

    public BasicComponent(String message) {
        this.message = message;
        log.info("message from constructor: {}", message);
    }

    public void init() {
        log.info("from init: {}", message);
    }

    @Autowired
    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
        log.info("from setter: {}", debugMessage);
    }

    @Override
    public String toString() {
        return "BasicComponent{message=" + message + ", debugMessage=" + debugMessage + '}';
    }
}
