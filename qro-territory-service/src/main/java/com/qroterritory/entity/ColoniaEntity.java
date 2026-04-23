package com.qroterritory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "colonias")
public class ColoniaEntity extends PanacheEntityBase {

    @Id
    public Long id;

    public String nombre;
    public String codigoPostal;

    // Aquí ocurre la magia de la relación relacional
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegacion_id")
    public DelegacionEntity delegacion;
}