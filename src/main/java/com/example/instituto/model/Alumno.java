package com.example.Instituto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String nombre;
    String apellidos;
    String curso;

    public Alumno() {
    }


    public Alumno(int id) {
        this.id = id;
    }


    public Alumno(String nombre, String apellidos) {
        this.nombre = nombre;
        apellidos = apellidos;
    }

    public Alumno(int id, String nombre, String apellidos) {
        this.id = id;
        this.nombre = nombre;
        apellidos = apellidos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        apellidos = apellidos;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    @Override
    public String toString() {
        return "Alumno [id=" + id + ", nombre=" + nombre + ", Apellidos=" + apellidos + ", curso=" + curso + "]";
    }

}
