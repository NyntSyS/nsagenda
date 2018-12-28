package com.dam.nestor_samuel.nsagenda;

public class Usuario {
    int id;
    String nick;
    String email;

    public Usuario(String nick,String email){
        id = 0;
        this.nick = nick;
        this.email = email;
    }

    public Usuario(int id,String nick,String email){
        this.id = id;
        this.nick = nick;
        this.email = email;
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
}
