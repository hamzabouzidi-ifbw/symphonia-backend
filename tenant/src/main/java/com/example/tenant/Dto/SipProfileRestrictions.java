package com.example.tenant.Dto;

import com.example.tenant.Entities.CallLimit;
import com.example.tenant.Entities.DialPlanRestriction;
import com.example.tenant.Entities.SipProfile;
import com.example.tenant.Entities.TimeRestriction;

import java.util.List;

public class SipProfileRestrictions {
    private SipProfile profile;
    private List<CallLimit> callLimits;
    private List<DialPlanRestriction> dialPlanRestrictions;
    private List<TimeRestriction> timeRestrictions;

    public SipProfileRestrictions(SipProfile profile,
                                  List<CallLimit> callLimits,
                                  List<DialPlanRestriction> dialPlanRestrictions,
                                  List<TimeRestriction> timeRestrictions) {
        this.profile = profile;
        this.callLimits = callLimits;
        this.dialPlanRestrictions = dialPlanRestrictions;
        this.timeRestrictions = timeRestrictions;
    }

    public SipProfile getProfile() { return profile; }
    public List<CallLimit> getCallLimits() { return callLimits; }
    public List<DialPlanRestriction> getDialPlanRestrictions() { return dialPlanRestrictions; }
    public List<TimeRestriction> getTimeRestrictions() { return timeRestrictions; }
}
