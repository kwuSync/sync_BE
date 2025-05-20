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
        // 아이디 형식 검증
        if (!joinDTO.getUserid().matches("^[a-zA-Z0-9]{4,20}$")) {
            throw new IllegalArgumentException("아이디는 4~20자의 영문자와 숫자만 사용 가능합니다.");
        }

        // 비밀번호 형식 검증
        if (!joinDTO.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
            throw new IllegalArgumentException("비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.");
        }

        // 비밀번호 확인
        if (!joinDTO.getPassword().equals(joinDTO.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 아이디 중복 확인
        if (userRepository.findByUserid(joinDTO.getUserid()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

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

    @Override
    public User delete(String userid) {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
        return user;
    }

    @Override
    public User update(UserRequestDTO.UpdateDTO updateDTO) {
        // 사용자 조회
        User user = userRepository.findByUserid(updateDTO.getUserid())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 변경 시 비밀번호 확인
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            // 비밀번호 형식 검증
            if (!updateDTO.getPassword().matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")) {
                throw new IllegalArgumentException("비밀번호는 8자 이상의 영문자와 숫자 조합이어야 합니다.");
            }

            if (updateDTO.getPasswordConfirm() == null || updateDTO.getPasswordConfirm().isEmpty()) {
                throw new IllegalArgumentException("비밀번호 확인을 입력해주세요.");
            }
            if (!updateDTO.getPassword().equals(updateDTO.getPasswordConfirm())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }
            String encodedPassword = passwordEncoder.encode(updateDTO.getPassword());
            user.setPassword(encodedPassword);
        }

        // 이름이 제공된 경우에만 업데이트
        if (updateDTO.getName() != null && !updateDTO.getName().isEmpty()) {
            user.setName(updateDTO.getName());
        }

        return userRepository.save(user);
    }
} 