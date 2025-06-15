package com.exemplo.models;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venda {
    private int vendaId;
    private OffsetDateTime dataVenda;
    private double valorTotal;
    private String metodoPagamento;
    private String observacoes; // <-- CAMPO ADICIONADO
    private List<VendaItem> itens;

    public Venda(double valorTotal, String metodoPagamento) {
        this.valorTotal = valorTotal;
        this.metodoPagamento = metodoPagamento;
        this.itens = new ArrayList<>();
    }

    // --- Getters e Setters ---

    public int getVendaId() { return vendaId; }
    public void setVendaId(int vendaId) { this.vendaId = vendaId; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public List<VendaItem> getItens() { return itens; }
    public void setItens(List<VendaItem> itens) { this.itens = itens; }
    public void addItem(VendaItem item) { this.itens.add(item); }

    /**
     * MÉTODO ADICIONADO: Retorna as observações da venda.
     * O TransacaoService precisa deste método para ler as observações.
     */
    public String getObservacoes() {
        return observacoes;
    }

    /**
     * MÉTODO ADICIONADO: Define as observações para a venda.
     * O PdvController precisa deste método para guardar as observações.
     */
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
