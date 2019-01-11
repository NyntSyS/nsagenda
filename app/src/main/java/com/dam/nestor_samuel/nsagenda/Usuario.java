package com.dam.nestor_samuel.nsagenda;

import android.os.Parcel;
import android.os.Parcelable;

public class Usuario implements Parcelable {

    private int id;
    private String nick;
    private String email;
    private String nombre;
    private String apellidos;
    private String password;

    public Usuario(String nick, String email, String nombre, String apellidos){
        id = 0;
        this.nick = nick;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        password = "";
    }

    public Usuario(int id, String nick, String email, String nombre, String apellidos){
        this.id = id;
        this.nick = nick;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        password = "";
    }

    public Usuario(int id, String nick, String email, String nombre, String apellidos, String password){
        this.id = id;
        this.nick = nick;
        this.email = email;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }

    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public void copiarUsuario(Usuario usuario, String passwordMD5) {
        id = usuario.id;
        nick = usuario.nick;
        email = usuario.email;
        nombre = usuario.nombre;
        apellidos = usuario.apellidos;
        if(!passwordMD5.isEmpty())
            password = passwordMD5;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.nick);
        dest.writeString(this.email);
        dest.writeString(this.nombre);
        dest.writeString(this.apellidos);
        dest.writeString(this.password);
    }

    protected Usuario(Parcel in) {
        this.id = in.readInt();
        this.nick = in.readString();
        this.email = in.readString();
        this.nombre = in.readString();
        this.apellidos = in.readString();
        this.password = in.readString();
    }

    public static final Parcelable.Creator<Usuario> CREATOR = new Parcelable.Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel source) {
            return new Usuario(source);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };
}
