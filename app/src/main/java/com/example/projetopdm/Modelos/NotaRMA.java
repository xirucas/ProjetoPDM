package com.example.projetopdm.Modelos;

import com.example.projetopdm.LocalDataBase.Entity.NotaRMAEntity;

public class NotaRMA {
    int Id;
    String Titulo;
    String dataCriacao;
    String Nota;
    int ImagemNotaId;
    String ImagemNota;
    int RMAId;

    public NotaRMA(){

    }

    public NotaRMA(int id,String titulo ,String dataCriacao ,String nota, String imagemNota, int rmaId) {
        Id = id;
        Titulo = titulo;
        this.dataCriacao = dataCriacao;
        Nota = nota;
        ImagemNota = imagemNota;
        RMAId = rmaId;
    }

    public NotaRMA(int id,String titulo,String dataCriacao, String nota,int imagemNotaId ,String imagemNota, int rmaId) {
        Id = id;
        Titulo = titulo;
        this.dataCriacao = dataCriacao;
        Nota = nota;
        ImagemNotaId = imagemNotaId;
        ImagemNota = imagemNota;
        RMAId = rmaId;
    }

    public NotaRMA(String titulo,String dataCriacao, String nota, String imagemNota, int rmaId) {
        Titulo = titulo;
        this.dataCriacao = dataCriacao;
        Nota = nota;
        ImagemNota = imagemNota;
        RMAId = rmaId;
    }

    public NotaRMA(String titulo,String dataCriacao, String nota, int rmaId) {
        Titulo = titulo;
        this.dataCriacao = dataCriacao;
        Nota = nota;
        RMAId = rmaId;
    }

    public NotaRMA(int notaId, int rmaId, String titulo, String dataCriacao, String nota) {
        Id = notaId;
        RMAId = rmaId;
        Titulo = titulo;
        this.dataCriacao = dataCriacao;
        Nota = nota;
    }

    public NotaRMAEntity toNotaRMAEntity() {
        return new NotaRMAEntity(this.Id, this.Titulo, this.dataCriacao, this.Nota, this.ImagemNota, this.RMAId,this.ImagemNotaId);
    }


    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo(String titulo) {
        Titulo = titulo;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getNota() {
        return Nota;
    }

    public void setNota(String nota) {
        Nota = nota;
    }

    public int getImagemNotaId() {
        return ImagemNotaId;
    }

    public void setImagemNotaId(int imagemNotaId) {
        ImagemNotaId = imagemNotaId;
    }

    public String getImagemNota() {
        return ImagemNota;
    }

    public void setImagemNota(String imagemNota) {
        ImagemNota = imagemNota;
    }

    public int getRMAId() {
        return RMAId;
    }

    public void setRMAId(int RMAId) {
        this.RMAId = RMAId;
    }

    @Override

    public String toString() {
        return "NotaRMA{" +
                "Id=" + Id +
                ", Nota='" + Nota + '\'' +
                ", ImagemNota='" + ImagemNota + '\'' +
                ", RMAId=" + RMAId +
                '}';
    }
}