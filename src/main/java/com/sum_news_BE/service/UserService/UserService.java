package com.sum_news_BE.service.UserService;

import org.springframework.transaction.annotation.Transactional;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.web.dto.UserRequestDTO;

public interface UserService {

    @Transactional
    void join(UserRequestDTO.JoinDTO joinDTO);

    User login(UserRequestDTO.LoginDTO loginDTO);

    User getUserByEmail(String email);

    @Transactional
    void delete(UserRequestDTO.DeleteDTO deleteDTO);

    @Transactional
    User update(String email, UserRequestDTO.UpdateDTO updateDTO);
} 