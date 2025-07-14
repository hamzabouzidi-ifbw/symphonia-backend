package com.example.tenant.Services.UsersConfig;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Repositories.DidNumberRepository;
import com.example.tenant.Repositories.SipProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DidNumberService {

    @Autowired
    private  DidNumberRepository didNumberRepository;
    @Autowired
    private  SipProfileRepository sipProfileRepository;

    public DidNumber createDid(DidNumber request) {
        String didNumber = request.getDidNumber();
        if (didNumber == null) {
            throw new IllegalArgumentException("Le numéro DID ne peut pas être null.");
        }

        if (!didNumber.matches("^\\d{10,15}$")) {
            throw new IllegalArgumentException("Format du numéro DID invalide. Il doit contenir entre 10 et 15 chiffres.");
        }

        Optional<DidNumber> existing = didNumberRepository.findByDidNumber(didNumber);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Ce numéro DID est déjà attribué.");
        }

        Optional<SipProfile> sipOpt = sipProfileRepository.findByExtensionAndId(
                request.getExtension(), request.getSipProfileId());

        if (sipOpt.isEmpty()) {
            throw new IllegalArgumentException("Aucun utilisateur SIP correspondant à cette extension.");
        }

        SipProfile sip = sipOpt.get();
        request.setDomainName(sip.getDomainName());

        return didNumberRepository.save(request);
    }
    public Optional<DidNumber> findActiveDid(String didNumber) {
        return didNumberRepository.findByDidNumber(didNumber);
    }


}

