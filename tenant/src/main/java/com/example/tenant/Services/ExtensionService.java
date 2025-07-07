package com.example.tenant.Services;

import com.example.tenant.Entities.Tenant;
import com.example.tenant.Repositories.SipProfileRepository;
import com.example.tenant.Repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class ExtensionService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SipProfileRepository sipProfileRepository;

    @Transactional
    public synchronized String generateNextExtension(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant non trouvé"));

        String fullExtension;
        int attempts = 0;
        final int MAX_ATTEMPTS = 1000; // Sécurité contre les boucles infinies

        do {
            if (attempts++ >= MAX_ATTEMPTS) {
                throw new RuntimeException("Impossible de générer une extension unique après " + MAX_ATTEMPTS + " tentatives");
            }

            String numberPart = String.format("%0" + tenant.getExtensionLength() + "d", tenant.getNextExtensionNumber());
            fullExtension = tenant.getExtensionPrefix() + numberPart;
            tenant.setNextExtensionNumber(tenant.getNextExtensionNumber() + 1);
        } while (sipProfileRepository.existsByExtension(fullExtension)); // Utilisation de la nouvelle méthode

        tenantRepository.save(tenant);
        return fullExtension;
    }

    public boolean isExtensionValidForTenant(Long tenantId, String extension) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Vérifier que l'extension commence par le préfixe du tenant
        if (!extension.startsWith(tenant.getExtensionPrefix().toString())) {
            return false;
        }

        // Vérifier la longueur totale
        int expectedLength = tenant.getExtensionPrefix().toString().length() + tenant.getExtensionLength();
        if (extension.length() != expectedLength) {
            return false;
        }

        // Vérifier que la partie numérique est valide
        String numberPart = extension.substring(tenant.getExtensionPrefix().toString().length());
        try {
            Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
