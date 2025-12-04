package bzh.stack.apimovix.service.pdfGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.google.zxing.WriterException;
import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Tour;

/**
 * Générateur de PDF pour les tournées
 */
@Component
public class TourPdfGenerator {

    /**
     * Génère un PDF de tournée
     */
    public byte[] generateTourPdf(Tour tour) throws IOException {
        float w = PageSize.A4.getWidth();
        float h = PageSize.A4.getHeight();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, new PageSize(w, h));
        document.setMargins(0, 0, 0, 0);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        pdf.addNewPage();

        // En-tête de la tournée
        drawTourBarcode(document, tour, font);
        PdfDrawingUtils.drawFullTextInZone(document, 20, 10, 130, 20, tour.getName(), true, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 20, 30, 130, 10, tour.getFormattedDate(), false, null, false, font, fontBold);

        String assigned = tour.getProfil() != null ? "Assigné à " + tour.getProfil().getFullName() : "Non assignée";
        PdfDrawingUtils.drawFullTextInZone(document, 20, 40, 130, 10, assigned, false, null, false, font, fontBold);

        float margin = 10;
        float y = 65;
        float height = 120;
        int commandCount = 0;
        int totalPages = (int) Math.ceil(tour.getCommands().size() / 6.0);

        PdfDrawingUtils.drawPageNumber(document, 1, totalPages, font);

        // Dessiner chaque commande
        for (Command command : tour.getCommands()) {
            if (commandCount % 6 == 0 && commandCount > 0) {
                pdf.addNewPage();
                y = 50;
                PdfDrawingUtils.drawPageNumber(document, pdf.getNumberOfPages(), totalPages, font);
            }

            drawCommand(document, command, y, height, font, fontBold);
            y = y + height + margin;
            commandCount++;
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * Dessine le code-barres de la tournée
     */
    private void drawTourBarcode(Document document, Tour tour, PdfFont font) throws IOException {
        Barcode128 barcode = new Barcode128(document.getPdfDocument());
        barcode.setCode(tour.getId());
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setFont(null);

        Image barcodeImage = new Image(barcode.createFormXObject(document.getPdfDocument()));

        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight();
        float margin = 160;
        float charSpacing = 0;

        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getFirstPage());
        canvas.setFontAndSize(font, 12);
        canvas.setFillColor(ColorConstants.BLACK);
        float textWidth = font.getWidth(tour.getId()) * 12 / 1000;
        float totalSpacing = charSpacing * (tour.getId().length() - 1);
        float textX = (pageWidth - (textWidth + totalSpacing)) / 2;

        canvas.setCharacterSpacing(charSpacing);
        canvas.beginText();
        canvas.moveText(textX, pageHeight - barcodeImage.getImageScaledHeight() - 20);
        canvas.showText(tour.getId());
        canvas.endText();
        canvas.setCharacterSpacing(0);

        barcodeImage.setWidth(pageWidth - (2 * margin));
        barcodeImage.setHeight(25);
        barcodeImage.setFixedPosition(margin, pageHeight - barcodeImage.getImageScaledHeight() - 8);

        document.add(barcodeImage);
    }

    /**
     * Dessine une commande sur le PDF
     */
    private void drawCommand(Document document, Command command, float y, float height, PdfFont font, PdfFont fontBold) {
        float x = 20;
        float qrWidth = 70;
        PdfDrawingUtils.drawSquare(document, x, y, 20, height);

        // Nom de la pharmacie avec indication "NEW" si nécessaire
        if (command.getNewPharmacy()) {
            PdfDrawingUtils.drawFullTextInZone(document, x + 5, y + 5, 30, 12, "NEW", true, null, false, font, fontBold);
            PdfDrawingUtils.drawFullTextInZone(document, x + 35, y + 5,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth - 30, 15,
                    command.getPharmacy().getName(), true, null, false, font, fontBold);
        } else {
            PdfDrawingUtils.drawFullTextInZone(document, x + 5, y + 5,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 15,
                    command.getPharmacy().getName(), true, null, false, font, fontBold);
        }

        // Adresse de la pharmacie
        PdfDrawingUtils.drawFullTextInZone(document, x + 5, y + 20,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 12,
                command.getPharmacy().getFullAdr(), false, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, x + 5, y + 32,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 12,
                command.getPharmacy().getFullCity(), false, null, false, font, fontBold);

        // Informations complémentaires
        String informations = command.getPharmacy().getInformations();
        if (informations != null && !informations.isEmpty()) {
            PdfDrawingUtils.drawText(document, x + 5, y + 46,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 50,
                    "Informations : " + informations.strip(), false, null, false, 10, font, fontBold);
        }

        // Statistiques sur les colis
        double totalWeight = command.getPackages().stream()
                .mapToDouble(pkg -> pkg.getWeight() != null ? pkg.getWeight() : 0.0)
                .sum();
        float itemSize = (document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2) / 4;
        PdfDrawingUtils.drawFullTextInZone(document, x + 5, y + 100, itemSize, 12,
                "Colis : " + command.getPackages().size() + " (" + String.format("%.1f", totalWeight) + "kg)", false,
                null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, x + 5 + itemSize * 2, y + 100, itemSize, 12,
                "CIP : " + command.getPharmacy().getCip(), false, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, x + 5 + itemSize * 3, y + 100, itemSize, 12, "DATE : _____________", false, null,
                false, font, fontBold);

        // QR Code pour localisation Google Maps
        try {
            String mapsUrl = QrCodeUtils.generateMapsUrl(
                    command.getPharmacy().getLatitude(),
                    command.getPharmacy().getLongitude());

            Image qrCodeImage = QrCodeUtils.generateQrCode(mapsUrl, 200, qrWidth);

            int currentPage = document.getPdfDocument().getNumberOfPages();
            qrCodeImage.setFixedPosition(currentPage,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - x - qrWidth,
                    document.getPdfDocument().getDefaultPageSize().getHeight() - y - qrWidth);
            document.add(qrCodeImage);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }
}
