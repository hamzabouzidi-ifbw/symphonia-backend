package com.example.licenses.Schedulers;

import com.example.licenses.Services.LicenceDefinitionService;
import com.example.licenses.Services.LicenceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LicenceExpirationScheduler {
    @Autowired
    private LicenceDefinitionService licenceService;


    @Scheduled(cron = "0 0 1 * * *") // Tous les jours à 01:00
    public void checkLicencesExpiration() {
        licenceService.checkExpiredLicences();
    }
}