package com.example.projetopdm.LocalDataBase.Entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "funcionarios")
public class FuncionarioEntity {
    @PrimaryKey
    private int id;
    private String GUID;
    private String nome;
    private String email;
    private String contacto;
    private String pin;
    private String imagemFuncionario;
    private int estadoFuncionarioId;
    private String estadoFuncionario;

    public int getId() {
        return id;
    }

    public int getEstadoFuncionarioId() {
        return estadoFuncionarioId;
    }

    public String getContacto() {
        return contacto;
    }

    public String getEmail() {
        return email;
    }

    public String getEstadoFuncionario() {
        return estadoFuncionario;
    }

    public String getGUID() {
        return GUID;
    }

    public String getImagemFuncionario() {
        return imagemFuncionario;
    }

    public String getNome() {
        return nome;
    }

    public String getPin() {
        return pin;
    }

    public void setImagemFuncionario(String imagemFuncionario) {
        this.imagemFuncionario = imagemFuncionario;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEstadoFuncionario(String estadoFuncionario) {
        this.estadoFuncionario = estadoFuncionario;
    }

    public void setEstadoFuncionarioId(int estadoFuncionarioId) {
        this.estadoFuncionarioId = estadoFuncionarioId;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
