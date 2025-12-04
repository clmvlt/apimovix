package bzh.stack.apimovix.service.pdfGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.service.picture.PictureService;

/**
 * Générateur de PDF pour les étiquettes de pharmacie
 */
@Component
public class PharmacyLabelGenerator {

    private final PictureService pictureService;

    public PharmacyLabelGenerator(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    /**
     * Génère un PDF d'étiquette avec le logo de l'account, le nom de la pharmacie et le CIP en code-barres
     * Format adapté pour imprimante d'étiquettes (89x41mm)
     *
     * @param pharmacy La pharmacie pour laquelle générer le label
     * @param account Le compte dont on veut utiliser le logo (optionnel, utilise le logo par défaut si null)
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generatePharmacyLabel(Pharmacy pharmacy, Account account) throws IOException {
        // ========== CONFIGURATION - TOUTES LES VALEURS AJUSTABLES ==========

        // Dimensions du label (89x41mm)
        float labelWidth = 252.283f;   // 89mm en points
        float labelHeight = 116.220f;  // 41mm en points

        // Marges globales
        float globalMargin = 1f;

        // Configuration du LOGO
        float logoMaxWidth = labelWidth - (2 * globalMargin);  // Largeur max du logo
        float logoMaxHeight = 50f;          // Hauteur max du logo
        float logoMarginTop = globalMargin; // Marge en haut du logo

        // Configuration du NOM DE PHARMACIE
        float namesSectionHeight = 30f;     // Hauteur totale de la section nom
        float namesPositionX = globalMargin; // Position X du texte
        float namesMaxWidth = labelWidth - (2 * globalMargin); // Largeur max du texte

        // Configuration du CODE-BARRES
        float barcodeBarHeight = 12f;       // Hauteur des barres du code-barres
        float barcodeBarWidth = 0.5f;       // Épaisseur des barres
        float barcodeWidth = (labelWidth - 20) / 2; // Largeur totale du code-barres
        float barcodeMarginBottom = 4f;     // Marge en bas du code-barres

        // ========== FIN CONFIGURATION ==========

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, new PageSize(labelWidth, labelHeight));
        document.setMargins(0, 0, 0, 0);

        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        pdf.addNewPage();

        // Section 1: Logo centré en haut du label
        try {
            if (account != null && account.getLogo() != null) {
                File logoFile = pictureService.findImageFile(account.getLogo().getName());

                if (logoFile != null && logoFile.exists()) {
                    byte[] imageBytes = java.nio.file.Files.readAllBytes(logoFile.toPath());
                    byte[] grayscaleBytes = PdfDrawingUtils.convertToGrayscale(imageBytes);
                    Image logo = new Image(ImageDataFactory.create(grayscaleBytes));

                    // Redimensionner le logo pour qu'il rentre dans les dimensions max
                    logo.scaleToFit(logoMaxWidth, logoMaxHeight);

                    // Calculer la position Y (verticale)
                    float logoY = labelHeight - logo.getImageScaledHeight() - logoMarginTop;

                    // Calculer la position X pour un centrage parfait
                    // On s'assure que le logo est centré en utilisant la largeur réelle après scaling
                    float logoActualWidth = logo.getImageScaledWidth();
                    float logoX = (labelWidth - logoActualWidth) / 2;

                    // Appliquer le centrage horizontal et la position
                    logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    logo.setFixedPosition(logoX, logoY);
                    document.add(logo);
                }
            }
        } catch (Exception e) {
            // Si le logo ne peut pas être chargé, on continue sans
        }

        // Section 2: Nom de la pharmacie
        String pharmacyName = pharmacy.getName();

        // Utiliser drawFullTextInZone pour gérer automatiquement le texte
        PdfDrawingUtils.drawFullTextInZone(
            document,
            namesPositionX,                         // x
            labelHeight - 65,                         // y
            namesMaxWidth,                          // width
            namesSectionHeight,                     // height
            pharmacyName,                           // text
            true,                                   // bolded
            null,                  // bg color
            true,                                   // centré
            font,                                   // font
            fontBold                                // fontBold
        );

        // Section 3: Code-barres
        Barcode128 barcode = new Barcode128(pdf);
        barcode.setCode(pharmacy.getCip());
        barcode.setCodeType(Barcode128.CODE128);
        barcode.setFont(null);
        barcode.setBarHeight(barcodeBarHeight);
        barcode.setX(barcodeBarWidth);

        Image barcodeImage = new Image(barcode.createFormXObject(pdf));
        barcodeImage.setWidth(barcodeWidth);
        barcodeImage.setAutoScale(false);

        // Centrer horizontalement, positionner en bas avec marge
        float barcodePositionX = (labelWidth - barcodeWidth) / 2;
        barcodeImage.setFixedPosition(barcodePositionX, barcodeMarginBottom);
        document.add(barcodeImage);

        document.close();
        return baos.toByteArray();
    }
}
