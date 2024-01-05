package com.example.projetopdm.Modelos;

public class Funcionario {
    int Id;
    String GUID;
    String Nome;
    String Email;
    String Contacto;
    String Pin;
    int ImagemFuncionario;


    public Funcionario() {

    }

    public Funcionario(int id, String GUID, String nome, String email, String contacto, String pin, int imagemFuncionario) {
        Id = id;
        this.GUID = GUID;
        Nome = nome;
        Email = email;
        Contacto = contacto;
        Pin = pin;
        ImagemFuncionario = imagemFuncionario;
    }

    public Funcionario(String GUID, String nome, String email, String contacto, String pin, int imagemFuncionario) {
        this.GUID = GUID;
        Nome = nome;
        Email = email;
        Contacto = contacto;
        Pin = pin;
        ImagemFuncionario = imagemFuncionario;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String nome) {
        Nome = nome;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getContacto() {
        return Contacto;
    }

    public void setContacto(String contacto) {
        Contacto = contacto;
    }

    public String getPin() {
        return Pin;
    }

    public void setPin(String pin) {
        Pin = pin;
    }

    public int getImagemFuncionario() {
        return ImagemFuncionario;
    }

    public void setImagemFuncionario(int imagemFuncionario) {
        ImagemFuncionario = imagemFuncionario;
    }

    @Override
    public String toString() {
        return "Funcionario{" +
                "Id=" + Id +
                ", GUID='" + GUID + '\'' +
                ", Nome='" + Nome + '\'' +
                ", Email='" + Email + '\'' +
                ", Contacto='" + Contacto + '\'' +
                ", Pin='" + Pin + '\'' +
                ", ImagemFuncionario=" + ImagemFuncionario +
                '}';
    }

}
