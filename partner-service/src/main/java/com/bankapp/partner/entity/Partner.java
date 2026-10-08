package com.bankapp.partner.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "partners")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.bankapp.partner.dto.PartnerType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.bankapp.partner.dto.PartnerDirection direction;

    private String application;

    @Enumerated(EnumType.STRING)
    @Column(name = "processed_flow_type", nullable = false)
    private com.bankapp.partner.dto.ProcessedFlowType processedFlowType;

    @Column(nullable = false)
    private String description;
}
