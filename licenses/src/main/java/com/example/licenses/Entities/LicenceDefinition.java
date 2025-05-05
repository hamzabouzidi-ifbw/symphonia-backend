package com.example.licenses.Entities;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "licence_definition ")
public class LicenceDefinition {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private LicenceType type;

    @ElementCollection
    private List<String> features; // définies une seule fois

    @Column(unique = true, nullable = false)
    private String key; // La clé ou le code de la licence


    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LicenceType getType() { return type; }
    public void setType(LicenceType type) { this.type = type; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}