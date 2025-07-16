package com.example.tenant.Dto;

import java.util.List;

public class CallGroupRequest {
    private String groupName;
    private String extension;
    private Long tenantId;
    private List<Long> sipProfileIds;

    // Getters & Setters

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

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public List<Long> getSipProfileIds() {
        return sipProfileIds;
    }

    public void setSipProfileIds(List<Long> sipProfileIds) {
        this.sipProfileIds = sipProfileIds;
    }
}

