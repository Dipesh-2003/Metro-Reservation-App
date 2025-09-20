package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class UserDto {
    private Integer userId;
    private String name;
    private String email;
    private String profileImageUrl;
}
