package com.example.licenses.Scheduler;

import com.example.licenses.Services.LicenceDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class LicenseScheduler {

    private final LicenceDefinitionService licenseService;

    @Autowired
    public LicenseScheduler(LicenceDefinitionService licenseService) {
        this.licenseService = licenseService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateStatuses() {
        licenseService.checkAndUpdateStatuses();
    }
}