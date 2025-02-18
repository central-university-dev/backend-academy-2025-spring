package backend.academy.seminar4.tc;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Data
public class User {

    @Id
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}
