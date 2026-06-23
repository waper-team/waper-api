package com.waper.waperapi.controller;

import com.waper.waperapi.model.FrontendPayload;
import com.waper.waperapi.repository.FrontendPayloadRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private/frontend-data")
public class DataIngestController {

    private final FrontendPayloadRepository frontendPayloadRepository;

    public DataIngestController(FrontendPayloadRepository frontendPayloadRepository) {
        this.frontendPayloadRepository = frontendPayloadRepository;
    }

    @PostMapping
    public FrontendPayload ingest(@RequestBody Map<String, Object> payload, Authentication authentication) {
        FrontendPayload doc = new FrontendPayload(authentication.getName(), Instant.now(), payload);
        return frontendPayloadRepository.save(doc);
    }

    @GetMapping
    public List<FrontendPayload> list() {
        return frontendPayloadRepository.findAll();
    }
}
