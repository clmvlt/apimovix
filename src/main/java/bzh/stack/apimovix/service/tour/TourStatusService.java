package bzh.stack.apimovix.service.tour;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.StatusType.TourStatus;
import bzh.stack.apimovix.repository.tour.TourStatusRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TourStatusService {
    private final TourStatusRepository tourStatusRepository;

    private final Map<Integer, TourStatus> statusCache = new HashMap<>();

    @Transactional
    public Optional<TourStatus> findTourStatusById(int id) {
        TourStatus cachedStatus = statusCache.get(id);
        if (cachedStatus != null) {
            return Optional.of(cachedStatus);
        }

        TourStatus status = tourStatusRepository.findById(id).orElse(null);
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