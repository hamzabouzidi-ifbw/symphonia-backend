package com.example.tenant.Entities.UsersConfig;

public enum CallGroupStrategy {

    RING_ALL,    // Tous les agents sonnent en même temps
    LINEAR,      // Ordre fixe
    ROUND_ROBIN  // Distribution cyclique
}
