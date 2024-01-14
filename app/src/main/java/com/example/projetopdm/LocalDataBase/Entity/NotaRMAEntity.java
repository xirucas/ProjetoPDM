package com.example.projetopdm.LocalDataBase.Entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notas_rma")
public class NotaRMAEntity {
    @PrimaryKey
    private int id;
    private String titulo;
    private String dataCriacao;
    private String nota;
    private int imagemNotaId;
    private String imagemNota;
    private int RMAId;
    public NotaRMAEntity(int id,String titulo ,String dataCriacao ,String nota, String imagemNota, int rmaId) {
        this.id = id;
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
        this.nota = nota;
        this.imagemNota = imagemNota;
        RMAId = rmaId;
    }
    public NotaRMAEntity(){

    }
    public void setId(int id) {
        this.id = id;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public void setImagemNota(String imagemNota) {
        this.imagemNota = imagemNota;
    }

    public void setImagemNotaId(int imagemNotaId) {
        this.imagemNotaId = imagemNotaId;
    }

    public void setRMAId(int RMAId) {
        this.RMAId = RMAId;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getId() {
        return id;
    }

    public int getImagemNotaId() {
        return imagemNotaId;
    }

    public int getRMAId() {
        return RMAId;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public String getImagemNota() {
        return imagemNota;
    }

    public String getNota() {
        return nota;
    }

    public String getTitulo() {
        return titulo;
    }
}
