package com.sync_BE.service.UserService;

import com.sync_BE.domain.User;
import com.sync_BE.web.dto.userDTO.UserRequestDTO;

public interface UserService {

    void join(UserRequestDTO.JoinDTO joinDTO);

    User login(UserRequestDTO.LoginDTO loginDTO);

    User getUserByEmail(String email);

    void delete(UserRequestDTO.DeleteDTO deleteDTO);

    User update(String email, UserRequestDTO.UpdateDTO updateDTO);

    // 비밀번호 재설정 요청 (이메일 인증번호 전송)
    void requestPasswordReset(UserRequestDTO.PasswordResetRequestDTO requestDTO);

    // 비밀번호 재설정 확인 및 변경
    void confirmPasswordReset(UserRequestDTO.PasswordResetConfirmDTO confirmDTO);
} 