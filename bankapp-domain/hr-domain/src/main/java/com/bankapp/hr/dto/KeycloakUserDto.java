package com.bankapp.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Informations minimales d'un utilisateur réel du realm Keycloak.
 * Ne contient aucun secret ni jeton.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
}
