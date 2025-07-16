package com.example.tenant.Entities.UsersConfig;

import com.example.tenant.Entities.SipProfile;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "call_groups")
public class CallGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String groupName;

    private String extension; // Numéro du groupe, ex: 9000

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "call_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "sip_profile_id")
    )
    private Set<SipProfile> members = new HashSet<>();


    @ManyToOne
    @JoinColumn(name = "sip_profile_id")
    private SipProfile sipProfile;



    private boolean active = true;

    // Getters et Setters


    public SipProfile getSipProfile() {
        return sipProfile;
    }

    public void setSipProfile(SipProfile sipProfile) {
        this.sipProfile = sipProfile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Set<SipProfile> getMembers() {
        return members;
    }

    public void setMembers(Set<SipProfile> members) {
        this.members = members;
    }



    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}