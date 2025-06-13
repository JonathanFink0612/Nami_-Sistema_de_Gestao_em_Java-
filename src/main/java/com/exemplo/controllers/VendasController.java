package com.exemplo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.TilePane;

import java.net.URL;
import java.util.ResourceBundle;

public class VendasController implements Initializable {

    // Referência ao grid de produtos para que possamos adicionar itens dinamicamente no futuro
    @FXML
    private TilePane productGrid;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Tela de Vendas carregada!");
        // Futuramente, aqui você chamaria um método para carregar os produtos do banco
        // e popular o 'productGrid'.
    }
}