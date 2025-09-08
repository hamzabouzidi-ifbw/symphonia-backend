package com.example.tenant.Services;

import com.example.tenant.Entities.DialPlanRestriction;
import com.example.tenant.Repositories.DialPlanRestrictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DialPlanRestrictionService {
    @Autowired
    private DialPlanRestrictionRepository repository;

    public List<DialPlanRestriction> getBySipProfile(Long sipProfileId) {
        return repository.findBySipProfileId(sipProfileId);
    }
    public DialPlanRestriction save(DialPlanRestriction restriction) {
        return repository.save(restriction);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

