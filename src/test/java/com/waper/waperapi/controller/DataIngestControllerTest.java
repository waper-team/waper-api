package com.waper.waperapi.controller;

import com.waper.waperapi.model.FrontendPayload;
import com.waper.waperapi.repository.FrontendPayloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataIngestControllerTest {

    @Mock
    private FrontendPayloadRepository frontendPayloadRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DataIngestController dataIngestController;

    @Test
    void testIngestSuccess() {
        Map<String, Object> payloadData = Map.of("key", "value");
        when(authentication.getName()).thenReturn("testuser");
        when(frontendPayloadRepository.save(any(FrontendPayload.class))).thenAnswer(invocation -> {
            FrontendPayload fp = invocation.getArgument(0);
            fp.setId("payload-id-1");
            return fp;
        });

        FrontendPayload result = dataIngestController.ingest(payloadData, authentication);

        assertNotNull(result);
        assertEquals("payload-id-1", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(payloadData, result.getPayload());
        assertNotNull(result.getReceivedAt());

        verify(authentication, times(1)).getName();
        verify(frontendPayloadRepository, times(1)).save(any(FrontendPayload.class));
    }
}
