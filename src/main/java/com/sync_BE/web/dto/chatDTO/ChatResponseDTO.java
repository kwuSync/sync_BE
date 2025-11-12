package com.sync_BE.web.dto.chatDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDTO {
    public String answer;
    @JsonProperty("cluster_id")
    private String clusterId;

    @JsonProperty("session_id")
    private String sessionId;
}
