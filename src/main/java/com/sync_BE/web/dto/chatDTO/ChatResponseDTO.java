package com.sync_BE.web.dto.chatDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDTO {
    public String answer;
    private String clusterId;
    private String sessionId;
}
