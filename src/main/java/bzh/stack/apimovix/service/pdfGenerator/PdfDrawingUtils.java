package bzh.stack.apimovix.service.pdfGenerator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.service.picture.PictureService;

/**
 * Classe utilitaire contenant toutes les fonctions de dessin pour les PDF
 */
public class PdfDrawingUtils {

    /**
     * Convertit une image en noir et blanc (niveaux de gris) en préservant la transparence
     */
    public static byte[] convertToGrayscale(byte[] imageBytes) throws IOException {
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

    /**
     * Dessine le logo d'un compte en haut à droite du document
     */
    public static void drawLogo(Document document, Account account, PictureService pictureService) throws IOException {
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

            // Forcer la hauteur à exactement 40px en gardant le ratio
            float targetHeight = 40;
            float ratio = logo.getImageWidth() / logo.getImageHeight();
            float targetWidth = targetHeight * ratio;
            logo.scaleAbsolute(targetWidth, targetHeight);

            // Positionner le logo en haut à droite avec une marge de 15px
            float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
            float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight();
            float margin = 15;

            // setFixedPosition positionne le coin inférieur gauche de l'image
            // Pour avoir le coin supérieur droit du logo à 15px du coin supérieur droit de la page :
            float x = pageWidth - targetWidth - margin;
            float y = pageHeight - targetHeight - margin;

            logo.setFixedPosition(x, y);
            document.add(logo);
        }
    }

    /**
     * Dessine un carré (rectangle) sur le document
     */
    public static void drawSquare(Document document, float x, float y, float width, float height) {
        PdfCanvas canvas = new PdfCanvas(document.getPdfDocument().getLastPage());
        canvas.setStrokeColor(ColorConstants.BLACK);
        canvas.setLineWidth(2);
        canvas.rectangle(x, document.getPdfDocument().getDefaultPageSize().getHeight() - y - height,
                document.getPdfDocument().getDefaultPageSize().getWidth() - width - x, height);
        canvas.stroke();
    }

    /**
     * Dessine du texte dans une zone avec ajustement automatique de la taille de police
     */
    public static void drawFullTextInZone(Document document, float x, float y, float width, float height, String text,
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

    /**
     * Dessine du texte dans une zone avec une taille de police fixe
     */
    public static void drawText(Document document, float x, float y, float width, float height, String text, Boolean bolded,
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

    /**
     * Dessine du texte directement sur une page PDF
     */
    public static void drawTextOnPage(com.itextpdf.kernel.pdf.PdfPage page, float x, float y, float width, float height,
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
     * Dessine le numéro de page sur le document
     */
    public static void drawPageNumber(Document document, int pageNumber, int totalPages, PdfFont font) throws IOException {
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

    /**
     * Crée un PdfDocument avec les configurations de base
     */
    public static PdfDocument createPdfDocument(ByteArrayOutputStream baos) throws IOException {
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
        return new PdfDocument(writer);
    }
}
