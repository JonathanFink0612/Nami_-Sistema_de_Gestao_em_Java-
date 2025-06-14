package com.exemplo.controllers;

import com.exemplo.models.Peca;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class DetalhePecaController {

    // --- Componentes FXML ---
    @FXML private Label nomeLabel;
    @FXML private Label tipoLabel;
    @FXML private Label poderLabel;
    @FXML private ImageView imagemView;
    @FXML private Label precoLabel;
    @FXML private Label quantidadeLabel;
    @FXML private TextArea descricaoTextArea;
    @FXML private Button btnComprar;
    @FXML private Button btnAnterior;
    @FXML private Button btnProxima;
    @FXML private Label lerMaisLabel;

    // --- Lógica do Carrossel ---
    private List<String> imageUrls = new ArrayList<>();
    private int imagemAtualIndex = 0;

    public void setPeca(Peca peca) {
        // Preenche os dados básicos da peça
        nomeLabel.setText(peca.getNome());
        tipoLabel.setText("Tipo: " + peca.getTipoPeca());
        poderLabel.setText("Poder: " + peca.getPoderComputacional());
        precoLabel.setText(String.format("R$ %.2f", peca.getPreco()));

        quantidadeLabel.setText("Qtd: " + peca.getQuantidade());
        descricaoTextArea.setText(peca.getDescricao());

        // Lógica do carrossel
        this.imageUrls = new ArrayList<>(peca.getImageUrls());
        imagemAtualIndex = 0;
        if (!imageUrls.isEmpty()) {
            atualizarImagemExibida();
        } else {
            imagemView.setImage(new Image("https://placehold.co/300x200/e0e0e0/757575?text=Sem+Imagem"));
            btnAnterior.setVisible(false);
            btnProxima.setVisible(false);
        }

        // Lógica para a descrição expansível
        configurarDescricaoExpansivel();
    }

    private void configurarDescricaoExpansivel() {
        // Garante que o estado inicial está correto
        lerMaisLabel.setVisible(false);
        lerMaisLabel.setManaged(false);

        // Usa Platform.runLater para garantir que o layout foi calculado antes do teste
        Platform.runLater(() -> {
            // Procura a barra de rolagem interna do TextArea
            ScrollBar verticalScrollBar = (ScrollBar) descricaoTextArea.lookup(".scroll-bar:vertical");

            // Se a barra de rolagem existe e está visível, o texto é maior que o espaço
            if (verticalScrollBar != null && verticalScrollBar.isVisible()) {
                lerMaisLabel.setVisible(true);
                lerMaisLabel.setManaged(true);
            }
        });
    }

    @FXML
    private void expandirDescricao() {
        // Remove a restrição de altura para que o TextArea cresça o necessário
        descricaoTextArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
        // Esconde o link "Ler mais" depois que ele foi clicado
        lerMaisLabel.setVisible(false);
        lerMaisLabel.setManaged(false);
    }

    private void atualizarImagemExibida() {
        if (imageUrls.isEmpty() || imagemAtualIndex < 0 || imagemAtualIndex >= imageUrls.size()) {
            return;
        }
        String urlDaImagem = imageUrls.get(imagemAtualIndex);
        imagemView.setImage(new Image(urlDaImagem, true));
        btnAnterior.setVisible(imagemAtualIndex > 0);
        btnProxima.setVisible(imagemAtualIndex < imageUrls.size() - 1);
    }

    @FXML
    private void mostrarImagemAnterior() {
        if (imagemAtualIndex > 0) {
            imagemAtualIndex--;
            atualizarImagemExibida();
        }
    }

    @FXML
    private void mostrarProximaImagem() {
        if (imagemAtualIndex < imageUrls.size() - 1) {
            imagemAtualIndex++;
            atualizarImagemExibida();
        }
    }
    // isso fecha o app se um dia precissar
    @FXML
    private void fechar() {
        Stage stage = (Stage) nomeLabel.getScene().getWindow();
        stage.close();
    }
}
