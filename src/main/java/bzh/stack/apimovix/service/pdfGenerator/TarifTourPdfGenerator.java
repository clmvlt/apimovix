package bzh.stack.apimovix.service.pdfGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.service.ORSService;

/**
 * Générateur de PDF pour les tournées avec tarification
 */
@Component
public class TarifTourPdfGenerator {

    private final ORSService orsService;

    public TarifTourPdfGenerator(ORSService orsService) {
        this.orsService = orsService;
    }

    /**
     * Génère un PDF de tournée avec tarification
     */
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

        // En-tête de la tournée
        drawTourBarcode(document, tour, font);
        PdfDrawingUtils.drawFullTextInZone(document, 20, 10, 130, 20, tour.getName(), true, null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, 20, 30, 130, 10, tour.getFormattedDate(), false, null, false, font, fontBold);

        String assigned = tour.getProfil() != null ? "Assigné à " + tour.getProfil().getFullName() : "Non assignée";
        PdfDrawingUtils.drawFullTextInZone(document, 20, 40, 130, 10, assigned, false, null, false, font, fontBold);

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

        PdfDrawingUtils.drawPageNumber(document, 1, totalPages, font);

        // Pre-calculer toutes les distances en parallele (evite les appels HTTP sequentiels)
        Map<UUID, Double> distanceCache = orsService.calculateCommandDistancesBatch(tour.getCommands());

        // Dessiner chaque commande avec tarif
        for (Command command : tour.getCommands()) {
            Double distance = distanceCache.getOrDefault(command.getId(), 0.0);
            totalDistance += distance;

            Optional<Tarif> matchingTarif;
            if (command.getTarif() != null) {
                // Utiliser le tarif spécifique de la commande
                matchingTarif = Optional.empty();
            } else {
                // Utiliser la logique de tarification basée sur la distance
                matchingTarif = tarifs.stream()
                        .filter(tarif -> distance <= tarif.getKmMax())
                        .min((t1, t2) -> Double.compare(t1.getKmMax(), t2.getKmMax()));
            }

            if (commandCount % 8 == 0 && commandCount > 0) {
                pdf.addNewPage();
                y = 50;
                PdfDrawingUtils.drawPageNumber(document, pdf.getNumberOfPages(), totalPages, font);
            }

            drawCommandWithTarif(document, command, y, height, font, fontBold, distance, matchingTarif);

            // Collecter les statistiques
            if (command.getTarif() != null) {
                // Utiliser le tarif spécifique de la commande
                totalPrice += command.getTarif();
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
     * Dessine une commande avec ses informations tarifaires
     */
    private void drawCommandWithTarif(Document document, Command command, float y, float height, PdfFont font,
            PdfFont fontBold, Double distance, Optional<Tarif> matchingTarif) {
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

        // Informations de distance et tarif
        String distanceText = String.format("Distance : %.1f km", distance);
        String tarifText;
        if (command.getTarif() != null) {
            tarifText = String.format("Tarif : %.2f €", command.getTarif());
        } else if (matchingTarif.isPresent()) {
            tarifText = String.format("Tarif : %.2f €", matchingTarif.get().getPrixEuro());
        } else {
            tarifText = "Tarif : Non défini";
        }
        PdfDrawingUtils.drawText(document, x + 5, y + 46,
                document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2 - qrWidth, 50,
                distanceText + " | " + tarifText, false, null, false, 10, font, fontBold);

        // Statistiques sur les colis
        double totalWeight = command.getPackages().stream()
                .mapToDouble(pkg -> pkg.getWeight() != null ? pkg.getWeight() : 0.0)
                .sum();
        float itemSize = (document.getPdfDocument().getDefaultPageSize().getWidth() - (x + 5) * 2) / 4;
        PdfDrawingUtils.drawFullTextInZone(document, x + 5, y + height - 20, itemSize, 12,
                "Colis : " + command.getPackages().size() + " (" + String.format("%.1f", totalWeight) + "kg)", false,
                null, false, font, fontBold);
        PdfDrawingUtils.drawFullTextInZone(document, x + 5 + itemSize * 3, y + height - 20, itemSize, 12,
                "CIP : " + command.getPharmacy().getCip(), false, null, false, font, fontBold);
    }

    /**
     * Ajoute une page de résumé tarifaire
     */
    private void addTarifSummary(Document document, Map<Tarif, Integer> tarifCounts, double totalPrice,
            int totalCommands, int commandsWithoutTarif, double totalDistance, List<Tarif> tarifs, PdfFont font,
            PdfFont fontBold) throws IOException {
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float y = 50;

        // Créer une police italique pour l'avertissement
        PdfFont fontItalic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        // Rectangle de fond pour le résumé
        PdfDrawingUtils.drawSquare(document, 20, y, 20, 215);

        // Titre du résumé
        PdfDrawingUtils.drawFullTextInZone(document, 25, y + 5, pageWidth - 50, 20, "RÉSUMÉ TARIFAIRE", true, ColorConstants.LIGHT_GRAY,
                true, font, fontBold);

        // Informations générales
        y += 30;
        PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15, "Nombre total de commandes : " + totalCommands, false, null,
                false, 12, font, fontBold);
        y += 20;
        PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15, "Distance totale : " + String.format("%.1f km", totalDistance),
                false, null, false, 12, font, fontBold);
        y += 20;
        PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15, "Prix total : " + String.format("%.2f €", totalPrice), true, null,
                false, 14, font, fontBold);
        y += 20;

        if (commandsWithoutTarif > 0) {
            PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15, "Commandes sans tarif défini : " + commandsWithoutTarif,
                    false, null, false, 12, font, fontBold);
            y += 20;
        }

        // Détail par catégorie de tarif
        if (!tarifCounts.isEmpty()) {
            PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15, "Détail par catégorie :", true, null, false, 12, font,
                    fontBold);
            y += 20;

            // Afficher les tarifs dans l'ordre de la liste originale
            for (Tarif tarif : tarifs) {
                if (tarifCounts.containsKey(tarif)) {
                    int count = tarifCounts.get(tarif);
                    double subtotal = tarif.getPrixEuro() * count;

                    String tarifInfo = String.format("• Jusqu'à %.1f km (%.2f €) : %d commande(s) = %.2f €",
                            tarif.getKmMax(), tarif.getPrixEuro(), count, subtotal);
                    PdfDrawingUtils.drawText(document, 30, y, pageWidth - 60, 15, tarifInfo, false, null, false, 11, font, fontBold);
                    y += 15;
                }
            }
        }

        // Avertissement
        y += 10;
        PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15,
                "Ce document ne contient pas la TVA et ne constitue pas une facture.", false, null, false, 10,
                fontItalic, fontItalic);
        y += 15;
        PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15,
                "Les montants affichés sont des estimations tarifaires uniquement.", false, null, false, 10, fontItalic,
                fontItalic);
        y += 15;
        PdfDrawingUtils.drawText(document, 25, y, pageWidth - 50, 15,
                "Les distances sont calculées entre chaque pharmacie et la localisation du dépôt.", false, null, false,
                10, fontItalic, fontItalic);
    }
}
