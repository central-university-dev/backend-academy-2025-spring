package backend.academy.kafka.controller;

import backend.academy.kafka.service.UserEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class UserEventsController {

    private final UserEventsService service;

    @PostMapping("/users/{user-id}/events")
    public void sendMessages(
        @PathVariable("user-id") long userId,
        @RequestParam(name = "count", defaultValue = "30") int count
    ) {
        service.sendMessages(userId, count);
    }

}
