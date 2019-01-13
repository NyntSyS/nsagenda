package com.dam.nestor_samuel.nsagenda;

import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.LocalDateTime;

public class Tarea implements Parcelable {

    private int id;
    private int color;
    private String nombreTarea;
    private String descripcion;
    private LocalDateTime fechaTarea;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.color);
        dest.writeString(this.nombreTarea);
        dest.writeString(this.descripcion);
        dest.writeSerializable(this.fechaTarea);
    }

    protected Tarea(Parcel in) {
        this.id = in.readInt();
        this.color = in.readInt();
        this.nombreTarea = in.readString();
        this.descripcion = in.readString();
        this.fechaTarea = (LocalDateTime) in.readSerializable();
    }

    public static final Parcelable.Creator<Tarea> CREATOR = new Parcelable.Creator<Tarea>() {
        @Override
        public Tarea createFromParcel(Parcel source) {
            return new Tarea(source);
        }

        @Override
        public Tarea[] newArray(int size) {
            return new Tarea[size];
        }
    };
}
