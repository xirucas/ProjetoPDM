package com.example.projetopdm.LocalDataBase.Entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rmas")
public class RMAEntity {
    @PrimaryKey
    private int id;
    private String RMA;
    private String descricaoCliente;
    private String dataCriacao;
    private String dataAbertura;
    private String dataFecho;
    private String estadoRMA;
    private int estadoRMAId;
    private int funcionarioId;

    public RMAEntity(int id, String rma, String descricaoCliente, String dataCriacao, String dataAbertura, String dataFecho, String estadoRMA, int estadoRMAId, int funcionarioId) {
        this.id = id;
        this.RMA = rma;
        this.descricaoCliente = descricaoCliente;
        this.dataCriacao = dataCriacao;
        this.dataAbertura = dataAbertura;
        this.dataFecho = dataFecho;
        this.estadoRMA = estadoRMA;
        this.estadoRMAId = estadoRMAId;
        this.funcionarioId = funcionarioId;
    }
    public RMAEntity(){

    }



    public String getDataCriacao() {
        return dataCriacao;
    }

    public int getId() {
        return id;
    }

    public int getEstadoRMAId() {
        return estadoRMAId;
    }

    public int getFuncionarioId() {
        return funcionarioId;
    }

    public String getDataAbertura() {
        return dataAbertura;
    }

    public String getDataFecho() {
        return dataFecho;
    }

    public String getDescricaoCliente() {
        return descricaoCliente;
    }

    public String getEstadoRMA() {
        return estadoRMA;
    }

    public String getRMA() {
        return RMA;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDataAbertura(String dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public void setDataFecho(String dataFecho) {
        this.dataFecho = dataFecho;
    }

    public void setDescricaoCliente(String descricaoCliente) {
        this.descricaoCliente = descricaoCliente;
    }

    public void setEstadoRMA(String estadoRMA) {
        this.estadoRMA = estadoRMA;
    }

    public void setEstadoRMAId(int estadoRMAId) {
        this.estadoRMAId = estadoRMAId;
    }

    public void setFuncionarioId(int funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public void setRMA(String RMA) {
        this.RMA = RMA;
    }
}
