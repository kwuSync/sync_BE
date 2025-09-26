package com.sync_BE.web.dto.userDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public class UserRequestDTO {
    @Getter
    @Setter
    public static class JoinDTO {
        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다.")
        private String nickname;

        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수 입력값입니다.")
        private String passwordConfirm;

        @NotBlank(message = "인증번호는 필수 입력값입니다.")
        private int authNumber;
    }

    @Getter
    @Setter
    public static class LoginDTO {
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        private String password;
    }

    @Getter
    @Setter
    public static class DeleteDTO {
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        private String password;
    }

    @Getter
    @Setter
    public static class UpdateDTO {
        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다.")
        private String nickname;

        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.")
        private String password;

        private String passwordConfirm;
    }

    @Getter
    @Setter
    public static class PasswordResetRequestDTO {
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;
    }

    @Getter
    @Setter
    public static class PasswordResetConfirmDTO {
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "인증번호는 필수 입력값입니다.")
        private int authNumber;

        @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인은 필수 입력값입니다.")
        private String newPasswordConfirm;
    }
}
