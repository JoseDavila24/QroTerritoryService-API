package com.qroterritory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "colonias")
public class ColoniaEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nombre;
    public String codigoPostal;

    @Column(name = "tipo_asentamiento")
    public String tipoAsentamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegacion_id")
    public DelegacionEntity delegacion;
}