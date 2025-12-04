package bzh.stack.apimovix.service.pdfGenerator;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Anomalie;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Pharmacy;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.model.Tour;

/**
 * Service principal pour la génération de PDF
 * Orchestrateur qui délègue aux générateurs spécialisés
 */
@Service
public class PdfGeneratorService {

    private final PackageLabelGenerator packageLabelGenerator;
    private final TourPdfGenerator tourPdfGenerator;
    private final TarifTourPdfGenerator tarifTourPdfGenerator;
    private final AnomaliePdfGenerator anomaliePdfGenerator;
    private final PharmacyLabelGenerator pharmacyLabelGenerator;

    public PdfGeneratorService(
            PackageLabelGenerator packageLabelGenerator,
            TourPdfGenerator tourPdfGenerator,
            TarifTourPdfGenerator tarifTourPdfGenerator,
            AnomaliePdfGenerator anomaliePdfGenerator,
            PharmacyLabelGenerator pharmacyLabelGenerator) {
        this.packageLabelGenerator = packageLabelGenerator;
        this.tourPdfGenerator = tourPdfGenerator;
        this.tarifTourPdfGenerator = tarifTourPdfGenerator;
        this.anomaliePdfGenerator = anomaliePdfGenerator;
        this.pharmacyLabelGenerator = pharmacyLabelGenerator;
    }

    /**
     * Génère une étiquette PDF pour un colis
     *
     * @param packageEntity Le colis pour lequel générer l'étiquette
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generateLabel(PackageEntity packageEntity) throws IOException {
        return packageLabelGenerator.generateLabel(packageEntity);
    }

    /**
     * Génère un PDF de tournée
     *
     * @param tour La tournée pour laquelle générer le PDF
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generateTourPdf(Tour tour) throws IOException {
        return tourPdfGenerator.generateTourPdf(tour);
    }

    /**
     * Génère un PDF de tournée avec tarification
     *
     * @param tour La tournée pour laquelle générer le PDF
     * @param tarifs La liste des tarifs à appliquer
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generateTarifTourPdf(Tour tour, List<Tarif> tarifs) throws IOException {
        return tarifTourPdfGenerator.generateTarifTourPdf(tour, tarifs);
    }

    /**
     * Génère un PDF d'anomalie en utilisant le template existant
     *
     * @param anomalie L'anomalie pour laquelle générer le PDF
     * @return Les octets du PDF généré
     * @throws IOException En cas d'erreur lors de la génération
     */
    public byte[] generateAnomaliePdf(Anomalie anomalie) throws IOException {
        return anomaliePdfGenerator.generateAnomaliePdf(anomalie);
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
        return pharmacyLabelGenerator.generatePharmacyLabel(pharmacy, account);
    }
}
