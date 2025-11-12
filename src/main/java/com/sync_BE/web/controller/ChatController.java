package com.sync_BE.web.controller;

import com.sync_BE.api.ApiResponse;
import com.sync_BE.web.dto.chatDTO.ChatRequestDTO;
import com.sync_BE.web.dto.chatDTO.ChatResponseDTO;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Tag(name="chat", description = "챗봇 API")
public class ChatController {

    @Value("${python.api.url}")
    private String pythonApiUrl;

    @PostMapping
    public Mono<ApiResponse<ChatResponseDTO>> handleChat(@RequestBody ChatRequestDTO request) {
        log.debug("Python 챗봇 서버로 요청 전달: {}", pythonApiUrl);
        log.debug("요청 내용: {}", request.getMessage());

        WebClient webClient = WebClient.create(pythonApiUrl);

        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponseDTO.class)
                .map(pythonResponse ->
                        ApiResponse.ok("챗봇 응답입니다.", pythonResponse)
                )
                .onErrorResume(e -> {
                    log.error("Python 챗봇 서버 연결 실패: {}", e.getMessage());
                    return Mono.just(ApiResponse.error("500", "챗봇 서버와 통신 중 오류가 발생했습니다."));
                });
    }
}
