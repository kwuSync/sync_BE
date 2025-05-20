package com.sum_news_BE.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public class UserRequestDTO {
    @Getter
    @Setter
    public static class JoinDTO {
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 4~20자의 영문자와 숫자만 사용 가능합니다.")
        private String userid;

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", 
                message = "비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
        private String passwordConfirm;

        @NotBlank(message = "이름은 필수 입력값입니다.")
        private String name;
    }

    @Getter
    @Setter
    public static class LoginDTO {
        private String userid;
        private String password;
    }

    @Getter
    @Setter
    public static class DeleteDTO {
        private String userid;
        private String password;
    }

    @Getter
    @Setter
    public static class UpdateDTO {
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        private String userid;

        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", 
                message = "비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.")
        private String password;

        private String passwordConfirm;

        private String name;
    }
}
