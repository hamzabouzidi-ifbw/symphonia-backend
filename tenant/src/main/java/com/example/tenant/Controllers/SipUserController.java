package com.example.tenant.Controllers;

import com.example.tenant.Dto.*;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.Tenant;
import com.example.tenant.Entities.UsersConfig.CallGroup;
import com.example.tenant.Entities.UsersConfig.DidNumber;
import com.example.tenant.Entities.UsersConfig.VoicemailConfig;
import com.example.tenant.Repositories.SipProfileRepository;
import com.example.tenant.Services.SipProfileRestrictionService;
import com.example.tenant.Services.SipUserService;
import com.example.tenant.Services.UsersConfig.CallGroupService;
import com.example.tenant.Services.UsersConfig.DidNumberService;
import com.example.tenant.Services.UsersConfig.VoicemailConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/sip-users")

public class SipUserController {
    @Autowired
    private SipUserService sipUserService;
    @Autowired
    private SipProfileRepository sipProfileRepository;
    @Autowired
    private DidNumberService didNumberService;
    @Autowired
    private VoicemailConfigService voicemailConfigService;
    @Autowired
    private CallGroupService callGroupService;
    @Autowired
    private SipProfileRestrictionService restrictionService;
    @PostMapping()
    public ResponseEntity<?> createSipUser(
            @RequestBody CreateSipUserRequest request,
            @RequestHeader(value = "role", required = false) String role) {

        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You do not have permission to create a tenant."));
        }

        try {
            SipProfile profile = sipUserService.createSipUser(request);
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            // Tu peux ici affiner le code selon le message ou type d'exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<SipUserCreationResponse> updateSipUser(
            @PathVariable Long id,
            @RequestBody CreateSipUserRequest request) {
        SipUserCreationResponse response = sipUserService.updateSipUser(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<SipProfile>> getSipUsersByTenantId(@PathVariable Long tenantId) {
        List<SipProfile> users = sipUserService.getSipUsersByTenantId(tenantId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SipProfile> getSipUserById(@PathVariable Long id) {
        return sipUserService.getSipUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSipUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        try {
            sipUserService.deleteSipUser(id, token);
            return ResponseEntity.ok("SIP User deleted and licence quota updated.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du SIP user: " + e.getMessage());
        }
    }
    @GetMapping("/all")
    public List<SipProfile> getAllSipProfiles() {
        return sipUserService.getAllSipProfiles();
    }









    /******************************************** Freeswitch *****************************************/





    // ✅ GET voicemail config by sipProfileId
    @GetMapping("/voice_mail/{sipProfileId}")
    public ResponseEntity<VoicemailConfigDto> getVoicemailConfig(@PathVariable Long sipProfileId) {
        return voicemailConfigService.getBySipProfileId(sipProfileId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ POST save/update voicemail config for sipProfileId
    @PostMapping("/{sipProfileId}")
    public ResponseEntity<VoicemailConfigDto> saveVoicemailConfig(
            @PathVariable Long sipProfileId,
            @RequestBody VoicemailConfig config) {

        // set the SipProfile reference manually if needed
        voicemailConfigService.attachSipProfile(config, sipProfileId); // We'll define this in service

        VoicemailConfig saved = voicemailConfigService.save(config);
        return ResponseEntity.ok(toDto(saved));
    }

    // ✅ Mapping function: VoicemailConfig → VoicemailConfigDto
    private VoicemailConfigDto toDto(VoicemailConfig entity) {
        VoicemailConfigDto dto = new VoicemailConfigDto();
        dto.setId(entity.getId());

        if (entity.getSipProfile() != null) {
            dto.setSipProfileId(entity.getSipProfile().getId());
            dto.setSipUserExtension(entity.getSipProfile().getExtension());
        }

        dto.setVoicemailPassword(entity.getVoicemailPassword());
        dto.setVoicemailEnabled(entity.isVoicemailEnabled());
        dto.setNotificationEmail(entity.getNotificationEmail());

        return dto;
    }
    @GetMapping("/voice-mail/all")
    public List<VoicemailConfig> getAllVoicemailConfigs() {
        return voicemailConfigService.getAllVoicemailConfigs();
    }


    @PostMapping("/{sipProfileId}/activate")
    public ResponseEntity<?> activateVoicemail(@PathVariable Long sipProfileId) {
        boolean updated = voicemailConfigService.activateVoicemail(sipProfileId);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{sipProfileId}/deactivate")
    public ResponseEntity<?> deactivateVoicemail(@PathVariable Long sipProfileId) {
        boolean updated = voicemailConfigService.deactivateVoicemail(sipProfileId);
        if (updated) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/call-groups")
    public ResponseEntity<?> createGroup(@RequestBody CallGroupRequest request) {
        CallGroup group = callGroupService.createCallGroupWithMembers(
                request.getGroupName(),
                request.getExtension(),
                request.getSipProfileIds()
        );

        return ResponseEntity.ok(group);
    }

    @GetMapping("/call-groups/all")
    public ResponseEntity<List<CallGroup>> getAllCallGroups() {
        List<CallGroup> callGroups = callGroupService.getAllCallGroupsWithMembers();
        return ResponseEntity.ok(callGroups);
    }

    @PutMapping("/call-groups/{id}")
    public CallGroup updateCallGroup(
            @PathVariable Long id,
            @RequestBody CallGroupRequest request) {
        return callGroupService.updateCallGroup(
                id,
                request.getGroupName(),
                request.getExtension(),
                request.getSipProfileIds()
        );
    }
    @PostMapping("/call-groups/{id}/members")
    public ResponseEntity<CallGroup> addMembers(@PathVariable Long id, @RequestBody List<Long> sipIds) {
        CallGroup updated = callGroupService.addMembersToGroup(id, sipIds);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/call-groups/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        callGroupService.deleteCallGroup(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/call-groups/{groupId}/members/{sipId}")
    public ResponseEntity<CallGroup> removeMember(@PathVariable Long groupId, @PathVariable Long sipId) {
        CallGroup updated = callGroupService.removeMemberFromGroup(groupId, sipId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/restrictions")
    public ResponseEntity<String> createRestrictions(@RequestBody RestrictionRequest request) {
        try {
            restrictionService.createRestrictions(
                    request.getDomain(),
                    request.getExtension(),
                    request.getMaxCalls(),
                    request.getDialplanRegex(),
                    request.isAllowedDialplan(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.isAllowTime()
            );
            return ResponseEntity.ok("Restrictions créées pour " + request.getExtension() + "@" + request.getDomain());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur : " + e.getMessage());
        }
    }


    // 🔹 Récupérer toutes les restrictions de TOUS les profils
    @GetMapping("/get-all-restrictions")
    public ResponseEntity<List<SipProfileRestrictions>> getProfilesWithRestrictions() {
        List<SipProfileRestrictions> restrictions = restrictionService.getProfilesWithRestrictions();

        if (restrictions.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 si aucun résultat
        }

        return ResponseEntity.ok(restrictions); // 200 avec la liste JSON
    }
    @PutMapping("/update-restriction")
    public String updateRestrictions(@RequestBody RestrictionUpdateRequest request) {
        restrictionService.updateRestrictions(request);
        return "Restrictions mises à jour avec succès pour " + request.getExtension() + "@" + request.getDomain();
    }
    @DeleteMapping("/delete-restriction/{domain}/{extension}")
    public String deleteRestrictions(@PathVariable String domain, @PathVariable String extension) {
        restrictionService.deleteRestrictions(domain, extension);
        return "Toutes les restrictions du profil " + extension + "@" + domain + " ont été supprimées";
    }
}