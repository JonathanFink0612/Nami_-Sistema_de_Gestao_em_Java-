package com.exemplo.models;

public class VendaItem {
    private int itemId;
    private int vendaId;
    private Peca peca; // Usamos o objeto Peca para ter acesso a todos os seus dados
    private int quantidade;
    private double precoUnitario;

    public VendaItem(Peca peca, int quantidade) {
        this.peca = peca;
        this.quantidade = quantidade;
        this.precoUnitario = peca.getPreco();
    }

    // Getters e Setters
    public Peca getPeca() { return peca; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public double getSubtotal() { return this.quantidade * this.precoUnitario; }
    public String getNomePeca() { return this.peca.getNome(); }
}