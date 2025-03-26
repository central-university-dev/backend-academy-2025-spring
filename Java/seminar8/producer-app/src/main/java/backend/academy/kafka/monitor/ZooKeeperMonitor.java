package backend.academy.kafka.monitor;

import static java.lang.Math.max;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ZooKeeperMonitor {

    private final ZooKeeper zooKeeper;

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void logZooKeeperState() {
        var description = new StringBuilder();
        traverseZooKeeper("", description, 0);
        log.info("Данные в ZooKeeper: \n{}", description);
    }

    @SneakyThrows
    private void traverseZooKeeper(String path, StringBuilder description, int level) {
        var actualPath = path.isEmpty() ? "/" : path;

        description.append("\t".repeat(max(1, level)));
        var data = Optional.ofNullable(zooKeeper.getData(actualPath, false, new Stat()))
            .map(String::new)
            .orElse("");
        if (!data.isBlank()) {
            description.append(
                "- Уровень вложенности: %s, путь: %s, данные: %s\n"
                    .formatted(level, path, data));
        } else {
            description.append(
                "- Уровень вложенности: %s, путь: %s, промежуточное звено\n"
                    .formatted(level, path));
        }

        var children = zooKeeper.getChildren(actualPath, false);
        for (var child : children) {
            traverseZooKeeper(path + "/" + child, description, level + 1);
        }
    }

}
