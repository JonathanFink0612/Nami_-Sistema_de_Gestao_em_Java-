package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.CarrinhoService; // Adicionada a importação do serviço de carrinho
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
    @FXML private Button btnComprar; // Este botão agora terá a ação de adicionar ao carrinho
    @FXML private Button btnAnterior;
    @FXML private Button btnProxima;
    @FXML private Label lerMaisLabel;

    // --- Variáveis de controle ---
    private List<String> imageUrls = new ArrayList<>();
    private int imagemAtualIndex = 0;
    private Peca pecaAtual; // Variável para guardar a peça sendo exibida
    private final CarrinhoService carrinhoService = CarrinhoService.getInstance(); // Instância do serviço de carrinho

    /**
     * Configura a tela com os detalhes da peça recebida.
     * Este método agora também armazena a peça para uso posterior.
     * @param peca O objeto Peca a ser exibido.
     */
    public void setPeca(Peca peca) {
        this.pecaAtual = peca; // Armazena a peça atual

        // Preenche os dados básicos da peça
        nomeLabel.setText(peca.getNome());
        tipoLabel.setText("Tipo: " + peca.getTipoPeca());
        poderLabel.setText("Poder: " + peca.getPoderComputacional());
        precoLabel.setText(String.format("R$ %.2f", peca.getPreco()));
        quantidadeLabel.setText("Qtd: " + peca.getQuantidade());
        descricaoTextArea.setText(peca.getDescricao());

        // Lógica do carrossel de imagens
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

    /**
     * MÉTODO MESCLADO: Chamado pelo botão "Comprar" ou "Adicionar ao Carrinho".
     * Usa o CarrinhoService para adicionar a peça atual ao carrinho de compras.
     */
    @FXML
    private void handleAdicionarAoCarrinho() {
        if (pecaAtual != null) {
            carrinhoService.adicionarItem(pecaAtual);

            // Exibe uma mensagem de sucesso para o usuário.
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Produto Adicionado");
            alert.setHeaderText(null);
            alert.setContentText("'" + pecaAtual.getNome() + "' foi adicionado ao seu carrinho!");
            alert.showAndWait();
        } else {
            // Mensagem de erro caso algo inesperado aconteça
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Não foi possível adicionar o produto. Nenhuma peça selecionada.");
            alert.showAndWait();
        }
    }

    // --- Lógica da Interface (do código do GitHub) ---

    private void configurarDescricaoExpansivel() {
        lerMaisLabel.setVisible(false);
        lerMaisLabel.setManaged(false);
        Platform.runLater(() -> {
            ScrollBar verticalScrollBar = (ScrollBar) descricaoTextArea.lookup(".scroll-bar:vertical");
            if (verticalScrollBar != null && verticalScrollBar.isVisible()) {
                lerMaisLabel.setVisible(true);
                lerMaisLabel.setManaged(true);
            }
        });
    }

    @FXML
    private void expandirDescricao() {
        descricaoTextArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
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

    @FXML
    private void fechar() {
        Stage stage = (Stage) nomeLabel.getScene().getWindow();
        stage.close();
    }
}
