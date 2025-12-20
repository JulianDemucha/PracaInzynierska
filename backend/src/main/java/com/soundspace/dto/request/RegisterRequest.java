package com.soundspace.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest{
    @NotBlank(message = "Login jest wymagany")
    @Size(min = 3, max = 16, message = "Login musi mieć pomiędzy 3 a 16 znaków")
    private String username;

    @NotBlank(message = "Email jest wymagany")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Niewłaściwy format email")
    private String email;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 8, max = 24, message = "Hasło musi mieć pomiędzy 8 a 24 znaki")
    private String password;

    @NotBlank(message = "Płeć jest wymagana")
     @Pattern(regexp = "^(?i)(MALE|FEMALE)$", message = "Płeć musi być MALE lub FEMALE")
    private String sex;
}
