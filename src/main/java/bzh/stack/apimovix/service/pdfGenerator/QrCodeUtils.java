package bzh.stack.apimovix.service.pdfGenerator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

/**
 * Classe utilitaire pour la génération de QR codes
 */
public class QrCodeUtils {

    /**
     * Génère un QR code pour une URL Google Maps
     */
    public static Image generateQrCode(String content, int size, float qrWidth) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0x00000000);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        Image qrCodeImage = new Image(ImageDataFactory.create(outputStream.toByteArray()));
        qrCodeImage.setWidth(qrWidth);
        qrCodeImage.setHeight(qrWidth);
        return qrCodeImage;
    }

    /**
     * Génère une URL Google Maps pour des coordonnées
     */
    public static String generateMapsUrl(double latitude, double longitude) {
        return String.format("https://www.google.com/maps?q=%f,%f", latitude, longitude);
    }
}
