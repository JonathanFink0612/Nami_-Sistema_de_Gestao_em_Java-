package com.exemplo.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Peca {
    private int pecaId;
    private String nome;
    private String descricao;
    private String tipoPeca;
    private int poderComputacional;
    private double preco;
    private int quantidade;
    private String codigoDeBarras;
    private String observacoes;

    private final List<String> imageUrls = new ArrayList<>();
    private final Map<String, Object> camposAdicionais = new HashMap<>();

    /**
     * CONSTRUTOR 1: Para CARREGAR uma peça existente do banco de dados.
     * Usado pelo PecaSupabaseClient ao buscar produtos.
     * Note que ele começa com 'pecaId' (int) e também carrega a 'quantidade'.
     */
    public Peca(int pecaId, String nome, String descricao, String tipoPeca, int poderComputacional, double preco, String codigoDeBarras, int quantidade) {
        this.pecaId = pecaId;
        this.nome = nome;
        this.descricao = descricao;
        this.tipoPeca = tipoPeca;
        this.poderComputacional = poderComputacional;
        this.preco = preco;
        this.codigoDeBarras = codigoDeBarras;
        this.quantidade = quantidade;
    }

    /**
     * CONSTRUTOR 2: Para CRIAR uma nova peça a partir do formulário de cadastro.
     * Este é o construtor que o seu ProdutoController precisa. Note que ele começa
     * com 'nome' (String) e não com um ID.
     */
    public Peca(String nome, String descricao, double preco, int quantidade, String tipoPeca, String codigoDeBarras, int poderComputacional) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidade = quantidade;
        this.tipoPeca = tipoPeca;
        this.codigoDeBarras = codigoDeBarras;
        this.poderComputacional = poderComputacional;
    }

    // Getters e Setters
    public int getPecaId() { return pecaId; }
    public void setPecaId(int pecaId) { this.pecaId = pecaId; }
    public String getNome() { return nome; }
    public String getCodigoDeBarras() { return codigoDeBarras; }
    public String getDescricao() { return descricao; }
    public String getTipoPeca() { return tipoPeca; }
    public int getPoderComputacional() { return poderComputacional; }
    public double getPreco() { return preco; }
    public int getQuantidade() { return quantidade; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public List<String> getImageUrls() { return imageUrls; }
    public void addImageUrl(String url) { this.imageUrls.add(url); }
    public Map<String, Object> getCamposAdicionais() { return camposAdicionais; }
    public void setCampoAdicional(String key, Object value) { this.camposAdicionais.put(key, value); }
}
