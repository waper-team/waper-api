package com.waper.waperapi.model;

import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "frontend_payloads")
public class FrontendPayload {

    @Id
    private String id;

    private String username;
    private Instant receivedAt;
    private Map<String, Object> payload;

    public FrontendPayload() {
    }

    public FrontendPayload(String username, Instant receivedAt, Map<String, Object> payload) {
        this.username = username;
        this.receivedAt = receivedAt;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
