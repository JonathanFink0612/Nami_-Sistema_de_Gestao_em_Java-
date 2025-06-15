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
    private boolean ativo;

    private final List<String> imageUrls = new ArrayList<>();
    private final Map<String, Object> camposAdicionais = new HashMap<>();

    public Peca(int pecaId, String nome, String descricao, String tipoPeca, int poderComputacional, double preco, String codigoDeBarras, int quantidade, boolean ativo) {
        this.pecaId = pecaId;
        this.nome = nome;
        this.descricao = descricao;
        this.tipoPeca = tipoPeca;
        this.poderComputacional = poderComputacional;
        this.preco = preco;
        this.codigoDeBarras = codigoDeBarras;
        this.quantidade = quantidade;
        this.ativo = ativo;
    }

    public Peca(String nome, String descricao, double preco, int quantidade, String tipoPeca, String codigoDeBarras, int poderComputacional) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidade = quantidade;
        this.tipoPeca = tipoPeca;
        this.codigoDeBarras = codigoDeBarras;
        this.poderComputacional = poderComputacional;
        this.ativo = true;
    }

    // --- Getters e Setters ---
    public int getPecaId() { return pecaId; }
    public void setPecaId(int pecaId) { this.pecaId = pecaId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; } // Adicionado
    public String getCodigoDeBarras() { return codigoDeBarras; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; } // Adicionado
    public String getTipoPeca() { return tipoPeca; }
    public void setTipoPeca(String tipo) { this.tipoPeca = tipo; } // Adicionado
    public int getPoderComputacional() { return poderComputacional; }
    public void setPoderComputacional(int poder) { this.poderComputacional = poder; } // Adicionado
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; } // Adicionado
    public int getQuantidade() { return quantidade; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public List<String> getImageUrls() { return imageUrls; }
    public void addImageUrl(String url) { this.imageUrls.add(url); }
    public Map<String, Object> getCamposAdicionais() { return camposAdicionais; }
    public void setCampoAdicional(String key, Object value) { this.camposAdicionais.put(key, value); }
}
