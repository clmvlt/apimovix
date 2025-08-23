package bzh.stack.apimovix.service.packageservices;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.PackageEntity;
import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.History.HistoryPackageStatus;
import bzh.stack.apimovix.model.StatusType.PackageStatus;
import bzh.stack.apimovix.repository.packagerepository.HistoryPackageStatusRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryPackageStatusService {
    private final HistoryPackageStatusRepository historyPackageStatusRepository;

    @Transactional
    public HistoryPackageStatus createHistoryPackageStatus(PackageEntity packageEntity, Profil profil, @NotNull PackageStatus status) {
        HistoryPackageStatus historyPackageStatus = new HistoryPackageStatus();
        historyPackageStatus.setPackageEntity(packageEntity);
        historyPackageStatus.setProfil(profil);
        historyPackageStatus.setId(UUID.randomUUID());
        historyPackageStatus.setStatus(status);
        return historyPackageStatusRepository.save(historyPackageStatus);
    }
} 