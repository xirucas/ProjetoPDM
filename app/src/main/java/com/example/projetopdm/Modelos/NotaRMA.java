package com.example.projetopdm.Modelos;

public class NotaRMA {
    int Id;
    String Nota;
    int ImagemNotaId;
    String ImagemNota;
    int RMAId;

    public NotaRMA(){

    }

    public NotaRMA(int id, String nota, String imagemNota, int rmaId) {
        Id = id;
        Nota = nota;
        ImagemNota = imagemNota;
        RMAId = rmaId;
    }

    public NotaRMA(int id, String nota,int imagemNotaId ,String imagemNota, int rmaId) {
        Id = id;
        Nota = nota;
        ImagemNotaId = imagemNotaId;
        ImagemNota = imagemNota;
        RMAId = rmaId;
    }

    public NotaRMA(String nota, String imagemNota, int rmaId) {
        Nota = nota;
        ImagemNota = imagemNota;
        RMAId = rmaId;
    }

    public NotaRMA(String nota, int rmaId) {
        Nota = nota;
        RMAId = rmaId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
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