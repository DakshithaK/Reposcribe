package com.reposcribe.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reposcribe.ai.dto.OllamaRequest;
import com.reposcribe.ai.dto.OllamaResponse;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class OllamaClient {

    private final String baseUrl;
    private final String defaultModel;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3}") String defaultModel) {
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel;
        this.objectMapper = new ObjectMapper();
        
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();
    }

    public String generate(String prompt) throws IOException {
        return generate(defaultModel, prompt);
    }

    public String generate(String model, String prompt) throws IOException {
        if (!isAvailable()) {
            throw new IOException("Ollama service is not available. Make sure Ollama is running.");
        }
        
        OllamaRequest request = new OllamaRequest(model, prompt);
        String url = baseUrl + "/api/generate";
        
        String json = objectMapper.writeValueAsString(request);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        
        Request httpRequest = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ollama API error: " + response.code());
            }

            String responseBody = response.body().string();
            OllamaResponse ollamaResponse = objectMapper.readValue(responseBody, OllamaResponse.class);
            
            if (ollamaResponse.getResponse() == null || ollamaResponse.getResponse().isEmpty()) {
                throw new IOException("Empty response from Ollama");
            }
            
            return ollamaResponse.getResponse();
        }
    }

    public boolean isAvailable() {
        try {
            String url = baseUrl + "/api/tags";
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public String getDefaultModel() {
        return defaultModel;
    }
}

