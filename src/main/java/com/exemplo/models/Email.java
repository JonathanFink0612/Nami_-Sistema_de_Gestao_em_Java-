package com.exemplo.models;

import java.util.regex.Pattern;

public class Email {
    private static final Pattern PADRAO_EMAIL = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String valor;
    private final String erro;

    public Email(String valor) {
        String valorFormatado = valor != null ? valor.trim() : "";
        if (valorFormatado.isEmpty()) {
            this.valor = "";
            this.erro = "O campo de e-mail está vazio.";
        } else if (!PADRAO_EMAIL.matcher(valorFormatado).matches()) {
            this.valor = valorFormatado;
            this.erro = "Formato de e-mail inválido: " + valorFormatado;
        } else {
            this.valor = valorFormatado;
            this.erro = null;
        }
    }

    public boolean estaVazio() {
        return valor.isEmpty();
    }

    public boolean temErro() {
        return erro != null;
    }

    public String getErro() {
        return erro;
    }

    public String getValor() {
        return valor;
    }
}
