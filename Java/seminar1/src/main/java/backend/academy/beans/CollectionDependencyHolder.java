package backend.academy.beans;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CollectionDependencyHolder {
    private final List<BasicComponent> basicComponents;

    public CollectionDependencyHolder(List<BasicComponent> basicComponents) {
        this.basicComponents = basicComponents;
    }
}
