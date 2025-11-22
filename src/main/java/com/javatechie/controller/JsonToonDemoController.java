package com.javatechie.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.dto.UserProfile;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import im.arun.toon4j.Toon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;

@RestController
@RequestMapping("/api/demo")
@Slf4j
public class JsonToonDemoController {

    private final OllamaChatModel ollama;
    private final RestClient rest = RestClient.create("http://localhost:11434");
    private final ObjectMapper mapper = new ObjectMapper();

    private final Encoding encoder =
            Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    public JsonToonDemoController(OllamaChatModel ollama) {
        this.ollama = ollama;
    }

    @GetMapping("/json-vs-toon")
    public Map<String, Object> compareJsonVsToon() {

        try {
            // ---------- 1. Sample POJO ----------
            UserProfile profile = new UserProfile(
                    "Java Techie",
                    32,
                    "India",
                    List.of("Spring Boot", "Kafka", "Microservices", "gRPC", "Kubernetes"),
                    new UserProfile.Address("MG Road", "Bangalore", "560001")
            );

            // ---------- 2. JSON ----------
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(profile);

            log.info("JSON Payload: {}", json);

            // ---------- 3. TOON ----------
            String toon = Toon.encode(profile);
            log.info("TOON Payload: {}", toon);

            // ---------- 4. Token counting using LM Studio's tokenizer ----------
            int jsonTokens = encoder.encode(json).size();
            log.info("JSON Input Tokens: {}", jsonTokens);
            int toonTokens = encoder.encode(toon).size();
            log.info("TOON Input Tokens: {}", toonTokens);

            // ---------- 5. Chat processing using OllamaChatModel ----------
            String jsonSummary = ollama.call("Summarize this profile:\n" + json);
            String toonSummary = ollama.call("Summarize this profile:\n" + toon);

            // ---------- 6. Build response ----------
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("json_payload", json);
            result.put("toon_payload", toon);
            result.put("input_json_tokens", jsonTokens);
            result.put("input_toon_tokens", toonTokens);
            result.put("token_savings", jsonTokens - toonTokens);
            result.put("json_summary", jsonSummary);
            result.put("toon_summary", toonSummary);
            return result;

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }


}
