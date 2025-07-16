package com.example.tenant.Services.UsersConfig;

import com.example.tenant.Dto.VoicemailConfigDto;
import com.example.tenant.Entities.UsersConfig.VoicemailConfig;
import com.example.tenant.Repositories.SipProfileRepository;
import com.example.tenant.Repositories.VoicemailConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoicemailConfigService {

    @Autowired
    private VoicemailConfigRepository voicemailConfigRepository;

    @Autowired
    private SipProfileRepository sipProfileRepository;

    public Optional<VoicemailConfig> getBySipProfileId(Long sipProfileId) {
        return voicemailConfigRepository.findBySipProfileId(sipProfileId);
    }

    public VoicemailConfig save(VoicemailConfig config) {
        return voicemailConfigRepository.save(config);
    }

    public void attachSipProfile(VoicemailConfig config, Long sipProfileId) {
        sipProfileRepository.findById(sipProfileId).ifPresent(config::setSipProfile);
    }

    public List<VoicemailConfig> getAllVoicemailConfigs() {
        return voicemailConfigRepository.findAll();
    }

    // 🆕 Ajouter cette méthode :
    public List<VoicemailConfigDto> getAllVoicemailConfigDTOs() {
        return voicemailConfigRepository.findAll()
                .stream()
                .map(entity -> {
                    VoicemailConfigDto dto = new VoicemailConfigDto();
                    dto.setId(entity.getId());
                    dto.setVoicemailEnabled(entity.isVoicemailEnabled());
                    dto.setVoicemailPassword(entity.getVoicemailPassword());
                    dto.setNotificationEmail(entity.getNotificationEmail());
                    dto.setSipProfileId(entity.getSipProfile().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    // Activer le voicemail d'un profil SIP donné
    public boolean activateVoicemail(Long sipProfileId) {
        Optional<VoicemailConfig> configOpt = voicemailConfigRepository.findBySipProfileId(sipProfileId);
        if (configOpt.isPresent()) {
            VoicemailConfig config = configOpt.get();
            config.setVoicemailEnabled(true);
            voicemailConfigRepository.save(config);
            return true;
        }
        return false;  // pas trouvé
    }

    // Désactiver le voicemail d'un profil SIP donné
    public boolean deactivateVoicemail(Long sipProfileId) {
        Optional<VoicemailConfig> configOpt = voicemailConfigRepository.findBySipProfileId(sipProfileId);
        if (configOpt.isPresent()) {
            VoicemailConfig config = configOpt.get();
            config.setVoicemailEnabled(false);
            voicemailConfigRepository.save(config);
            return true;
        }
        return false;  // pas trouvé
    }
}
