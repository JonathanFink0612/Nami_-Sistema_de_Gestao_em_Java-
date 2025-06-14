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
    // Campos para criação
    private double preco;
    private int quantidade;
    private String codigoDeBarras;

    // Campos para visualização e upload
    private final List<String> imageUrls = new ArrayList<>();
    private final Map<String, Object> camposAdicionais = new HashMap<>();

    // Construtor para buscar dados do banco para visualização
    public Peca(int pecaId, String nome, String descricao, String tipoPeca, int poderComputacional, double preco , String codigoDeBarras) {
        this.pecaId = pecaId;
        this.nome = nome;
        this.descricao = descricao;
        this.tipoPeca = tipoPeca;
        this.poderComputacional = poderComputacional;
        this.preco = preco;
        this.codigoDeBarras = codigoDeBarras;
    }

    // Construtor para criar uma nova peça na tela de cadastro
    public Peca(String nome, String descricao, double preco, int quantidade, String tipoPeca, String codigoDeBarras, int poderComputacional ) {
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
    public List<String> getImageUrls() { return imageUrls; }
    public void addImageUrl(String url) { this.imageUrls.add(url); }
    public Map<String, Object> getCamposAdicionais() { return camposAdicionais; } // Método restaurado
    public void setCampoAdicional(String key, Object value) { this.camposAdicionais.put(key, value); }
    // Adicione estes métodos dentro da sua classe Peca.java


    public int getQuantidade() {
        return quantidade;
    }


}