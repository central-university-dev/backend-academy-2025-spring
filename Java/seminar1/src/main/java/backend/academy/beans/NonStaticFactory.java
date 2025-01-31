package backend.academy.beans;

public class NonStaticFactory {
    public BasicComponent getBasicComponent(String message) {
        return new BasicComponent(message);
    }
}
