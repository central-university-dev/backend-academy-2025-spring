package backend.academy.kafka.monitor;

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
        traverseZooKeeper("", 0);
    }

    @SneakyThrows
    private void traverseZooKeeper(String path, int level) {
        var actualPath = path.isEmpty() ? "/" : path;
        var children = zooKeeper.getChildren(actualPath, false);
        for (var child : children) {
            traverseZooKeeper(path + "/" + child, level + 1);
        }

        var data = zooKeeper.getData(actualPath, false, new Stat());
        if (data != null) {
            log.info("Уровень вложенности: {}, путь: {}, данные: {}", level, path, new String(data));
        } else {
            log.info("Уровень вложенности: {}, путь: {}, промежуточное звено", level, path);
        }
    }

}
