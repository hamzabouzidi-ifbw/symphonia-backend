package com.example.licenses.dto;



import com.example.licenses.Entities.LicenceType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LicenceDefinitionDTO {
    private UUID id;
    private LicenceType type;
    private List<String> features;
    private String key;


}