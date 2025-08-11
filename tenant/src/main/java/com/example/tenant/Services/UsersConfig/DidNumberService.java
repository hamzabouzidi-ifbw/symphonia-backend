package com.example.tenant.Services.UsersConfig;

import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.DidNumberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DidNumberService {

    private final DidNumberRepository didNumberRepository;

    public DidNumberService(DidNumberRepository didNumberRepository) {
        this.didNumberRepository = didNumberRepository;
    }

    /** Récupérer tous les DIDs actifs d'un tenant */
    public List<DidNumber> getByTenant(Long tenantId) {
        return didNumberRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    /** Récupérer un DID par son ID */
    public Optional<DidNumber> getById(Long id) {
        return didNumberRepository.findById(id);
    }

    /** Créer ou mettre à jour un DID */
    public DidNumber save(DidNumber didNumber) {
        if (didNumberRepository.existsByNumber(didNumber.getNumber())) {
            throw new IllegalArgumentException("Ce numéro DID existe déjà : " + didNumber.getNumber());
        }
        return didNumberRepository.save(didNumber);
    }

    /** Supprimer un DID par ID */
    public void delete(Long id) {
        didNumberRepository.deleteById(id);
    }

    /** Activer / désactiver un DID */
    public DidNumber setActive(Long id, boolean active) {
        DidNumber did = didNumberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DID introuvable"));
        did.setActive(active);
        return didNumberRepository.save(did);
    }
}
