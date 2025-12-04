package bzh.stack.apimovix.service.pdfGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.service.picture.PictureService;

/**
 * Générateur de PDF pour les étiquettes de colis
 */
@Component
public class PackageLabelGenerator {

    private static final float LABEL_WIDTH = 295;
    private static final float LABEL_HEIGHT = 421;

    private final PictureService pictureService;

    public PackageLabelGenerator(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    /**
     * Génère une étiquette PDF pour un colis
     */
    public byte[] generateLabel(PackageEntity packageEntity) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, new PageSize(LABEL_WIDTH, LABEL_HEIGHT));
        document.setMargins(0, 0, 0, 0);

        // Ajouter explicitement la première page
        pdf.addNewPage();

        PdfFont font = PdfFontFactory.createFont(StandardFonts.COURIER);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);

        // Récupérer le compte depuis le tour de la commande
        Account account = null;
        if (packageEntity.getCommand() != null && packageEntity.getCommand().getTour() != null) {
            account = packageEntity.getCommand().getTour().getAccount();
        }

        // Dessiner le logo
        PdfDrawingUtils.drawLogo(document, account, pictureService);

        // Dessiner le code-barres
        drawPackageBarcode(document, packageEntity, font);

        // Dessiner les sections
        PdfDrawingUtils.drawSquare(document, 10, 10, 10, 80);
        PdfDrawingUtils.drawSquare(document, 10, 100, 10, 130);
        PdfDrawingUtils.drawSquare(document, 10, 240, 10, 60);

        // Section expéditeur
        PdfDrawingUtils.drawFullTextInZone(document, 10, 10, 90, 16, "EXPÉDITEUR :", true, ColorConstants.BLACK, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 14, 34, 300, 16, packageEntity.getCommand().getSender().getName(), false, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 14, 50, 300, 16, packageEntity.getCommand().getSender().getFullAdr(), false, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 14, 66, 300, 16, packageEntity.getCommand().getSender().getFullCity(), false, null, false, font, fontBold);

        // Section destinataire
        PdfDrawingUtils.drawFullTextInZone(document, 10, 100, 105, 16, "DESTINATAIRE :", true, ColorConstants.BLACK, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 15, 115, LABEL_WIDTH - 30, 30, packageEntity.getCommand().getPharmacy().getName(), true, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 15, 145, LABEL_WIDTH - 30, 20, packageEntity.getCommand().getPharmacy().getFullAdr(), false, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 15, 170, LABEL_WIDTH - 30, 60, packageEntity.getCommand().getPharmacy().getFullCity(), true, null, false, font, fontBold);

        // Section informations complémentaires
        PdfDrawingUtils.drawFullTextInZone(document, 225, 240, 60, 60, packageEntity.getZoneName(), true, ColorConstants.BLACK, true, font, fontBold);

        String numTransport = packageEntity.getCNumTransport() == null ? "Aucun" : packageEntity.getCNumTransport();
        PdfDrawingUtils.drawText(document, 15, 242, LABEL_WIDTH - 85, 28, "N° prepa : " + numTransport, false, null, false, 12, font, fontBold);

        String transportName = account != null && account.getSociete() != null ? account.getSociete() : "";
        PdfDrawingUtils.drawText(document, 15, 270, LABEL_WIDTH - 85, 28, "Transport : " + transportName, false, null, false, 12, font, fontBold);

        // Section date et poids
        PdfDrawingUtils.drawText(document, 10, 302, LABEL_WIDTH - 80, 20,
            "Imprimé le " + LocalDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            false, null, false, 10, font, fontBold);

        PdfDrawingUtils.drawFullTextInZone(document, 225, 302, 60, 40, packageEntity.getWeight() + " KG", true, null, true, font, fontBold);
        PdfDrawingUtils.drawText(document, 10, 322, LABEL_WIDTH - 20, 20, "Bac n° " + packageEntity.getNum(), true, null, false, 10, font, fontBold);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Dessine le code-barres du colis
     */
    private void drawPackageBarcode(Document document, PackageEntity packageEntity, PdfFont font) throws IOException {
        Barcode128 barcode = new Barcode128(document.getPdfDocument());
        barcode.setCode(packageEntity.getBarcode());
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setFont(null);

        Image barcodeImage = new Image(barcode.createFormXObject(document.getPdfDocument()));

        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float margin = 10;
        float charSpacing = 7;

        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getFirstPage());
        canvas.setFontAndSize(font, 12);
        canvas.setFillColor(ColorConstants.BLACK);
        float textWidth = font.getWidth(packageEntity.getBarcode()) * 12 / 1000;
        float totalSpacing = charSpacing * (packageEntity.getBarcode().length() - 1);
        float textX = (pageWidth - (textWidth + totalSpacing)) / 2;

        canvas.setCharacterSpacing(charSpacing);
        canvas.beginText();
        canvas.moveText(textX, 1);
        canvas.showText(packageEntity.getBarcode());
        canvas.endText();
        canvas.setCharacterSpacing(0);

        barcodeImage.setWidth(pageWidth - (2 * margin));
        barcodeImage.setFixedPosition(margin, margin);

        document.add(barcodeImage);
    }
}
