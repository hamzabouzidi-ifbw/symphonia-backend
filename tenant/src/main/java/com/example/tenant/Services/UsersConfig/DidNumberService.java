package com.example.tenant.Services.UsersConfig;

import com.example.tenant.Entities.Trunk;
import com.example.tenant.Entities.TrunkPool;
import com.example.tenant.Entities.UsersConfig.DestinationType;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.DidNumberRepository;
import com.example.tenant.Repositories.TrunkPoolRepository;
import com.example.tenant.Repositories.TrunkRepository;
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
    private TrunkPoolRepository trunkPoolRepository;

    @Autowired
    private TrunkRepository trunkRepository;

   /* public DidNumber createDidForTenant(Long trunkPoolId,
                                        Long trunkId,
                                        String number,
                                        DestinationType destinationType,
                                        String destinationValue) {

        // Vérifier que le TrunkPool existe
        TrunkPool trunkPool = trunkPoolRepository.findById(trunkPoolId)
                .orElseThrow(() -> new RuntimeException("TrunkPool non trouvé"));

        // Vérifier que le Trunk existe
        Trunk trunk = trunkRepository.findById(trunkId)
                .orElseThrow(() -> new RuntimeException("Trunk non trouvé"));

        // Vérifier que le Trunk appartient au pool
        if (!trunkPool.getTrunks().contains(trunk)) {
            throw new RuntimeException("Le trunk choisi n'appartient pas à ce TrunkPool");
        }

        // Vérifier que le numéro est dans la plage du pool
        // Extraire la partie locale du numéro
        int countryLength = String.valueOf(trunkPool.getCountryCode()).length();
        int areaLength = String.valueOf(trunkPool.getAreaCode()).length();
        int localLength = String.valueOf(trunkPool.getLocalCode()).length();

        String localNumberStr = number.substring(countryLength + areaLength + localLength);
        int localNumber = Integer.parseInt(localNumberStr);

        if (localNumber < trunkPool.getStartNumber() || localNumber > trunkPool.getEndNumber()) {
            throw new RuntimeException("Numéro en dehors de la plage du TrunkPool");
        }

        // Vérifier que le DID n'existe pas déjà
        if (didNumberRepository.existsByNumber(number)) {
            throw new RuntimeException("Ce numéro DID existe déjà");
        }

        // Créer le DID
        DidNumber did = new DidNumber();
        did.setNumber(number);
        did.setTenant(trunkPool.getTenant());
        did.setTrunk(trunk);
        did.setDestinationType(destinationType);
        did.setDestinationValue(destinationValue);
        did.setActive(true);

        return didNumberRepository.save(did);
    }
*/

    public DidNumber createDidForTenant(Long tenantId,
                                        Long trunkId,
                                        String number,
                                        DestinationType destinationType,
                                        String destinationValue) {

        // Vérifier que le Trunk existe et appartient au tenant
        Trunk trunk = trunkRepository.findById(trunkId)
                .orElseThrow(() -> new RuntimeException("Trunk non trouvé"));

        if (!trunk.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Le trunk choisi n'appartient pas au tenant spécifié");
        }

        // Récupérer le TrunkPool du trunk
        TrunkPool trunkPool = trunk.getTrunkPool();
        if (trunkPool == null) {
            throw new RuntimeException("Le trunk n'est associé à aucun TrunkPool");
        }

        // Vérifier que le TrunkPool appartient bien au tenant
        if (!trunkPool.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Le TrunkPool du trunk n'appartient pas au tenant spécifié");
        }

        // Vérifier que le numéro est dans la plage du pool
        int countryLength = String.valueOf(trunkPool.getCountryCode()).length();
        int areaLength = String.valueOf(trunkPool.getAreaCode()).length();
        int localLength = String.valueOf(trunkPool.getLocalCode()).length();

        if (number.length() <= countryLength + areaLength + localLength) {
            throw new RuntimeException("Numéro DID trop court pour extraire la partie locale");
        }

        String localNumberStr = number.substring(countryLength + areaLength + localLength);
        int localNumber;
        try {
            localNumber = Integer.parseInt(localNumberStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Numéro DID invalide");
        }

        if (localNumber < trunkPool.getStartNumber() || localNumber > trunkPool.getEndNumber()) {
            throw new RuntimeException("Numéro en dehors de la plage du TrunkPool");
        }

        // Vérifier que le DID n'existe pas déjà
        if (didNumberRepository.existsByNumber(number)) {
            throw new RuntimeException("Ce numéro DID existe déjà");
        }

        // Créer le DID
        DidNumber did = new DidNumber();
        did.setNumber(number);
        did.setTenant(trunk.getTenant()); // Le tenant du trunk
        did.setTrunk(trunk);
        did.setDestinationType(destinationType);
        did.setDestinationValue(destinationValue);
        did.setActive(true);

        return didNumberRepository.save(did);
    }

    public DidNumber updateDid(Long tenantId,
                               Long didId,
                               Long trunkId,
                               String number,
                               DestinationType destinationType,
                               String destinationValue) {

        // Vérifier que le DID existe
        DidNumber did = didNumberRepository.findById(didId)
                .orElseThrow(() -> new RuntimeException("DID non trouvé"));

        // Vérifier que le DID appartient au tenant
        if (!did.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Le DID n'appartient pas au tenant spécifié");
        }

        // Vérifier que le Trunk existe et appartient au tenant
        Trunk trunk = trunkRepository.findById(trunkId)
                .orElseThrow(() -> new RuntimeException("Trunk non trouvé"));

        if (!trunk.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Le trunk choisi n'appartient pas au tenant spécifié");
        }

        // Vérifier que le Trunk appartient au même TrunkPool que le DID
        TrunkPool trunkPool = did.getTrunk().getTrunkPool();
        if (!trunkPool.getTrunks().contains(trunk)) {
            throw new RuntimeException("Le trunk choisi n'appartient pas au même TrunkPool que le DID");
        }

        // Vérifier la plage du numéro si on change le numéro
        if (number != null && !number.equals(did.getNumber())) {
            int countryLength = String.valueOf(trunkPool.getCountryCode()).length();
            int areaLength = String.valueOf(trunkPool.getAreaCode()).length();
            int localLength = String.valueOf(trunkPool.getLocalCode()).length();

            if (number.length() <= countryLength + areaLength + localLength) {
                throw new RuntimeException("Numéro DID trop court pour extraire la partie locale");
            }

            String localNumberStr = number.substring(countryLength + areaLength + localLength);
            int localNumber;
            try {
                localNumber = Integer.parseInt(localNumberStr);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Numéro DID invalide");
            }

            if (localNumber < trunkPool.getStartNumber() || localNumber > trunkPool.getEndNumber()) {
                throw new RuntimeException("Numéro en dehors de la plage du TrunkPool");
            }

            // Vérifier unicité
            if (didNumberRepository.existsByNumber(number)) {
                throw new RuntimeException("Ce numéro DID existe déjà");
            }

            did.setNumber(number);
        }

        // Mise à jour des autres champs
        did.setTrunk(trunk);
        did.setDestinationType(destinationType);
        did.setDestinationValue(destinationValue);

        return didNumberRepository.save(did);
    }

    public void deleteDid(Long didId) {
        // Vérifier que le DID existe
        DidNumber did = didNumberRepository.findById(didId)
                .orElseThrow(() -> new RuntimeException("DID non trouvé"));

        // Supprimer le DID
        didNumberRepository.delete(did);
    }
    public List<DidNumber> getAllDids() {
        return didNumberRepository.findAll();
    }
    public List<DidNumber> getActiveDidsByTenant(Long tenantId) {
        return didNumberRepository.findByTenantIdAndActiveTrue(tenantId);
    }



}