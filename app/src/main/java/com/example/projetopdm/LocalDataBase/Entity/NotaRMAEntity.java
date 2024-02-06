package com.example.projetopdm.LocalDataBase.Entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.projetopdm.Modelos.NotaRMA;

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
    private String offSync; // novo/modificado/null

    public NotaRMAEntity(int id,String titulo ,String dataCriacao ,String nota, String imagemNota, int rmaId,int imagemNotaId) {
        this.id = id;
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
        this.nota = nota;
        this.imagemNota = imagemNota;
        RMAId = rmaId;
        this.imagemNotaId=imagemNotaId;
    }

    public NotaRMAEntity(){

    }

    public void setOffSync(String offSync) {
        this.offSync = offSync;
    }

    public String getOffSync() {
        return offSync;
    }
    public boolean equals(NotaRMA x) {
        if (x.getRMAId()==this.RMAId && x.getId()==this.id && x.getNota().equals( this.nota) && x.getTitulo().equals(this.titulo) && x.getImagemNota().equals(this.imagemNota)){
            return true;
        }else {
            return false;
        }
    }

    public NotaRMA toNotaRMA(){
        return new NotaRMA(this.id,this.titulo,this.dataCriacao,this.nota,this.imagemNotaId,this.imagemNota,this.RMAId);
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
