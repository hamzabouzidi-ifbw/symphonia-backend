package com.example.tenant.Services;

import com.example.tenant.Entities.TimeRestriction;
import com.example.tenant.Repositories.TimeRestrictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeRestrictionService {
    @Autowired
    private TimeRestrictionRepository repository;


    public TimeRestriction save(TimeRestriction timeRestriction) {
        return repository.save(timeRestriction);
    }

    public List<TimeRestriction> getBySipProfile(Long sipProfileId) {
        return repository.findAll().stream()
                .filter(t -> t.getSipProfile().getId().equals(sipProfileId))
                .collect(Collectors.toList());
    }
    public void delete(Long id) {
        repository.deleteById(id);
    }
}

