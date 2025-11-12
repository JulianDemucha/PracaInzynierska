package com.soundspace.security.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class RegisterRequest{
    private String username;
    private String email;
    private String password;
    private String sex;
}
