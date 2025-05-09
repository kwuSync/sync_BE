package com.sum_news_BE.web.dto;

import lombok.Getter;
import lombok.Setter;

public class UserRequestDTO {
    @Getter
    @Setter
    public static class JoinDTO {
        private String userid;
        private String password;
        private String name;
    }
}
