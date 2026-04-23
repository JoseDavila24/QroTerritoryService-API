package com.qroterritory.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "delegaciones")
public class DelegacionEntity extends PanacheEntityBase {
    @Id
    public Long id;
    public String nombre;
    public String sede;
}