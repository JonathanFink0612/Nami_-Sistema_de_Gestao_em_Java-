package com.exemplo.models;

public class Senha {
    private final String valor;

    public Senha(String valor) {
        this.valor = valor != null ? valor.trim() : "";
    }

    public boolean estaVazio() {
        return valor.isEmpty();
    }

    public String getValor() {
        return valor;
    }
}
