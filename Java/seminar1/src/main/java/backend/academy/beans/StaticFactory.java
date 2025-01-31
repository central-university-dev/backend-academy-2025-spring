package backend.academy.beans;

public class StaticFactory {
    public static BasicComponent getBasicComponent(String message) {
        return new BasicComponent(message);
    }
}
