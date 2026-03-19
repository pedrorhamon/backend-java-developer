package com.cmanager.app.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "episode")
@Data
public class Episode {

    @Id
    private String id;

    @Column(name = "id_integration")
    private Integer idIntegration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_show", nullable = false)
    private Show show;

    @Column(name = "name")
    private String name;

    @Column(name = "season")
    private Integer season;

    @Column(name = "number")
    private Integer number;

    @Column(name = "type")
    private String type;

    @Column(name = "airdate")
    private String airdate;

    @Column(name = "airtime")
    private String airtime;

    @Column(name = "airstamp", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime airstamp;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "rating", precision = 5, scale = 2)
    private BigDecimal rating;

    @Column(name = "summary")
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        final OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.id = UUID.randomUUID().toString();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
