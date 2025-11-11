package com.sync_BE.web.dto.chatDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDTO {
    public String message;
    public String clusterId;
    public String sessionId;
}
