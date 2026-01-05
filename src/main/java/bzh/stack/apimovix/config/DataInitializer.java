package bzh.stack.apimovix.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.StatusType.CommandStatus;
import bzh.stack.apimovix.model.StatusType.PackageStatus;
import bzh.stack.apimovix.model.StatusType.TourStatus;
import bzh.stack.apimovix.model.StatusType.TypeAnomalie;
import bzh.stack.apimovix.repository.anomalie.TypeAnomalieRepository;
import bzh.stack.apimovix.repository.command.CommandStatusRepository;
import bzh.stack.apimovix.repository.packagerepository.PackageStatusRepository;
import bzh.stack.apimovix.repository.tour.TourStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final CommandStatusRepository commandStatusRepository;
    private final PackageStatusRepository packageStatusRepository;
    private final TourStatusRepository tourStatusRepository;
    private final TypeAnomalieRepository typeAnomalieRepository;

    @Bean
    @Transactional
    public CommandLineRunner initDefaultData() {
        return args -> {
            initCommandStatuses();
            initPackageStatuses();
            initTourStatuses();
            initTypeAnomalies();
        };
    }

    private void initCommandStatuses() {
        if (commandStatusRepository.count() == 0) {
            log.info("Initialisation des statuts de commande par défaut...");

            createCommandStatus(1, "À enlever");
            createCommandStatus(2, "Chargé");
            createCommandStatus(3, "Livré");
            createCommandStatus(4, "Non Livré");
            createCommandStatus(5, "Livré incomplet");
            createCommandStatus(6, "Chargé incomplet");
            createCommandStatus(7, "Non chargé - MANQUANT");
            createCommandStatus(8, "Non livré - Inaccessible");
            createCommandStatus(9, "Non livré - Instructions invalides");

            log.info("Statuts de commande initialisés avec succès.");
        }
    }

    private void initPackageStatuses() {
        if (packageStatusRepository.count() == 0) {
            log.info("Initialisation des statuts de colis par défaut...");

            createPackageStatus(1, "À enlever");
            createPackageStatus(2, "Chargé");
            createPackageStatus(3, "Livré");
            createPackageStatus(4, "Non Livré");
            createPackageStatus(5, "Non chargé - MANQUANT");
            createPackageStatus(6, "Non livré - ANOMALIE");

            log.info("Statuts de colis initialisés avec succès.");
        }
    }

    private void initTourStatuses() {
        if (tourStatusRepository.count() == 0) {
            log.info("Initialisation des statuts de tournée par défaut...");

            createTourStatus(1, "Création");
            createTourStatus(2, "Chargement");
            createTourStatus(3, "Livraison");
            createTourStatus(4, "Debrief");
            createTourStatus(5, "Clôturé");

            log.info("Statuts de tournée initialisés avec succès.");
        }
    }

    private void initTypeAnomalies() {
        if (typeAnomalieRepository.count() == 0) {
            log.info("Initialisation des types d'anomalie par défaut...");

            createTypeAnomalie("c_dev", "Colis dévoyé");
            createTypeAnomalie("c_end", "Colis endommagé");
            createTypeAnomalie("c_per", "Colis perdu");
            createTypeAnomalie("other", "Autre");
            createTypeAnomalie("excu_temp", "Excursion de température");

            log.info("Types d'anomalie initialisés avec succès.");
        }
    }

    private void createCommandStatus(Integer id, String name) {
        CommandStatus status = new CommandStatus();
        status.setId(id);
        status.setName(name);
        commandStatusRepository.save(status);
    }

    private void createPackageStatus(Integer id, String name) {
        PackageStatus status = new PackageStatus();
        status.setId(id);
        status.setName(name);
        packageStatusRepository.save(status);
    }

    private void createTourStatus(Integer id, String name) {
        TourStatus status = new TourStatus();
        status.setId(id);
        status.setName(name);
        tourStatusRepository.save(status);
    }

    private void createTypeAnomalie(String code, String name) {
        TypeAnomalie type = new TypeAnomalie();
        type.setCode(code);
        type.setName(name);
        typeAnomalieRepository.save(type);
    }
}
