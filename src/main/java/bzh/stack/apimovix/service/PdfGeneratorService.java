package bzh.stack.apimovix.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.service.picture.PictureService;
import bzh.stack.apimovix.service.update.FileService;

@Service
public class PdfGeneratorService {

    @Autowired
    private PictureService pictureService;
    @Autowired
    private ORSService orsService;
    @Autowired
    private FileService fileService;

    private static final float LABEL_WIDTH = 295;
    private static final float LABEL_HEIGHT = 421;

    /**
     * Convertit une image en noir et blanc (niveaux de gris) en préservant la transparence
     */
    private byte[] convertToGrayscale(byte[] imageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));

        // Vérifier si l'image a de la transparence
        boolean hasAlpha = originalImage.getColorModel().hasAlpha();

        BufferedImage grayscaleImage;
        if (hasAlpha) {
            // Utiliser TYPE_BYTE_GRAY avec canal alpha pour préserver la transparence
            grayscaleImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
            );

            // Convertir en niveaux de gris pixel par pixel en préservant l'alpha
            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    int rgb = originalImage.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    // Formule de luminance pour conversion en niveaux de gris
                    int gray = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                    // Reconstruire le pixel avec le niveau de gris et l'alpha original
                    int grayRgb = (alpha << 24) | (gray << 16) | (gray << 8) | gray;
                    grayscaleImage.setRGB(x, y, grayRgb);
                }
            }
        } else {
            // Pas de transparence, conversion simple
            grayscaleImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
            );

            java.awt.Graphics2D g2d = grayscaleImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(grayscaleImage, "png", baos);
        return baos.toByteArray();
    }

    public void drawLogo(Document document, Account account) throws IOException {
        // Ne dessiner le logo que si le compte a un logo défini
        if (account == null || account.getLogo() == null) {
            return;
        }

        File file = pictureService.findImageFile(account.getLogo().getName());

        if (file != null && file.exists()) {
            byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());
            // Convertir l'image en noir et blanc
            byte[] grayscaleBytes = convertToGrayscale(imageBytes);
            Image logo = new Image(ImageDataFactory.create(grayscaleBytes));

            // Forcer la hauteur à exactement 30px en gardant le ratio
            float targetHeight = 40;
            float ratio = logo.getImageWidth() / logo.getImageHeight();
            float targetWidth = targetHeight * ratio;
            logo.scaleAbsolute(targetWidth, targetHeight);

            // Positionner le logo en haut à droite avec une marge de 20px
            float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
            float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight();
            float margin = 15;

            // setFixedPosition positionne le coin inférieur gauche de l'image
            // Pour avoir le coin supérieur droit du logo à 20px du coin supérieur droit de la page :
            float x = pageWidth - targetWidth - margin;
            float y = pageHeight - targetHeight - margin;

            logo.setFixedPosition(x, y);
            document.add(logo);
        }
    }

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

    private void drawSquare(Document document, float x, float y, float width, float height) {
        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());
        canvas.setStrokeColor(ColorConstants.BLACK);
        canvas.setLineWidth(2);
        canvas.rectangle(x, document.getPdfDocument().getDefaultPageSize().getHeight() - y - height,
                document.getPdfDocument().getDefaultPageSize().getWidth() - width - x, height);
        canvas.stroke();
    }

    private void drawFullTextInZone(Document document, float x, float y, float width, float height, String text,
            Boolean bolded, Color bg, Boolean centred, PdfFont font, PdfFont fontBold) {
        if (text == null) {
            text = "";
        }
        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());

        PdfFont selectedFont = bolded ? fontBold : font;

        if (bg != null) {
            canvas.setFillColor(bg);
            canvas.rectangle(x, document.getPdfDocument().getDefaultPageSize().getHeight() - y - height, width, height);
            canvas.fill();
        }

        float maxFontSize = 100;
        float minFontSize = 1;
        float currentFontSize = maxFontSize;
        float lineHeight = 1.2f;

        while (currentFontSize > minFontSize) {
            String[] words = text.split(" ");
            float maxLineWidth = 0;
            int totalLines = 1;
            float currentLineWidth = 0;

            for (String word : words) {
                float wordWidth = selectedFont.getWidth(word + " ") * currentFontSize / 1000;
                if (currentLineWidth + wordWidth > width) {
                    totalLines++;
                    currentLineWidth = wordWidth;
                    maxLineWidth = Math.max(maxLineWidth, currentLineWidth);
                } else {
                    currentLineWidth += wordWidth;
                    maxLineWidth = Math.max(maxLineWidth, currentLineWidth);
                }
            }

            float totalHeight = totalLines * currentFontSize * lineHeight;

            if (maxLineWidth <= width && totalHeight <= height) {
                break;
            }

            currentFontSize -= 1;
        }

        canvas.setFontAndSize(selectedFont, currentFontSize);
        canvas.setFillColor(ColorConstants.BLACK);
        if (bg == ColorConstants.BLACK) {
            canvas.setFillColor(ColorConstants.WHITE);
        }

        String[] words = text.split(" ");
        float currentX = x;
        float currentY = document.getPdfDocument().getDefaultPageSize().getHeight() - y - currentFontSize;
        float lineSpacing = currentFontSize * lineHeight;

        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            float lineWidth = selectedFont.getWidth(testLine) * currentFontSize / 1000;

            if (lineWidth <= width) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        if (centred) {
            float totalTextHeight = lines.size() * lineSpacing;
            float startY = document.getPdfDocument().getDefaultPageSize().getHeight() - y - height
                    + (height - totalTextHeight) / 2 + (lines.size() - 1) * lineSpacing
                    + (currentFontSize / 3);

            for (String line : lines) {
                float lineWidth = selectedFont.getWidth(line) * currentFontSize / 1000;
                float lineX = x + (width - lineWidth) / 2;

                canvas.beginText();
                canvas.moveText(lineX, startY);
                canvas.showText(line);
                canvas.endText();

                startY -= lineSpacing;
            }
        } else {
            for (String line : lines) {
                canvas.beginText();
                canvas.moveText(currentX, currentY);
                canvas.showText(line);
                canvas.endText();

                currentY -= lineSpacing;
            }
        }
    }

    private void drawText(Document document, float x, float y, float width, float height, String text, Boolean bolded,
            Color bg, Boolean centred, float fontSize, PdfFont font, PdfFont fontBold) {
        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());

        PdfFont selectedFont = bolded ? fontBold : font;

        if (bg != null) {
            canvas.setFillColor(bg);
            canvas.rectangle(x, document.getPdfDocument().getDefaultPageSize().getHeight() - y - height, width, height);
            canvas.fill();
        }

        float lineHeight = 1.2f;

        canvas.setFontAndSize(selectedFont, fontSize);
        canvas.setFillColor(ColorConstants.BLACK);
        if (bg == ColorConstants.BLACK) {
            canvas.setFillColor(ColorConstants.WHITE);
        }

        String[] words = text.split(" ");
        float currentX = x;
        float currentY = document.getPdfDocument().getDefaultPageSize().getHeight() - y - fontSize;
        float lineSpacing = fontSize * lineHeight;

        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            float lineWidth = selectedFont.getWidth(testLine) * fontSize / 1000;

            if (lineWidth <= width) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        if (centred) {
            float totalTextHeight = lines.size() * lineSpacing;
            float startY = document.getPdfDocument().getDefaultPageSize().getHeight() - y - height
                    + (height - totalTextHeight) / 2 + (lines.size() - 1) * lineSpacing
                    + (fontSize / 3);

            for (String line : lines) {
                float lineWidth = selectedFont.getWidth(line) * fontSize / 1000;
                float lineX = x + (width - lineWidth) / 2;

                canvas.beginText();
                canvas.moveText(lineX, startY);
                canvas.showText(line);
                canvas.endText();

                startY -= lineSpacing;
            }
        } else {
            for (String line : lines) {
                canvas.beginText();
                canvas.moveText(currentX, currentY);
                canvas.showText(line);
                canvas.endText();

                currentY -= lineSpacing;
            }
        }
    }

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

        drawLogo(document, account);
        drawPackageBarcode(document, packageEntity, font);

        drawSquare(document, 10, 10, 10, 80);
        drawSquare(document, 10, 100, 10, 130);
        drawSquare(document, 10, 240, 10, 60);

        drawFullTextInZone(document, 10, 10, 90, 16, "EXPÉDITEUR :", true, ColorConstants.BLACK, false, font, fontBold);

        drawFullTextInZone(document, 14, 34, 300, 16, packageEntity.getCommand().getSender().getName(), false, null,
                false, font, fontBold);
        drawFullTextInZone(document, 14, 50, 300, 16, packageEntity.getCommand().getSender().getFullAdr(), false, null,
                false, font, fontBold);
        drawFullTextInZone(document, 14, 66, 300, 16, packageEntity.getCommand().getSender().getFullCity(), false, null,
                false, font, fontBold);

        drawFullTextInZone(document, 10, 100, 105, 16, "DESTINATAIRE :", true, ColorConstants.BLACK, false, font,
                fontBold);
        drawFullTextInZone(document, 15, 115, LABEL_WIDTH - 30, 30, packageEntity.getCommand().getPharmacy().getName(),
                true,
                null, false, font, fontBold);
        drawFullTextInZone(document, 15, 145, LABEL_WIDTH - 30, 20,
                packageEntity.getCommand().getPharmacy().getFullAdr(), false,
                null, false, font, fontBold);
        drawFullTextInZone(document, 15, 170, LABEL_WIDTH - 30, 60,
                packageEntity.getCommand().getPharmacy().getFullCity(), true,
                null, false, font, fontBold);

        drawFullTextInZone(document, 225, 240, 60, 60, packageEntity.getZoneName(), true, ColorConstants.BLACK, true,
                font, fontBold);
        String numTransport = packageEntity.getCNumTransport() == null ? "Aucun" : packageEntity.getCNumTransport();
        drawText(document, 15, 242, LABEL_WIDTH - 85, 28, "N° prepa : " + numTransport, false, null, false,
                12, font, fontBold);
        String transportName = account != null && account.getSociete() != null ? account.getSociete() : "";
        drawText(document, 15, 270, LABEL_WIDTH - 85, 28, "Transport : " + transportName, false, null, false, 12, font,
                fontBold);

        drawText(document, 10, 302, LABEL_WIDTH - 80, 20, "Imprimé le " + LocalDateTime.now(ZoneId.of("Europe/Paris"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), false, null, false, 10, font, fontBold);
        drawFullTextInZone(document, 225, 302, 60, 40, packageEntity.getWeight() + " KG", true, null, true, font,
                fontBold);
        drawText(document, 10, 322, LABEL_WIDTH - 20, 20, "Bac n° " + packageEntity.getNum(), true, null, false, 10,
                font, fontBold);

        document.close();
        return baos.toByteArray();
    }

    private Image generateQrCode(String content, int size, float qrWidth) throws WriterException, IOException {
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

    public void drawCommand(Document document, Command command, float y, float height, PdfFont font, PdfFont fontBold) {
        float x = 20;
        float qrWidth = 70;
        drawSquare(document, x, y, 20, height);
        if (command.getNewPharmacy()) {
            drawFullTextInZone(document, x + 5, y + 5, 30, 12, "NEW", true, null, false, font, fontBold);
            drawFullTextInZone(document, x + 35, y + 5,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth - 30, 15,
                    command.getPharmacy().getName(), true, null, false, font, fontBold);
        } else {
            drawFullTextInZone(document, x + 5, y + 5,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 15,
                    command.getPharmacy().getName(), true, null, false, font, fontBold);
        }
        drawFullTextInZone(document, x + 5, y + 20,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 12,
                command.getPharmacy().getFullAdr(), false, null, false, font, fontBold);
        drawFullTextInZone(document, x + 5, y + 32,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 12,
                command.getPharmacy().getFullCity(), false, null, false, font, fontBold);
        String informations = command.getPharmacy().getInformations();
        if (informations != null && !informations.isEmpty()) {
            drawText(document, x + 5, y + 46,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 50,
                    "Informations : " + informations.strip(), false, null, false, 10, font,
                    fontBold);
        }

        double totalWeight = command.getPackages().stream()
                .mapToDouble(pkg -> pkg.getWeight() != null ? pkg.getWeight() : 0.0)
                .sum();
        float itemSize = (document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2) / 4;
        drawFullTextInZone(document, x + 5, y + 100, itemSize, 12,
                "Colis : " + command.getPackages().size() + " (" + String.format("%.1f", totalWeight) + "kg)", false,
                null, false, font, fontBold);
        drawFullTextInZone(document, x + 5 + itemSize * 2, y + 100, itemSize, 12,
                "CIP : " + command.getPharmacy().getCip(), false, null, false, font, fontBold);
        drawFullTextInZone(document, x + 5 + itemSize * 3, y + 100, itemSize, 12, "DATE : _____________", false, null,
                false, font, fontBold);

        try {
            String mapsUrl = String.format("https://www.google.com/maps?q=%f,%f",
                    command.getPharmacy().getLatitude(),
                    command.getPharmacy().getLongitude());

            Image qrCodeImage = generateQrCode(mapsUrl, 200, qrWidth);

            int currentPage = document.getPdfDocument().getNumberOfPages();
            qrCodeImage.setFixedPosition(currentPage,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - x - qrWidth,
                    document.getPdfDocument().getDefaultPageSize().getHeight() - y - qrWidth);
            document.add(qrCodeImage);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

    private void drawPageNumber(Document document, int pageNumber, int totalPages, PdfFont font) throws IOException {
        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getPage(pageNumber));
        canvas.setFontAndSize(font, 12);
        canvas.setFillColor(ColorConstants.BLACK);

        String pageText = "Page " + pageNumber + "/" + totalPages;
        float textWidth = font.getWidth(pageText) * 12 / 1000;
        float x = document.getPdfDocument().getDefaultPageSize().getWidth() - textWidth - 20;
        float y = document.getPdfDocument().getDefaultPageSize().getHeight() - 20;

        canvas.beginText();
        canvas.moveText(x, y);
        canvas.showText(pageText);
        canvas.endText();
    }

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

        drawTourBarcode(document, tour, font);
        drawFullTextInZone(document, 20, 10, 130, 20, tour.getName(), true, null, false, font, fontBold);
        drawFullTextInZone(document, 20, 30, 130, 10, tour.getFormattedDate(), false, null, false, font, fontBold);
        String assigned = tour.getProfil() != null ? "Assigné à " + tour.getProfil().getFullName() : "Non assignée";
        drawFullTextInZone(document, 20, 40, 130, 10, assigned, false, null, false, font, fontBold);

        float margin = 10;
        float y = 65;
        float height = 120;
        int commandCount = 0;
        int totalPages = (int) Math.ceil(tour.getCommands().size() / 6.0);

        drawPageNumber(document, 1, totalPages, font);

        for (Command command : tour.getCommands()) {
            if (commandCount % 6 == 0 && commandCount > 0) {
                pdf.addNewPage();
                y = 50;
                drawPageNumber(document, pdf.getNumberOfPages(), totalPages, font);
            }

            drawCommand(document, command, y, height, font, fontBold);
            y = y + height + margin;
            commandCount++;
        }

        document.close();
        return baos.toByteArray();
    }

    public byte[] generateTarifTourPdf(Tour tour, List<Tarif> tarifs) throws IOException {
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

        drawTourBarcode(document, tour, font);
        drawFullTextInZone(document, 20, 10, 130, 20, tour.getName(), true, null, false, font, fontBold);
        drawFullTextInZone(document, 20, 30, 130, 10, tour.getFormattedDate(), false, null, false, font, fontBold);
        String assigned = tour.getProfil() != null ? "Assigné à " + tour.getProfil().getFullName() : "Non assignée";
        drawFullTextInZone(document, 20, 40, 130, 10, assigned, false, null, false, font, fontBold);

        float margin = 10;
        float y = 65;
        float height = 80;
        int commandCount = 0;
        int totalPages = (int) Math.ceil(tour.getCommands().size() / 8.0) + 1; // +1 pour la page de résumé

        // Structures pour collecter les statistiques
        Map<Tarif, Integer> tarifCounts = new HashMap<>();
        double totalPrice = 0.0;
        double totalDistance = 0.0;
        int totalCommands = tour.getCommands().size();
        int commandsWithoutTarif = 0;

        drawPageNumber(document, 1, totalPages, font);

        for (Command command : tour.getCommands()) {
            Double distance = orsService.calculateCommandDistance(command).orElse(0.0);
            totalDistance += distance;

            Optional<Tarif> matchingTarif;
            if (command.getTarif() != null) {
                // Utiliser le tarif spécifique de la commande
                matchingTarif = Optional.empty(); // On n'utilise pas les tarifs de la liste
            } else {
                // Utiliser la logique de tarification basée sur la distance
                matchingTarif = tarifs.stream()
                        .filter(tarif -> distance <= tarif.getKmMax())
                        .min((t1, t2) -> Double.compare(t1.getKmMax(), t2.getKmMax()));
            }

            if (commandCount % 8 == 0 && commandCount > 0) {
                pdf.addNewPage();
                y = 50;
                drawPageNumber(document, pdf.getNumberOfPages(), totalPages, font);
            }

            drawCommandWithTarif(document, command, y, height, font, fontBold, distance, matchingTarif);

            // Collecter les statistiques
            if (command.getTarif() != null) {
                // Utiliser le tarif spécifique de la commande
                totalPrice += command.getTarif();
                // On ne compte pas dans les tarifs de la liste car c'est un tarif personnalisé
            } else if (matchingTarif.isPresent()) {
                Tarif tarif = matchingTarif.get();
                tarifCounts.put(tarif, tarifCounts.getOrDefault(tarif, 0) + 1);
                totalPrice += tarif.getPrixEuro();
            } else {
                commandsWithoutTarif++;
            }

            y = y + height + margin;
            commandCount++;
        }

        // Créer une page dédiée pour le résumé tarifaire
        pdf.addNewPage();
        addTarifSummary(document, tarifCounts, totalPrice, totalCommands, commandsWithoutTarif, totalDistance, tarifs,
                font, fontBold);

        document.close();
        return baos.toByteArray();
    }

    private void addTarifSummary(Document document, Map<Tarif, Integer> tarifCounts, double totalPrice,
            int totalCommands, int commandsWithoutTarif, double totalDistance, List<Tarif> tarifs, PdfFont font,
            PdfFont fontBold) throws IOException {
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float y = 50; // Position en haut de la page dédiée

        // Créer une police italique pour l'avertissement
        PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        // Rectangle de fond pour le résumé
        drawSquare(document, 20, y, 20, 215);

        // Titre du résumé
        drawFullTextInZone(document, 25, y + 5, pageWidth - 50, 20, "RÉSUMÉ TARIFAIRE", true, ColorConstants.LIGHT_GRAY,
                true, font, fontBold);

        // Informations générales
        y += 30;
        drawText(document, 25, y, pageWidth - 50, 15, "Nombre total de commandes : " + totalCommands, false, null,
                false, 12, font, fontBold);
        y += 20;
        drawText(document, 25, y, pageWidth - 50, 15, "Distance totale : " + String.format("%.1f km", totalDistance),
                false, null, false, 12, font, fontBold);
        y += 20;
        drawText(document, 25, y, pageWidth - 50, 15, "Prix total : " + String.format("%.2f €", totalPrice), true, null,
                false, 14, font, fontBold);
        y += 20;

        if (commandsWithoutTarif > 0) {
            drawText(document, 25, y, pageWidth - 50, 15, "Commandes sans tarif défini : " + commandsWithoutTarif,
                    false, null, false, 12, font, fontBold);
            y += 20;
        }

        // Détail par catégorie de tarif
        if (!tarifCounts.isEmpty()) {
            drawText(document, 25, y, pageWidth - 50, 15, "Détail par catégorie :", true, null, false, 12, font,
                    fontBold);
            y += 20;

            // Afficher les tarifs dans l'ordre de la liste originale
            for (Tarif tarif : tarifs) {
                if (tarifCounts.containsKey(tarif)) {
                    int count = tarifCounts.get(tarif);
                    double subtotal = tarif.getPrixEuro() * count;

                    String tarifInfo = String.format("• Jusqu'à %.1f km (%.2f €) : %d commande(s) = %.2f €",
                            tarif.getKmMax(), tarif.getPrixEuro(), count, subtotal);
                    drawText(document, 30, y, pageWidth - 60, 15, tarifInfo, false, null, false, 11, font, fontBold);
                    y += 15;
                }
            }
        }

        // Avertissement
        y += 10;
        drawText(document, 25, y, pageWidth - 50, 15,
                "Ce document ne contient pas la TVA et ne constitue pas une facture.", false, null, false, 10,
                fontItalic, fontItalic);
        y += 15;
        drawText(document, 25, y, pageWidth - 50, 15,
                "Les montants affichés sont des estimations tarifaires uniquement.", false, null, false, 10, fontItalic,
                fontItalic);
        y += 15;
        drawText(document, 25, y, pageWidth - 50, 15,
                "Les distances sont calculées entre chaque pharmacie et la localisation du dépôt.", false, null, false,
                10, fontItalic, fontItalic);
    }

    public void drawCommandWithTarif(Document document, Command command, float y, float height, PdfFont font,
            PdfFont fontBold, Double distance, Optional<Tarif> matchingTarif) {
        float x = 20;
        float qrWidth = 70;
        drawSquare(document, x, y, 20, height);
        if (command.getNewPharmacy()) {
            drawFullTextInZone(document, x + 5, y + 5, 30, 12, "NEW", true, null, false, font, fontBold);
            drawFullTextInZone(document, x + 35, y + 5,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth - 30, 15,
                    command.getPharmacy().getName(), true, null, false, font, fontBold);
        } else {
            drawFullTextInZone(document, x + 5, y + 5,
                    document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 15,
                    command.getPharmacy().getName(), true, null, false, font, fontBold);
        }
        drawFullTextInZone(document, x + 5, y + 20,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 12,
                command.getPharmacy().getFullAdr(), false, null, false, font, fontBold);
        drawFullTextInZone(document, x + 5, y + 32,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 12,
                command.getPharmacy().getFullCity(), false, null, false, font, fontBold);

        String distanceText = String.format("Distance : %.1f km", distance);
        String tarifText;
        if (command.getTarif() != null) {
            tarifText = String.format("Tarif : %.2f €", command.getTarif());
        } else if (matchingTarif.isPresent()) {
            tarifText = String.format("Tarif : %.2f €", matchingTarif.get().getPrixEuro());
        } else {
            tarifText = "Tarif : Non défini";
        }
        drawText(document, x + 5, y + 46,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 50,
                distanceText + " | " + tarifText, false, null, false, 10, font, fontBold);

        double totalWeight = command.getPackages().stream()
                .mapToDouble(pkg -> pkg.getWeight() != null ? pkg.getWeight() : 0.0)
                .sum();
        float itemSize = (document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2) / 4;
        drawFullTextInZone(document, x + 5, y + height - 20, itemSize, 12,
                "Colis : " + command.getPackages().size() + " (" + String.format("%.1f", totalWeight) + "kg)", false,
                null, false, font, fontBold);
        drawFullTextInZone(document, x + 5 + itemSize * 3, y + height - 20, itemSize, 12,
                "CIP : " + command.getPharmacy().getCip(), false, null, false, font, fontBold);
    }

    /**
     * Génère un PDF d'anomalie en utilisant le template existant
     * 
     * @param anomalie L'anomalie pour laquelle générer le PDF
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generateAnomaliePdf(Anomalie anomalie) throws IOException {
        // Récupérer le template PDF
        File templateFile = fileService.findFile("pdf/template_anomalie.pdf");
        if (templateFile == null || !templateFile.exists()) {
            throw new IOException("Template PDF d'anomalie non trouvé");
        }

        // Créer un nouveau document basé sur le template
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(templateFile);
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(reader, writer);

        // Créer les polices
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Remplir les informations de l'anomalie sur la première page du template
        fillAnomalieDataOnTemplate(pdf, anomalie, font, fontBold);

        // Ajouter les images sur la première page si disponibles
        if (anomalie.getPictures() != null && !anomalie.getPictures().isEmpty()) {
            addAnomalieImagesToTemplate(pdf, anomalie, font, fontBold);
        }

        pdf.close();
        return baos.toByteArray();
    }

    /**
     * Remplit les données de l'anomalie directement sur le template PDF
     */
    private void fillAnomalieDataOnTemplate(PdfDocument pdf, Anomalie anomalie, PdfFont font, PdfFont fontBold)
            throws IOException {
        float pageWidth = pdf.getFirstPage().getPageSize().getWidth();
        float pageHeight = pdf.getFirstPage().getPageSize().getHeight();

        String dateHeure = anomalie.getCreatedAt() != null ? 
            anomalie.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
            LocalDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        drawTextOnPage(pdf.getFirstPage(), 175, 217, pageWidth - 62, 15, dateHeure, false, null, false, 14, font,
                fontBold);

        float x = 27;

        float y = pageHeight;
        switch (anomalie.getTypeAnomalie().getCode()) {
            case "excu_temp":
                y -= 252;
                break;
            case "c_dev":
                y -= 265;
                break;
            case "c_end":
                y -= 279;
                break;
            case "c_per":
                y -= 292;
                break;
            default:
                y -= 306;
                break;
        }

        drawTextOnPage(pdf.getFirstPage(), x, pageHeight - y, 10, 15, "*", true, null, false, 20, font, fontBold);

        if ("other".equals(anomalie.getTypeAnomalie().getCode()) && anomalie.getOther() != null) {
            drawTextOnPage(pdf.getFirstPage(), 80, pageHeight - y + 1, pageWidth - 28, 15, anomalie.getOther(), true,
                    null, false, 11, font, fontBold);
        }

        Integer freshFalseCount = (int) anomalie.getPackages().stream()
                .filter(pkg -> pkg.getFresh() != null && !pkg.getFresh())
                .count();
        Integer freshTrueCount = (int) anomalie.getPackages().stream()
                .filter(pkg -> pkg.getFresh() != null && pkg.getFresh())
                .count();

        if (freshFalseCount > 0) {
            drawTextOnPage(pdf.getFirstPage(), x, 348, 10, 15, "*", true, null, false, 20, font, fontBold);
            drawTextOnPage(pdf.getFirstPage(), 110, 350, pageWidth - 28, 15, Integer.toString(freshFalseCount), true,
                    null, false, 11, font,
                    fontBold);
        }

        if (freshTrueCount > 0) {
            drawTextOnPage(pdf.getFirstPage(), x, 388, 10, 15, "*", true, null, false, 20, font, fontBold);
            drawTextOnPage(pdf.getFirstPage(), 98, 389, pageWidth - 28, 15, Integer.toString(freshTrueCount), true,
                    null, false, 11, font,
                    fontBold);
        }

        if (anomalie.getPharmacy() != null) {
            String pharmacyInfo = anomalie.getPharmacy().getName() + " (" + anomalie.getPharmacy().getCip() + ")";
            drawTextOnPage(pdf.getFirstPage(), x, 460, pageWidth - x, 15, pharmacyInfo, false, null, false, 14, font,
                    fontBold);

            String address = anomalie.getPharmacy().getFullAdr();
            drawTextOnPage(pdf.getFirstPage(), x, 480, pageWidth - x, 15, address, false, null, false, 14, font,
                    fontBold);

            String city = anomalie.getPharmacy().getFullCity();
            drawTextOnPage(pdf.getFirstPage(), x, 500, pageWidth - x, 15, city, false, null, false, 14, font, fontBold);
        }

        if (anomalie.getActions() != null && !anomalie.getActions().trim().isEmpty()) {
            drawTextOnPage(pdf.getFirstPage(), x, 545, pageWidth - x, 20, anomalie.getActions(), false, null, false, 14,
                    font, fontBold);
        }

        drawTextOnPage(pdf.getFirstPage(), x, 630, 300, 15, dateHeure, false, null, false, 14, font, fontBold);

        if (anomalie.getProfil() != null) {
            String creatorName = anomalie.getProfil().getFullName();
            drawTextOnPage(pdf.getFirstPage(), 305, 630, pageWidth - 305, 15, creatorName, false, null, false, 14, font,
                    fontBold);
        }
    }

    /**
     * Ajoute les images de l'anomalie sur une nouvelle page du PDF
     */
    private void addAnomalieImagesToTemplate(PdfDocument pdf, Anomalie anomalie, PdfFont font, PdfFont fontBold)
            throws IOException {
        // Ajouter une nouvelle page pour les images
        pdf.addNewPage();
        com.itextpdf.kernel.pdf.PdfPage imagePage = pdf.getLastPage();
        
        float pageWidth = imagePage.getPageSize().getWidth();
        float pageHeight = imagePage.getPageSize().getHeight();

        // Titre pour les images
        float y = pageHeight - 50;
        drawTextOnPage(imagePage, 50, 10, pageWidth - 100, 15, "Images de l'anomalie :", true, null, false, 16,
                font, fontBold);
        y += 30;

        int imagesPerRow = 2;
        float imageWidth = (pageWidth - 120) / imagesPerRow;
        float imageHeight = imageWidth * 0.75f; // Ratio 4:3

        for (int i = 0; i < anomalie.getPictures().size(); i++) {
            // Vérifier si on doit passer à la ligne suivante
            if (i > 0 && i % imagesPerRow == 0) {
                y += imageHeight + 20;
            }
            
            // Vérifier si on doit créer une nouvelle page
            if (y + imageHeight > pageHeight - 100) {
                //pdf.addNewPage();
                imagePage = pdf.getLastPage();
                y = 50;
            }

            float x = 50 + (i % imagesPerRow) * (imageWidth + 20);

            try {
                File imageFile = pictureService.findImageFile(anomalie.getPictures().get(i).getName());
                if (imageFile != null && imageFile.exists()) {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
                    
                    // Ajouter l'image directement sur la page des images avec PdfCanvas
                    PdfCanvas canvas = new PdfCanvas(imagePage);
                    com.itextpdf.kernel.geom.Rectangle imageRect = new com.itextpdf.kernel.geom.Rectangle(x, pageHeight - y - imageHeight, imageWidth, imageHeight);
                    canvas.addImageFittedIntoRectangle(ImageDataFactory.create(imageBytes), imageRect, false);
                }
            } catch (Exception e) {
                drawTextOnPage(imagePage, x, y, imageWidth, 20, "Image non disponible", false, null, false, 10,
                        font, fontBold);
            }
        }
    }

    /**
     * Dessine du texte directement sur une page PDF
     */
    private void drawTextOnPage(com.itextpdf.kernel.pdf.PdfPage page, float x, float y, float width, float height,
            String text, Boolean bolded,
            Color bg, Boolean centred, float fontSize, PdfFont font, PdfFont fontBold) {
        PdfCanvas canvas = new PdfCanvas(page);

        PdfFont selectedFont = bolded ? fontBold : font;

        if (bg != null) {
            canvas.setFillColor(bg);
            canvas.rectangle(x, page.getPageSize().getHeight() - y - height, width, height);
            canvas.fill();
        }

        float lineHeight = 1.2f;

        canvas.setFontAndSize(selectedFont, fontSize);
        canvas.setFillColor(ColorConstants.RED);
        if (bg == ColorConstants.BLACK) {
            canvas.setFillColor(ColorConstants.WHITE);
        }

        String[] words = text.split(" ");
        float currentX = x;
        float currentY = page.getPageSize().getHeight() - y - fontSize;
        float lineSpacing = fontSize * lineHeight;

        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            float lineWidth = selectedFont.getWidth(testLine) * fontSize / 1000;

            if (lineWidth <= width) {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        if (centred) {
            float totalTextHeight = lines.size() * lineSpacing;
            float startY = page.getPageSize().getHeight() - y - height + (height - totalTextHeight) / 2
                    + (lines.size() - 1) * lineSpacing
                    + (fontSize / 3);

            for (String line : lines) {
                float lineWidth = selectedFont.getWidth(line) * fontSize / 1000;
                float lineX = x + (width - lineWidth) / 2;

                canvas.beginText();
                canvas.moveText(lineX, startY);
                canvas.showText(line);
                canvas.endText();

                startY -= lineSpacing;
            }
        } else {
            for (String line : lines) {
                canvas.beginText();
                canvas.moveText(currentX, currentY);
                canvas.showText(line);
                canvas.endText();

                currentY -= lineSpacing;
            }
        }
    }

    /**
     * Génère un PDF d'étiquette avec le logo de l'account, le nom de la pharmacie et le CIP en code-barres
     * Format adapté pour imprimante d'étiquettes (62x100mm)
     *
     * @param pharmacy La pharmacie pour laquelle générer le label
     * @param account Le compte dont on veut utiliser le logo (optionnel, utilise le logo par défaut si null)
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generatePharmacyLabel(Pharmacy pharmacy, Account account) throws IOException {
        // Format A4
        float pageHeight = PageSize.A4.getHeight(); // 842 points

        // Dimensions du label (41x89mm)
        float labelHeight = 116.220f;  // 89mm en points
        float labelWidth = 252.283f;   // 41mm en points

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(0, 0, 0, 0);

        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        PdfCanvas canvas = new PdfCanvas(pdf.addNewPage());

        // Position du label : coin en haut à gauche
        float labelStartX = 0;
        float labelStartY = pageHeight - labelHeight;

        float margin = 5;
        float yPosition = labelHeight;

        // Section 1: Logo centré en haut du label (seulement si le compte a un logo)
        float logoSectionHeight = 40;
        try {
            // Ne dessiner le logo que si le compte a un logo défini
            if (account != null && account.getLogo() != null) {
                File logoFile = pictureService.findImageFile(account.getLogo().getName());

                if (logoFile != null && logoFile.exists()) {
                byte[] imageBytes = java.nio.file.Files.readAllBytes(logoFile.toPath());
                // Convertir l'image en noir et blanc
                byte[] grayscaleBytes = convertToGrayscale(imageBytes);
                Image logo = new Image(ImageDataFactory.create(grayscaleBytes));

                // Limiter la hauteur à 30px maximum en gardant le ratio
                logo.setAutoScale(true);
                logo.scaleToFit(140, 30);

                System.out.println("=== DEBUG PHARMACY LABEL LOGO ===");
                System.out.println("Label width: " + labelWidth);
                System.out.println("Label height: " + labelHeight);
                System.out.println("Label start X: " + labelStartX);
                System.out.println("Label start Y: " + labelStartY);
                System.out.println("Image scaled width: " + logo.getImageScaledWidth());
                System.out.println("Image scaled height: " + logo.getImageScaledHeight());

                // Centrer horizontalement dans le label, positionné en haut à gauche de la page A4
                float logoX = labelStartX + (labelWidth - logo.getImageScaledWidth()) / 2;
                float logoY = labelStartY + labelHeight - logo.getImageScaledHeight() - 3;

                System.out.println("Logo X position: " + logoX);
                System.out.println("Logo Y position: " + logoY);
                System.out.println("=================================");

                logo.setFixedPosition(logoX, logoY);
                document.add(logo);
                }
            }
        } catch (Exception e) {
            // Si le logo ne peut pas être chargé, on continue sans
        }
        yPosition -= logoSectionHeight;

        // Section 2: Nom de la pharmacie (zone réduite)
        String pharmacyName = pharmacy.getName();
        float maxTextWidth = labelWidth - (2 * margin);
        float maxNameSectionHeight = 40;

        // Police adaptée au nouveau format
        float fontSize = 16;
        float textWidth = fontBold.getWidth(pharmacyName) * fontSize / 1000;

        // Réduire si nécessaire
        while (textWidth > maxTextWidth && fontSize > 12) {
            fontSize -= 0.5f;
            textWidth = fontBold.getWidth(pharmacyName) * fontSize / 1000;
        }

        canvas.setFillColor(ColorConstants.BLACK);

        // Si le texte est trop long, le découper sur plusieurs lignes
        if (textWidth > maxTextWidth) {
            String[] words = pharmacyName.split(" ");
            StringBuilder currentLine = new StringBuilder();
            float lineHeight = fontSize * 1.15f;
            java.util.List<String> lines = new java.util.ArrayList<>();

            for (String word : words) {
                String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
                float testWidth = fontBold.getWidth(testLine) * fontSize / 1000;

                if (testWidth > maxTextWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }

            // Centrer verticalement le bloc de texte
            float totalTextHeight = lines.size() * lineHeight;
            float textStartY = labelStartY + yPosition - (maxNameSectionHeight - totalTextHeight) / 2;

            // Dessiner chaque ligne centrée dans le label
            for (String line : lines) {
                float lineWidth = fontBold.getWidth(line) * fontSize / 1000;
                float lineX = labelStartX + (labelWidth - lineWidth) / 2;
                canvas.setFontAndSize(fontBold, fontSize);
                canvas.beginText();
                canvas.moveText(lineX, textStartY);
                canvas.showText(line);
                canvas.endText();
                textStartY -= lineHeight;
            }
        } else {
            // Dessiner sur une seule ligne, parfaitement centré dans le label
            float textX = labelStartX + (labelWidth - textWidth) / 2;
            float textY = labelStartY + yPosition - (maxNameSectionHeight - fontSize) / 2;
            canvas.setFontAndSize(fontBold, fontSize);
            canvas.beginText();
            canvas.moveText(textX, textY);
            canvas.showText(pharmacyName);
            canvas.endText();
        }
        yPosition -= maxNameSectionHeight;

        // Section 3: Code-barres adapté au nouveau format
        float barcodeMarginLR = 10; // Marges gauche et droite réduites
        float barcodeMarginBottom = 6; // Marge en bas réduite

        Barcode128 barcode = new Barcode128(pdf);
        barcode.setCode(pharmacy.getCip());
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setFont(null);

        // Adapter la hauteur au nouveau format plus petit
        barcode.setBarHeight(15); // Hauteur des barres réduite
        barcode.setX(0.75f); // Largeur des barres optimisée

        Image barcodeImage = new Image(barcode.createFormXObject(pdf));
        float barcodeWidth = labelWidth - (2 * barcodeMarginLR);
        barcodeImage.setWidth(barcodeWidth);
        barcodeImage.setAutoScale(false);

        // Positionner avec marges à gauche, droite et bas dans le label
        float barcodeX = labelStartX + barcodeMarginLR;
        float barcodeY = labelStartY + barcodeMarginBottom;
        barcodeImage.setFixedPosition(barcodeX, barcodeY);
        document.add(barcodeImage);

        document.close();
        return baos.toByteArray();
    }
}