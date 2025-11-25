package bzh.stack.apimovix.service.update;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.MobileUpdate;
import bzh.stack.apimovix.repository.update.MobileUpdateRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MobileUpdateService {
    
    private final MobileUpdateRepository mobileUpdateRepository;
    private final FileService fileService;

    @Transactional
    public Optional<MobileUpdate> addUpdate(byte[] apkBytes, String version) {
        String filePath = fileService.saveApkFile(apkBytes, version);
        if (filePath == null) {
            return Optional.empty();
        }

        try {
            MobileUpdate update = new MobileUpdate();
            update.setId(UUID.randomUUID());
            update.setVersion(version);
            update.setFilePath(filePath);
            return Optional.of(mobileUpdateRepository.save(update));
        } catch (Exception e) {
            fileService.deleteFile(filePath);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<MobileUpdate> getLatestVersion() {
        return mobileUpdateRepository.findLatestVersion();
    }

    @Transactional(readOnly = true)
    public Optional<MobileUpdate> getUpdateByVersion(String version) {
        // Try to find by UUID first (for backward compatibility)
        try {
            UUID uuid = UUID.fromString(version);
            Optional<MobileUpdate> byId = mobileUpdateRepository.findById(uuid);
            if (byId.isPresent()) {
                return byId;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a valid UUID, try by version string
        }
        // Find by version string (e.g. "1.0.0")
        return mobileUpdateRepository.findByVersion(version);
    }

    @Transactional
    public boolean deleteUpdate(String version) {
        Optional<MobileUpdate> optUpdate = getUpdateByVersion(version);
        if (optUpdate.isEmpty()) {
            return false;
        }

        MobileUpdate update = optUpdate.get();
        boolean deleted = fileService.deleteFile(update.getFilePath());
        if (deleted) {
            mobileUpdateRepository.delete(update);
        }
        return deleted;
    }

    @Transactional(readOnly = true)
    public List<MobileUpdate> getAllUpdates() {
        return mobileUpdateRepository.findAllOrderByCreatedAtDesc();
    }
}
