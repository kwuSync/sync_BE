package com.sum_news_BE.service.UserService;

import com.sum_news_BE.domain.User;
import com.sum_news_BE.web.dto.UserRequestDTO;

public interface UserCommandService {
    User joinUser(UserRequestDTO.JoinDTO joinDTO);
}
