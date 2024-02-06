package com.example.projetopdm.Modelos;

import com.example.projetopdm.LocalDataBase.Entity.RMAEntity;

public class RMA {
    int Id;
    String RMA;
    String DescricaoCliente;
    String DataCriacao;
    String DataAbertura;
    String DataFecho;
    String HorasTrabalhadas;
    String EstadoRMA;
    int EstadoRMAId;
    int FuncionarioId;

    public RMA(){

    }

    public RMA(int id, String rma, String descricaoCliente,String dataCriacao , String dataAbertura, String dataFecho, String estadoRMA, int estadoRMAId, int funicionarioId) {
        Id = id;
        RMA = rma;
        DescricaoCliente = descricaoCliente;
        DataCriacao = dataCriacao;
        DataAbertura = dataAbertura;
        DataFecho = dataFecho;
        EstadoRMA = estadoRMA;
        EstadoRMAId = estadoRMAId;
        FuncionarioId = funicionarioId;
    }

    public RMA(String rma, String descricaoCliente,String dataCriacao , String dataAbertura, String dataFecho, String estadoRMA, int estadoRMAId, int funicionarioId) {
        RMA = rma;
        DescricaoCliente = descricaoCliente;
        DataCriacao = dataCriacao;
        DataAbertura = dataAbertura;
        DataFecho = dataFecho;
        EstadoRMA = estadoRMA;
        EstadoRMAId = estadoRMAId;
        FuncionarioId = funicionarioId;
    }

    public RMAEntity toRMAEntity(){
        return new RMAEntity(this.Id,this.RMA,this.DescricaoCliente,this.DataCriacao,this.DataAbertura,this.DataFecho,this.EstadoRMA,this.EstadoRMAId,this.FuncionarioId);
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getRMA() {
        return RMA;
    }

    public void setRMA(String RMA) {
        this.RMA = RMA;
    }

    public String getDescricaoCliente() {
        return DescricaoCliente;
    }

    public void setDescricaoCliente(String descricaoCliente) {
        DescricaoCliente = descricaoCliente;
    }

    public String getDataCriacao() {
        return DataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        DataCriacao = dataCriacao;
    }

    public String getDataAbertura() {
        return DataAbertura;
    }

    public void setDataAbertura(String dataAbertura) {
        DataAbertura = dataAbertura;
    }

    public String getDataFecho() {
        return DataFecho;
    }

    public void setDataFecho(String dataFecho) {
        DataFecho = dataFecho;
    }

    public String getHorasTrabalhadas() {
        return HorasTrabalhadas;
    }

    public void setHorasTrabalhadas(String horasTrabalhadas) {
        HorasTrabalhadas = horasTrabalhadas;
    }

    public String getEstadoRMA() {
        return EstadoRMA;
    }

    public void setEstadoRMA(String estadoRMA) {
        EstadoRMA = estadoRMA;
    }

    public int getEstadoRMAId() {
        return EstadoRMAId;
    }

    public void setEstadoRMAId(int estadoRMAId) {
        EstadoRMAId = estadoRMAId;
    }

    public int getFuncionarioId() {
        return FuncionarioId;
    }

    public void setFuncionarioId(int funcionarioId) {
        FuncionarioId = funcionarioId;
    }

    @Override
    public String toString() {
        return "RMA{" +
                "Id=" + Id +
                ", RMA='" + RMA + '\'' +
                ", DescricaoCliente='" + DescricaoCliente + '\'' +
                ", DataAbertura='" + DataAbertura + '\'' +
                ", DataFecho='" + DataFecho + '\'' +
                ", EstadoRMA='" + EstadoRMA + '\'' +
                ", EstadoRMAId=" + EstadoRMAId +
                ", FunicionarioId=" + FuncionarioId +
                '}';
    }
}
