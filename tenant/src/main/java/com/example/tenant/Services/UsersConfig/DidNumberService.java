package com.example.tenant.Services.UsersConfig;

import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.DidNumberRepository;
import com.example.tenant.Services.TrunkPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DidNumberService {

    @Autowired
    private DidNumberRepository didNumberRepository;


    @Autowired
    private TrunkPoolService trunkPoolService;



    public DidNumberService(DidNumberRepository didNumberRepository, TrunkPoolService trunkPoolService) {
        this.didNumberRepository = didNumberRepository;
        this.trunkPoolService = trunkPoolService;
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
    /*public DidNumber save(DidNumber didNumber) {
        if (didNumberRepository.existsByNumber(didNumber.getNumber())) {
            throw new IllegalArgumentException("Ce numéro DID existe déjà : " + didNumber.getNumber());
        }
        return didNumberRepository.save(didNumber);
    }*/

    public DidNumber save(DidNumber didNumber) {
        // Vérification unicité
        if (didNumberRepository.existsByNumber(didNumber.getNumber())) {
            throw new IllegalArgumentException("Ce numéro DID existe déjà : " + didNumber.getNumber());
        }

        // Vérification respect du TrunkPool
        Long tenantId = didNumber.getTenant().getId();
        if (!trunkPoolService.isNumberInTenantPool(tenantId, didNumber.getNumber())) {
            throw new IllegalArgumentException(
                    "Le numéro " + didNumber.getNumber() + " ne correspond à aucun pool autorisé pour ce tenant."
            );
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
