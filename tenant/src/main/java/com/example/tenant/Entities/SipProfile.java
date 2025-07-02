    package com.example.tenant.Entities;

    import javax.persistence.*;
    import java.time.LocalDateTime;
    import java.util.UUID;

    @Entity
    @Table(name = "sip_profiles")
    public class SipProfile {

        @Id
        @GeneratedValue
        private Long id;

        private String username;
        private String email;



        @Column(unique = true)
        private String extension;

        @Column(nullable = false)
        private Long tenantId;

        @Column(nullable = false)
        private String domainName;


        @Column(nullable = false)
        private String password;

        private UUID licenceDefinitionId;

        private LocalDateTime creationDate;

        private boolean active = true;





        @PrePersist
        protected void onCreate() {
            creationDate = LocalDateTime.now();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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

        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public UUID getLicenceDefinitionId() {
            return licenceDefinitionId;
        }

        public void setLicenceDefinitionId(UUID licenceDefinitionId) {
            this.licenceDefinitionId = licenceDefinitionId;
        }

        public LocalDateTime getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }