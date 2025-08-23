package bzh.stack.apimovix.service.packageservices;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.StatusType.PackageStatus;
import bzh.stack.apimovix.repository.packagerepository.PackageStatusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PackageStatusService {
    private final PackageStatusRepository PackageStatusRepository;

    private final Map<Integer, PackageStatus> statusCache = new HashMap<>();

    @Transactional(readOnly = true)
    public Optional<PackageStatus> findPackageStatus(Integer id) {
        PackageStatus cachedStatus = statusCache.get(id);
        if (cachedStatus != null) {
            return Optional.of(cachedStatus);
        }

        PackageStatus status = PackageStatusRepository.findById(id).orElse(null);
        if (status == null) {
            return Optional.empty();
        }

        statusCache.put(id, status);
        return Optional.of(status);
    }

    public void clearCache() {
        statusCache.clear();
    }
}
