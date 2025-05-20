package com.sum_news_BE.service.UserService;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.web.dto.UserRequestDTO;

public interface UserService {
    // 회원가입
    User joinUser(UserRequestDTO.JoinDTO joinDTO);
    
    // 로그인
    User login(UserRequestDTO.LoginDTO loginDTO);
    
    // 사용자 조회
    User getUserById(String userid);

    // 사용자 삭제
    User delete(String userid);

    // 사용자 정보 수정
    User update(UserRequestDTO.UpdateDTO updateDTO);
} 