package bzh.stack.apimovix.service.packageservices;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.packageentity.PackageDTO;
import bzh.stack.apimovix.dto.packageentity.PackageUpdateStatusDTO;
import bzh.stack.apimovix.mapper.PackageMapper;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Command;
import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;
import bzh.stack.apimovix.model.StatusType.PackageStatus;
import bzh.stack.apimovix.repository.packagerepository.PackageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PackageService {
    private final PackageRepository packageRepository;
    private final PackageMapper packageMapper;
    private final HistoryPackageStatusService historyPackageStatusService;
    private final PackageStatusService packageStatusService;
    
    private final Random random = new Random();

    public String generateBarcode() {
        StringBuilder barcode = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            barcode.append(random.nextInt(10));
        }
        
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        barcode.append(checkDigit);
        
        return barcode.toString();
    }

    @Transactional(readOnly = true)
    public String generateNewBarcode(String base) {
        String barcode;
        do {
            barcode = base + generateBarcode();
        } while (packageRepository.findById(barcode).orElse(null) != null);

        return barcode;
    }

    @Transactional
    public PackageEntity save(PackageEntity packageEntity) {
        return packageRepository.save(packageEntity);
    }

    @Transactional
    public PackageEntity createPackage(Command command, @Valid PackageDTO packageDTO, String numTransport) {
        PackageEntity packageEntity;
        if (packageDTO != null) {
            packageEntity = packageMapper.toEntity(packageDTO);
        } else {
            packageEntity = new PackageEntity();
        }
        packageEntity.setZoneName(command.getPharmacy().getZone().getName());
        packageEntity.setCommand(command);
        packageEntity.setCNumTransport(numTransport);
        String newBarcode = generateNewBarcode(command.getPharmacy().getPostalCode());
        packageEntity.setBarcode(newBarcode);

        packageEntity = packageRepository.save(packageEntity);

        Optional<PackageStatus> optStatus = packageStatusService.findPackageStatus(1);
        if (optStatus.isPresent()) {
            HistoryPackageStatus hs = historyPackageStatusService.createHistoryPackageStatus(packageEntity, null, optStatus.get());
            packageEntity.setLastHistoryStatus(hs);
        }

        return packageRepository.save(packageEntity);
    }

    @Transactional(readOnly = true)
    public Optional<PackageEntity> findPackage(String barcode) {
        PackageEntity packageEntity = packageRepository.findPackage(barcode);
        
        if (packageEntity != null) {
            packageEntity.getCommand();
            return Optional.of(packageEntity);
        }
        return Optional.empty();
    }

    public List<HistoryPackageStatus> findPackageHistory(Account account, String barcode) {
        return packageRepository.findPackageHistory(account, barcode);
    }

    @Transactional
    public PackageEntity updatePackageStatus(Profil profil, PackageStatus status, PackageEntity packageEntity) {
        HistoryPackageStatus historyPackageStatus = historyPackageStatusService.createHistoryPackageStatus(packageEntity, profil, status);
        packageEntity.setLastHistoryStatus(historyPackageStatus);
        return packageRepository.save(packageEntity);
    }

    @Transactional
    public boolean updatePackageStatusBulk(Profil profil, PackageUpdateStatusDTO packageUpdateStatusDTO) {
        Optional<PackageStatus> optStatus = packageStatusService.findPackageStatus(packageUpdateStatusDTO.getStatusId());
        if (optStatus.isEmpty()) {
            return false;
        }
        PackageStatus status = optStatus.get();

        List<PackageEntity> packages = packageRepository.findAllByIdIn(profil.getAccount(), packageUpdateStatusDTO.getPackageBarcodes());
        if (packages.size() != packageUpdateStatusDTO.getPackageBarcodes().size()) {
            return false;
        }

        List<HistoryPackageStatus> historyStatuses = packages.stream()
            .map(packageEntity -> historyPackageStatusService.createHistoryPackageStatus(packageEntity, profil, status))
            .collect(Collectors.toList());

            packages.forEach(packageEntity -> packageEntity.setLastHistoryStatus(
            historyStatuses.stream()
                .filter(hs -> hs.getPackageEntity().getBarcode().equals(packageEntity.getBarcode()))
                .findFirst()
                .orElse(null)
        ));

        packageRepository.saveAll(packages);
        return true;
    }

    @Transactional(readOnly = true)
    public List<PackageEntity> findPackagesByBarcodes(Account account, List<String> barcodes) {
        return packageRepository.findAllByIdIn(account, barcodes);
    }
} 