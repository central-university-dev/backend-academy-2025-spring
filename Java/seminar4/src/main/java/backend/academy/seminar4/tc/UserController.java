package backend.academy.seminar4.tc;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        if (user.getId() != null) {
            return ResponseEntity.badRequest()
                .build();
        }

        var message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom("seminar4@mail.com");
        message.setSubject("You have been successfully registered!");
        message.setText("Welcome to seminar4!");
        mailSender.send(message);

        return ResponseEntity.ok(userRepository.save(user));
    }
}
