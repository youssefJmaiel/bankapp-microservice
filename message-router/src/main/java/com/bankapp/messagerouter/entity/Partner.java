package com.bankapp.messagerouter.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;


import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Partner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerDirection direction;

    private String application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessedFlowType processedFlowType;

    @Column(nullable = false)
    private String description;

    @JsonIgnore  // Ignore this method during serialization
    public boolean isValid() {
        // Vérifie que tous les champs obligatoires sont non null et non vides
        return alias != null && !alias.isBlank() &&
                type != null && !type.toString().isBlank() &&  // Vérifie que le type est non nul et valide
                direction != null && !direction.toString().isBlank() &&  // Vérifie que la direction est valide
                processedFlowType != null && !processedFlowType.toString().isBlank() &&  // Vérifie que le flow type est valide
                description != null && !description.isBlank();  // Vérifie que la description est non nulle et non vide
    }




}

