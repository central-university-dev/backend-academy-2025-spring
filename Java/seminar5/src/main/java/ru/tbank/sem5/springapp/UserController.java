package ru.tbank.sem5.springapp;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<User> getUsers() {
        userRepository.transferMoney(1L, 1L, 10);
        return userRepository.getUsers();
    }
}
