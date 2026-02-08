package bzh.stack.apimovix.service.pdfGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.service.picture.PictureService;
import bzh.stack.apimovix.service.update.FileService;

/**
 * Générateur de PDF pour les anomalies
 */
@Component
public class AnomaliePdfGenerator {

    private final PictureService pictureService;
    private final FileService fileService;

    public AnomaliePdfGenerator(PictureService pictureService, FileService fileService) {
        this.pictureService = pictureService;
        this.fileService = fileService;
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
        if (anomalie.getAccount().getId().toString().equals("6bce8203-058c-43d4-8c92-fc5cad90acc9")) {
            templateFile = fileService.findFile("pdf/template_anomalie_mezegel.pdf");
        } else if (anomalie.getAccount().getId().toString().equals("b73efc9c-1c52-4941-ba6f-93e1d881e399")) {
            templateFile = fileService.findFile("pdf/template_anomalie_mediane.pdf");
        }
        
        // Si pas de template, generer un PDF basique
        if (templateFile == null || !templateFile.exists()) {
            return generateBasicPdf(anomalie);
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

        // Ajouter les images sur une nouvelle page si disponibles
        if (anomalie.getPictures() != null && !anomalie.getPictures().isEmpty()) {
            addAnomalieImagesToTemplate(pdf, anomalie, font, fontBold);
        }

        pdf.close();
        return baos.toByteArray();
    }

    /**
     * Genere un PDF basique sans template avec toutes les informations de l'anomalie
     */
    private byte[] generateBasicPdf(Anomalie anomalie) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // Titre
        document.add(new Paragraph("FICHE D'ANOMALIE")
                .setFont(fontBold)
                .setFontSize(18)
                .setMarginBottom(20));

        // Date et heure
        String dateHeure = anomalie.getCreatedAt() != null
                ? anomalie.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : LocalDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        document.add(new Paragraph("Date : " + dateHeure).setFont(font).setFontSize(12));

        // Type d'anomalie
        if (anomalie.getTypeAnomalie() != null) {
            document.add(new Paragraph("Type d'anomalie : " + anomalie.getTypeAnomalie().getName())
                    .setFont(font).setFontSize(12));
        }

        // Autre (si type "other")
        if (anomalie.getTypeAnomalie() != null && "other".equals(anomalie.getTypeAnomalie().getCode())
                && anomalie.getOther() != null) {
            document.add(new Paragraph("Precision : " + anomalie.getOther())
                    .setFont(font).setFontSize(12));
        }

        // Comptage des colis
        if (anomalie.getPackages() != null && !anomalie.getPackages().isEmpty()) {
            int freshFalseCount = (int) anomalie.getPackages().stream()
                    .filter(pkg -> pkg.getFresh() != null && !pkg.getFresh())
                    .count();
            int freshTrueCount = (int) anomalie.getPackages().stream()
                    .filter(pkg -> pkg.getFresh() != null && pkg.getFresh())
                    .count();

            if (freshFalseCount > 0) {
                document.add(new Paragraph("Colis non frais : " + freshFalseCount)
                        .setFont(font).setFontSize(12));
            }
            if (freshTrueCount > 0) {
                document.add(new Paragraph("Colis frais : " + freshTrueCount)
                        .setFont(font).setFontSize(12));
            }
        }

        // Pharmacie
        if (anomalie.getPharmacy() != null) {
            document.add(new Paragraph("").setMarginTop(10));
            document.add(new Paragraph("PHARMACIE").setFont(fontBold).setFontSize(14));
            document.add(new Paragraph(anomalie.getPharmacy().getName() + " (" + anomalie.getPharmacy().getCip() + ")")
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph(anomalie.getPharmacy().getFullAdr())
                    .setFont(font).setFontSize(12));
            document.add(new Paragraph(anomalie.getPharmacy().getFullCity())
                    .setFont(font).setFontSize(12));
        }

        // Actions
        if (anomalie.getActions() != null && !anomalie.getActions().trim().isEmpty()) {
            document.add(new Paragraph("").setMarginTop(10));
            document.add(new Paragraph("ACTIONS").setFont(fontBold).setFontSize(14));
            document.add(new Paragraph(anomalie.getActions()).setFont(font).setFontSize(12));
        }

        // Createur
        if (anomalie.getProfil() != null) {
            document.add(new Paragraph("").setMarginTop(10));
            document.add(new Paragraph("Cree par : " + anomalie.getProfil().getFullName())
                    .setFont(font).setFontSize(12));
        }

        // Images
        if (anomalie.getPictures() != null && !anomalie.getPictures().isEmpty()) {
            document.add(new Paragraph("").setMarginTop(20));
            document.add(new Paragraph("IMAGES").setFont(fontBold).setFontSize(14));

            for (var picture : anomalie.getPictures()) {
                try {
                    File imageFile = pictureService.findImageFile(picture.getName());
                    if (imageFile != null && imageFile.exists()) {
                        byte[] imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
                        com.itextpdf.layout.element.Image img = new com.itextpdf.layout.element.Image(
                                ImageDataFactory.create(imageBytes));
                        img.setMaxWidth(400);
                        img.setMarginTop(10);
                        document.add(img);
                    }
                } catch (Exception e) {
                    document.add(new Paragraph("Image non disponible : " + picture.getName())
                            .setFont(font).setFontSize(10));
                }
            }
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * Remplit les données de l'anomalie directement sur le template PDF
     */
    private void fillAnomalieDataOnTemplate(PdfDocument pdf, Anomalie anomalie, PdfFont font, PdfFont fontBold)
            throws IOException {
        float pageWidth = pdf.getFirstPage().getPageSize().getWidth();
        float pageHeight = pdf.getFirstPage().getPageSize().getHeight();

        String dateHeure = anomalie.getCreatedAt() != null
                ? anomalie.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : LocalDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), 175, 217, pageWidth - 62, 15, dateHeure, false, null, false, 14, font,
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

        PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, pageHeight - y, 10, 15, "*", true, null, false, 20, font, fontBold);

        if ("other".equals(anomalie.getTypeAnomalie().getCode()) && anomalie.getOther() != null) {
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), 80, pageHeight - y + 1, pageWidth - 28, 15, anomalie.getOther(), true,
                    null, false, 11, font, fontBold);
        }

        Integer freshFalseCount = (int) anomalie.getPackages().stream()
                .filter(pkg -> pkg.getFresh() != null && !pkg.getFresh())
                .count();
        Integer freshTrueCount = (int) anomalie.getPackages().stream()
                .filter(pkg -> pkg.getFresh() != null && pkg.getFresh())
                .count();

        if (freshFalseCount > 0) {
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 348, 10, 15, "*", true, null, false, 20, font, fontBold);
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), 110, 350, pageWidth - 28, 15, Integer.toString(freshFalseCount), true,
                    null, false, 11, font, fontBold);
        }

        if (freshTrueCount > 0) {
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 388, 10, 15, "*", true, null, false, 20, font, fontBold);
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), 98, 389, pageWidth - 28, 15, Integer.toString(freshTrueCount), true,
                    null, false, 11, font, fontBold);
        }

        if (anomalie.getPharmacy() != null) {
            String pharmacyInfo = anomalie.getPharmacy().getName() + " (" + anomalie.getPharmacy().getCip() + ")";
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 460, pageWidth - x, 15, pharmacyInfo, false, null, false, 14, font,
                    fontBold);

            String address = anomalie.getPharmacy().getFullAdr();
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 480, pageWidth - x, 15, address, false, null, false, 14, font,
                    fontBold);

            String city = anomalie.getPharmacy().getFullCity();
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 500, pageWidth - x, 15, city, false, null, false, 14, font, fontBold);
        }

        if (anomalie.getActions() != null && !anomalie.getActions().trim().isEmpty()) {
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 545, pageWidth - x, 20, anomalie.getActions(), false, null, false, 14,
                    font, fontBold);
        }

        PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), x, 630, 300, 15, dateHeure, false, null, false, 14, font, fontBold);

        if (anomalie.getProfil() != null) {
            String creatorName = anomalie.getProfil().getFullName();
            PdfDrawingUtils.drawTextOnPage(pdf.getFirstPage(), 305, 630, pageWidth - 305, 15, creatorName, false, null, false, 14, font,
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
        PdfDrawingUtils.drawTextOnPage(imagePage, 50, 10, pageWidth - 100, 15, "Images de l'anomalie :", true, null, false, 16,
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
                    com.itextpdf.kernel.geom.Rectangle imageRect = new com.itextpdf.kernel.geom.Rectangle(x,
                            pageHeight - y - imageHeight, imageWidth, imageHeight);
                    canvas.addImageFittedIntoRectangle(ImageDataFactory.create(imageBytes), imageRect, false);
                }
            } catch (Exception e) {
                PdfDrawingUtils.drawTextOnPage(imagePage, x, y, imageWidth, 20, "Image non disponible", false, null, false, 10,
                        font, fontBold);
            }
        }
    }
}
