package com.sum_news_BE.service.UserService;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.repository.UserRepository;
import com.sum_news_BE.service.MailService.MailService;
import com.sum_news_BE.service.MailService.MailVerificationService;
import com.sum_news_BE.web.dto.userDTO.UserRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailVerificationService mailVerificationService;
    private final MailService mailService;

    @Override
    @Transactional
    public void join(UserRequestDTO.JoinDTO joinDTO) {
        if (!joinDTO.getPassword().equals(joinDTO.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (!mailVerificationService.verifyAuthNumber(joinDTO.getEmail(), joinDTO.getAuthNumber())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        if (userRepository.existsByEmail(joinDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(joinDTO.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .nickname(joinDTO.getNickname())
                .password(passwordEncoder.encode(joinDTO.getPassword()))
                .email(joinDTO.getEmail())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        mailVerificationService.removeAuthNumber(joinDTO.getEmail());
    }

    @Override
    @Transactional
    public void requestPasswordReset(UserRequestDTO.PasswordResetRequestDTO requestDTO) {
        if (!userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }

        mailService.sendPasswordResetMail(requestDTO.getEmail());
    }

    @Override
    @Transactional
    public void confirmPasswordReset(UserRequestDTO.PasswordResetConfirmDTO confirmDTO) {
        if (!confirmDTO.getNewPassword().equals(confirmDTO.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        if (!mailVerificationService.verifyAuthNumber(confirmDTO.getEmail(), confirmDTO.getAuthNumber())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다.");
        }

        User user = userRepository.findByEmail(confirmDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updatePassword(passwordEncoder.encode(confirmDTO.getNewPassword()));
        userRepository.save(user);

        mailVerificationService.removeAuthNumber(confirmDTO.getEmail());
    }

    @Override
    @Transactional
    public User login(UserRequestDTO.LoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    @Override
    @Transactional
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    @Override
    @Transactional
    public void delete(UserRequestDTO.DeleteDTO deleteDTO) {
        User user = userRepository.findByEmail(deleteDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(deleteDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User update(String email, UserRequestDTO.UpdateDTO updateDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!user.getNickname().equals(updateDTO.getNickname()) && 
            userRepository.existsByNickname(updateDTO.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            if (!updateDTO.getPassword().equals(updateDTO.getPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            user.updatePassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        user.updateNickname(updateDTO.getNickname());
        return userRepository.save(user);
    }

} 