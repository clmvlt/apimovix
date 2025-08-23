package bzh.stack.apimovix.service.tour;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.model.Profil;
import bzh.stack.apimovix.model.Tour;
import bzh.stack.apimovix.model.History.HistoryTourStatus;
import bzh.stack.apimovix.model.StatusType.TourStatus;
import bzh.stack.apimovix.repository.tour.HistoryTourStatusRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryTourStatusService {
    private final HistoryTourStatusRepository historyTourStatusRepository;

    @Transactional
    public HistoryTourStatus createHistoryTourStatus(Tour tour, Profil profil, @NotNull TourStatus status) {
        HistoryTourStatus historyTourStatus = new HistoryTourStatus();
        historyTourStatus.setTour(tour);
        historyTourStatus.setProfil(profil);
        historyTourStatus.setId(UUID.randomUUID());
        historyTourStatus.setStatus(status);
        return historyTourStatusRepository.save(historyTourStatus);
    }
} 