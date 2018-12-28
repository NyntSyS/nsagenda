package com.dam.nestor_samuel.nsagenda;

import org.threeten.bp.LocalDateTime;

public class Tarea {
    int id;
    int color;
    String nombreTarea;
    String descripcion;
    LocalDateTime fechaTarea;


    public Tarea(int color, String nombreTarea, String descripcion, LocalDateTime fechaTarea) {
        id = 0;
        this.color = color;
        this.nombreTarea = nombreTarea;
        this.descripcion = descripcion;
        this.fechaTarea = fechaTarea;
    }

    public Tarea(int id,int color, String nombreTarea, String descripcion, LocalDateTime fechaTarea) {
        this.id = id;
        this.color = color;
        this.nombreTarea = nombreTarea;
        this.descripcion = descripcion;
        this.fechaTarea = fechaTarea;
    }

    public String getNombreTarea() {
        return nombreTarea;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaTarea() {
        return fechaTarea;
    }

    public void setFechaTarea(LocalDateTime fechaTarea) {
        this.fechaTarea = fechaTarea;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
