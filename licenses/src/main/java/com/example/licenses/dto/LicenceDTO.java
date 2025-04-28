package com.example.licenses.dto;

import com.example.licenses.Entities.LicenceDefinition;

import java.util.UUID;

public class LicenceDTO {

        private UUID id;
        private LicenceDefinition licence;

        // Constructeur avec tous les arguments
        public LicenceDTO( UUID id, LicenceDefinition licence) {
            this.id = id;
            this.licence = licence;
        }



        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public LicenceDefinition getLicence() {
            return licence;
        }

        public void setLicence(LicenceDefinition licence) {
            this.licence = licence;
        }

        // Optionnel : pour le debug
        @Override
        public String toString() {
            return "LicenceDTO{" +

                    ", id=" + id +
                    ", licence=" + licence +
                    '}';
        }

}
