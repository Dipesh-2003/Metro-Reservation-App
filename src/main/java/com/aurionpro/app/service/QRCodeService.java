package com.aurionpro.app.service;

public interface QRCodeService {
    /**
     * Generates a QR code from the given text and returns it as a Base64 encoded string.
     * @param text The text to encode in the QR code.
     * @return A Base64 string representing the QR code PNG image.
     */
    String generateQRCodeBase64(String text);
}