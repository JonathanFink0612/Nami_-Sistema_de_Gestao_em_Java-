package com.exemplo.models;

public class MovimentacaoEstoque {
    public enum Tipo { ENTRADA, SAIDA }

    private int movId;
    private int pecaId;
    private Tipo tipoMovimentacao;
    private int quantidade;
    private Integer vendaId; // Pode ser nulo para entradas manuais

    public MovimentacaoEstoque(int pecaId, Tipo tipoMovimentacao, int quantidade, Integer vendaId) {
        this.pecaId = pecaId;
        this.tipoMovimentacao = tipoMovimentacao;
        this.quantidade = quantidade;
        this.vendaId = vendaId;
    }
    // Getters
    public int getPecaId() { return pecaId; }
    public String getTipoMovimentacao() { return tipoMovimentacao.name(); }
    public int getQuantidade() { return quantidade; }
    public Integer getVendaId() { return vendaId; }
}