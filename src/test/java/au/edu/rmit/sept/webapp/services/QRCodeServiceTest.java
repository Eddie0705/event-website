package au.edu.rmit.sept.webapp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {

    private QRCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QRCodeService();
    }

    // test for generating qr code image
    @Test
    void generateQRCodeImage_returnsNonEmptyByteArray() {
        byte[] result = qrCodeService.generateQRCodeImage("https://rmit.edu.au", 200, 200);

        assertNotNull(result);
        assertTrue(result.length > 0, "QR code byte array should not be empty");
    }

    // test for invalid input
    @Test
    void generateQRCodeImage_throwsOnInvalidInput() {
        assertThrows(RuntimeException.class, () ->
            qrCodeService.generateQRCodeImage(null, 200, 200)
        );
    }

    // test for invalid dimensions
    @Test
    void generateQRCodeImage_throwsOnInvalidDimensions() {
        assertThrows(RuntimeException.class, () ->
            qrCodeService.generateQRCodeImage("text", -50, -50)
        );
    }
}
