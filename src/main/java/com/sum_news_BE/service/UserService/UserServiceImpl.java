package com.sum_news_BE.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sum_news_BE.converter.UserConverter;
import com.sum_news_BE.domain.User;
import com.sum_news_BE.repository.UserRepository;
import com.sum_news_BE.web.dto.UserRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User joinUser(UserRequestDTO.JoinDTO joinDTO) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(joinDTO.getPassword());
        joinDTO.setPassword(encodedPassword);

        User user = UserConverter.toUser(joinDTO);
        return userRepository.save(user);
    }

    @Override
    public User login(UserRequestDTO.LoginDTO loginDTO) {
        System.out.println("로그인 시도: " + loginDTO.getUserid());
        User user = userRepository.findByUserid(loginDTO.getUserid())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        System.out.println("유저 조회 성공: " + user.getUserid());
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            System.out.println("비밀번호 불일치");
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        System.out.println("로그인 성공");
        return user;
    }

    @Override
    public User getUserById(String userid) {
        return userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
} 