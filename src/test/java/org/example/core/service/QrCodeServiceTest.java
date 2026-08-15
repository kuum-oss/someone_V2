package org.example.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    @DisplayName("Should generate a valid 64-character SHA-256 hex token for an order")
    void testGenerateOrderToken() {
        Integer orderId = 123;
        String token = qrCodeService.generateOrderToken(orderId);

        assertNotNull(token);
        assertEquals(64, token.length(), "SHA-256 hex string should be 64 characters long");
        assertTrue(token.matches("^[0-9a-f]{64}$"), "Token should contain valid lowercase hex characters");
    }

    @Test
    @DisplayName("Should correctly format QR content string as ORDER:{orderId}:{token}")
    void testGetQrContent() {
        Integer orderId = 42;
        String token = "abc123def456";
        String qrContent = qrCodeService.getQrContent(orderId, token);

        assertEquals("ORDER:42:abc123def456", qrContent);
    }

    @Test
    @DisplayName("Should generate non-empty PNG image bytes for QR code")
    void testGenerateQrCodePng() throws Exception {
        String content = "ORDER:1:sampletoken123";
        byte[] pngBytes = qrCodeService.generateQrCodePng(content, 200, 200);

        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 0, "PNG bytes should not be empty");

        // Verify PNG magic header bytes: 0x89 'P' 'N' 'G'
        assertEquals((byte) 0x89, pngBytes[0]);
        assertEquals((byte) 'P', pngBytes[1]);
        assertEquals((byte) 'N', pngBytes[2]);
        assertEquals((byte) 'G', pngBytes[3]);
    }
}
