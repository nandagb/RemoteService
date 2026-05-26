package br.imd.ufrn.application.models;

public class User {
    public String name;
    public String number;
    public int id;

    public User(){

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.number;
    }

    public int getId() {
        return this.id;
    }
}
