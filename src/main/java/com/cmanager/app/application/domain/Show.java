package com.cmanager.app.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "SHOW")
@Data
@EqualsAndHashCode(callSuper = false)
public class Show extends BaseEntity {

    @Id
    private String id;

    @Column(name = "ID_INTEGRATION")
    private Integer idIntegration;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "LANGUAGE")
    private String language;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "RUNTIME")
    private Integer runtime;

    @Column(name = "AVERAGE_RUNTIME")
    private Integer averageRuntime;

    @Column(name = "OFFICIAL_SITE")
    private String officialSite;

    @Column(name = "RATING", precision = 5, scale = 2)
    private BigDecimal rating;

    @Column(name = "SUMMARY")
    private String summary;

    @PrePersist
    @Override
    protected void prePersist() {
        super.prePersist();
        this.id = generateId();
    }
}
