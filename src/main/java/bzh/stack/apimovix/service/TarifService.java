package bzh.stack.apimovix.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import bzh.stack.apimovix.dto.tarif.TarifCreateDTO;
import bzh.stack.apimovix.model.Account;
import bzh.stack.apimovix.model.Tarif;
import bzh.stack.apimovix.repository.TarifRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TarifService {

    private final TarifRepository tarifRepository;
    
    @Transactional(readOnly = true)
    public List<Tarif> findTarifsByAccount(Account account) {
        return tarifRepository.findTarifs(account.getId());
    }
    
    @Transactional(readOnly = true)
    public Optional<Tarif> findTarif(UUID id, Account account) {
        Tarif tarif = tarifRepository.findTarif(id, account.getId());

        if (tarif == null) {
            return Optional.empty();
        }
        
        return Optional.of(tarif);
    }
    
    @Transactional
    public Tarif createTarif(TarifCreateDTO tarifCreateDTO) {
        Tarif tarif = new Tarif();

        tarif.setAccount(tarifCreateDTO.getAccount());
        tarif.setKmMax(tarifCreateDTO.getKmMax());
        tarif.setPrixEuro(tarifCreateDTO.getPrixEuro());

        return tarifRepository.save(tarif);
    }
    
    @Transactional
    public void deleteTarif(UUID id) {
        tarifRepository.deleteById(id);
    }
} 