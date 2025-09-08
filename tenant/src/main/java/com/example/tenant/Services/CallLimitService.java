package com.example.tenant.Services;

import com.example.tenant.Entities.CallLimit;
import com.example.tenant.Repositories.CallLimitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CallLimitService {
    @Autowired
    private CallLimitRepository repository;

    public CallLimit getBySipProfile(Long sipProfileId) {
        return repository.findBySipProfileId(sipProfileId).orElse(null);
    }


    public CallLimit save(CallLimit callLimit) {
        return repository.save(callLimit);
    }
    public void delete(Long id) {
        repository.deleteById(id);
    }

}

