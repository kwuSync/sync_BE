package com.sync_BE.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1beta1.TextToSpeechSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

@Configuration
public class TTSConfig {
    @Bean
    public TextToSpeechClient textToSpeechClient() throws IOException {
        String keyFileName = "key-scarab-475015-m5-aa1891b2451b.json";

        ClassPathResource resource = new ClassPathResource(keyFileName);

        GoogleCredentials credentials;
        try (InputStream credentialsStream = resource.getInputStream()) {
            credentials = GoogleCredentials.fromStream(credentialsStream);
        }

        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return TextToSpeechClient.create(settings);
    }

    @Bean(name="ttsExecutor")
    public Executor ttsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("TTS-Async-");
        executor.initialize();
        return executor;
    }
}
