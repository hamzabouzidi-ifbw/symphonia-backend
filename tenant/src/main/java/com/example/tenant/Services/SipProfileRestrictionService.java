package com.example.tenant.Services;

import com.example.tenant.Dto.RestrictionUpdateRequest;
import com.example.tenant.Dto.SipProfileRestrictions;
import com.example.tenant.Entities.CallLimit;
import com.example.tenant.Entities.DialPlanRestriction;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.TimeRestriction;
import com.example.tenant.Repositories.CallLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SipProfileRestrictionService {

    @Autowired
    private SipUserService sipUserService;

    @Autowired
    private CallLimitService callLimitService;

    @Autowired
    private DialPlanRestrictionService restrictionService;

    @Autowired
    private TimeRestrictionService timeRestrictionService;
    @Autowired
    private CallLimitRepository callLimitRepository;


    public void createRestrictions(String domain,
                                   String extension,
                                   int maxCalls,
                                   String dialplanRegex,
                                   boolean allowedDialplan,
                                   String startTime,
                                   String endTime,
                                   boolean allowTime) {

        Optional<SipProfile> sipProfileOpt = sipUserService.findByDomainAndExtension(domain, extension);
        if (sipProfileOpt.isEmpty() || !sipProfileOpt.get().isActive()) {
            throw new RuntimeException("Profil SIP non trouvé ou inactif : " + extension + "@" + domain);
        }

        SipProfile profile = sipProfileOpt.get();

        // 1️⃣ Limite d'appels
        CallLimit callLimit = new CallLimit();
        callLimit.setSipProfile(profile);
        callLimit.setMaxConcurrentCalls(maxCalls);
        callLimit.setIgnoreTransfer(false);
        callLimitService.save(callLimit);

        // 2️⃣ Restriction DialPlan
        DialPlanRestriction restriction = new DialPlanRestriction();
        restriction.setSipProfile(profile);
        restriction.setRegex(dialplanRegex);
        restriction.setAllowed(allowedDialplan);
        restrictionService.save(restriction);

        // 3️⃣ Restriction horaire
        TimeRestriction timeRestriction = new TimeRestriction();
        timeRestriction.setSipProfile(profile);
        timeRestriction.setStartTime(startTime);
        timeRestriction.setEndTime(endTime);
        timeRestriction.setAllow(allowTime);
        timeRestrictionService.save(timeRestriction);
    }

    public List<SipProfileRestrictions> getProfilesWithRestrictions() {
        List<SipProfile> allProfiles = sipUserService.findAll(); // récupère tous les profils
        List<SipProfileRestrictions> result = new ArrayList<>();

        for (SipProfile profile : allProfiles) {
            CallLimit callLimit = callLimitService.getBySipProfile(profile.getId());
            List<DialPlanRestriction> dialPlans = restrictionService.getBySipProfile(profile.getId());
            List<TimeRestriction> timeRestrictions = timeRestrictionService.getBySipProfile(profile.getId());

            boolean hasRestrictions = (callLimit != null) ||
                    (dialPlans != null && !dialPlans.isEmpty()) ||
                    (timeRestrictions != null && !timeRestrictions.isEmpty());

            if (hasRestrictions) {
                result.add(new SipProfileRestrictions(
                        profile,
                        callLimit != null ? List.of(callLimit) : List.of(),
                        dialPlans,
                        timeRestrictions
                ));
            }
        }

        return result;
    }

    // ✅ Nouvelle méthode pour TOUS les profils
    public List<SipProfileRestrictions> getAllRestrictions() {
        List<SipProfile> profiles = sipUserService.findAll();

        return profiles.stream().map(profile -> {
            CallLimit callLimit = callLimitService.getBySipProfile(profile.getId());
            List<DialPlanRestriction> dialPlans = restrictionService.getBySipProfile(profile.getId());
            List<TimeRestriction> timeRestrictions = timeRestrictionService.getBySipProfile(profile.getId());

            return new SipProfileRestrictions(
                    profile,
                    callLimit != null ? List.of(callLimit) : List.of(),
                    dialPlans,
                    timeRestrictions
            );
        }).collect(Collectors.toList());
    }
    // 🔹 Méthode globale de mise à jour
    public void updateRestrictions(RestrictionUpdateRequest request) {

        // 1️⃣ Récupérer le profil SIP
        SipProfile profile = sipUserService.findByDomainAndExtension(request.getDomain(), request.getExtension())
                .orElseThrow(() -> new RuntimeException(
                        "Profil SIP introuvable : " + request.getExtension() + "@" + request.getDomain()
                ));

        // 2️⃣ Update CallLimit
        CallLimit callLimit = callLimitService.getBySipProfile(profile.getId());
        if (callLimit != null && request.getMaxCalls() != null) {
            callLimit.setMaxConcurrentCalls(request.getMaxCalls());
            if (request.getIgnoreTransfer() != null) {
                callLimit.setIgnoreTransfer(request.getIgnoreTransfer());
            }
            callLimitService.save(callLimit);
        }

        // 3️⃣ Update DialPlanRestriction
        List<DialPlanRestriction> dialPlans = restrictionService.getBySipProfile(profile.getId());
        if (!dialPlans.isEmpty() && request.getDialplanRegex() != null) {
            DialPlanRestriction restriction = dialPlans.get(0); // mettre à jour le premier existant
            restriction.setRegex(request.getDialplanRegex());
            if (request.getAllowedDialplan() != null) {
                restriction.setAllowed(request.getAllowedDialplan());
            }
            restrictionService.save(restriction);
        }

        // 4️⃣ Update TimeRestriction
        List<TimeRestriction> timeRestrictions = timeRestrictionService.getBySipProfile(profile.getId());
        if (!timeRestrictions.isEmpty() && request.getStartTime() != null && request.getEndTime() != null) {
            TimeRestriction timeRestriction = timeRestrictions.get(0); // mettre à jour le premier existant
            timeRestriction.setStartTime(request.getStartTime());
            timeRestriction.setEndTime(request.getEndTime());
            if (request.getAllowTime() != null) {
                timeRestriction.setAllow(request.getAllowTime());
            }
            timeRestrictionService.save(timeRestriction);
        }
    }

    public void deleteRestrictions(String domain, String extension) {

        // 1️⃣ Récupérer le profil SIP
        SipProfile profile = sipUserService.findByDomainAndExtension(domain, extension)
                .orElseThrow(() -> new RuntimeException(
                        "Profil SIP introuvable : " + extension + "@" + domain
                ));

        // 2️⃣ Supprimer CallLimit
        CallLimit callLimit = callLimitService.getBySipProfile(profile.getId());
        if (callLimit != null) {
            callLimitService.delete(callLimit.getId());
        }

        // 3️⃣ Supprimer toutes les DialPlanRestrictions
        List<DialPlanRestriction> dialPlans = restrictionService.getBySipProfile(profile.getId());
        for (DialPlanRestriction dp : dialPlans) {
            restrictionService.delete(dp.getId());
        }

        // 4️⃣ Supprimer toutes les TimeRestrictions
        List<TimeRestriction> timeRestrictions = timeRestrictionService.getBySipProfile(profile.getId());
        for (TimeRestriction tr : timeRestrictions) {
            timeRestrictionService.delete(tr.getId());
        }
    }


}
