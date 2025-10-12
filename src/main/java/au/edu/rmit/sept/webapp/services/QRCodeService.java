package au.edu.rmit.sept.webapp.services;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Service
public class QRCodeService {

    // service to generate a QR code with ZXing library for any string URL
    public byte[] generateQRCodeImage(String text, int width, int height) {
        try {
            // use ZXing to generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            var bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            // write to byte array
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            // return qr as byte array
            return pngOutputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR Code", e);
        }
    }

}

