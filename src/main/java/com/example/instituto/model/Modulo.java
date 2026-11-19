package com.example.instituto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity 
public class Modulo {
    @Id 
    private int id;
    private String nombre;
    
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    

}
