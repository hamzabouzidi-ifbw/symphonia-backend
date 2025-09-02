package com.example.tenant.Services.UsersConfig;

import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.UsersConfig.CallGroup;
import com.example.tenant.Repositories.CallGroupRepository;
import com.example.tenant.Repositories.SipProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CallGroupService {
    @Autowired
    private CallGroupRepository callGroupRepository;

    @Autowired
    private SipProfileRepository sipProfileRepository;

    public CallGroup createCallGroupWithMembers(String groupName, String extension, List<Long> sipProfileIds) {
        CallGroup callGroup = new CallGroup();
        callGroup.setGroupName(groupName);
        callGroup.setExtension(extension);
        callGroup.setActive(true);

        // Récupérer les membres SIP depuis leurs IDs
        Set<SipProfile> members = new HashSet<>(sipProfileRepository.findAllById(sipProfileIds));

        callGroup.setMembers(members);

        // Sauvegarder
        return callGroupRepository.save(callGroup);
    }
    public List<CallGroup> getAllCallGroupsWithMembers() {
        return callGroupRepository.findAll(); // Lazy loading à gérer dans JSON
    }
   /* public CallGroup updateCallGroup(Long groupId, String newName, String newExtension) {
        CallGroup group = callGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("CallGroup not found"));

        if (newName != null) group.setGroupName(newName);
        if (newExtension != null) group.setExtension(newExtension);

        return callGroupRepository.save(group);
    }*/
   public CallGroup updateCallGroup(Long groupId, String newName, String newExtension, List<Long> sipProfileIds) {
       CallGroup group = callGroupRepository.findById(groupId)
               .orElseThrow(() -> new RuntimeException("CallGroup not found"));

       if (newName != null) group.setGroupName(newName);
       if (newExtension != null) group.setExtension(newExtension);

       if (sipProfileIds != null && !sipProfileIds.isEmpty()) {
           Set<SipProfile> newMembers = new HashSet<>(sipProfileRepository.findAllById(sipProfileIds));
           group.getMembers().addAll(newMembers); // ajoute les nouveaux sans supprimer les anciens
       }

       return callGroupRepository.save(group);
   }

    public CallGroup addMembersToGroup(Long groupId, List<Long> sipProfileIds) {
        CallGroup group = callGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("CallGroup not found"));

        Set<SipProfile> newMembers = new HashSet<>(sipProfileRepository.findAllById(sipProfileIds));
        group.getMembers().addAll(newMembers);

        return callGroupRepository.save(group);
    }
    public void deleteCallGroup(Long groupId) {
        callGroupRepository.deleteById(groupId);
    }
    public CallGroup removeMemberFromGroup(Long groupId, Long sipProfileId) {
        CallGroup group = callGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        SipProfile member = sipProfileRepository.findById(sipProfileId)
                .orElseThrow(() -> new RuntimeException("SipProfile not found"));

        group.getMembers().remove(member);
        return callGroupRepository.save(group); // ⬅️ Nécessaire pour que les changements soient persistés
    }


}
