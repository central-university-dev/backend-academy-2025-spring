package backend.academy.beans;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Slf4j
@Component
public class DependencyHolder {
    private final BasicComponent dependency;
    @Autowired
    @Qualifier("basicComponent2")
    private BasicComponent dependency2;

    @Value("${log.default-message}")
    private String defaultMessage;

    public DependencyHolder(@Autowired @Qualifier("basicComponent1") BasicComponent dependency) {
        this.dependency = dependency;
        log.info("DependencyHolder created with dependency: {}", dependency);
    }

    @PostConstruct
    public void init() {
        log.info("DependencyHolder initialization method invoked. constructor dependency: {}. field dependency: {}", dependency, dependency2);
    }

}
