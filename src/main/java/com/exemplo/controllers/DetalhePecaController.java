package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.CarrinhoService; // IMPORTADO
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

    @FXML private Label nomeLabel;
    @FXML private Label tipoLabel;
    @FXML private Label poderLabel;
    @FXML private ImageView imagemView;
    @FXML private Label precoLabel;
    @FXML private Label quantidadeLabel;
    @FXML private TextArea descricaoTextArea;
    @FXML private Button btnComprar; // Este é o botão que vamos usar
    @FXML private Button btnAnterior;
    @FXML private Button btnProxima;
    @FXML private Label lerMaisLabel;

    private List<String> imageUrls = new ArrayList<>();
    private int imagemAtualIndex = 0;

    // --- Explicação das Modificações ---

    // 1. Criamos uma variável para guardar a peça que está sendo exibida nesta tela.
    private Peca pecaAtual;

    // 2. Pegamos a instância do nosso carrinho central.
    private final CarrinhoService carrinhoService = CarrinhoService.getInstance();

    public void setPeca(Peca peca) {
        // 3. Além de preencher os campos da tela, agora também guardamos a peça
        //    na nossa variável 'pecaAtual' para uso posterior.
        this.pecaAtual = peca;

        nomeLabel.setText(peca.getNome());
        tipoLabel.setText("Tipo: " + peca.getTipoPeca());
        poderLabel.setText("Poder: " + peca.getPoderComputacional());
        precoLabel.setText(String.format("R$ %.2f", peca.getPreco()));
        quantidadeLabel.setText("Qtd: " + peca.getQuantidade());
        descricaoTextArea.setText(peca.getDescricao());

        this.imageUrls = new ArrayList<>(peca.getImageUrls());
        imagemAtualIndex = 0;
        if (!imageUrls.isEmpty()) {
            atualizarImagemExibida();
        } else {
            imagemView.setImage(new Image("https://placehold.co/300x200/e0e0e0/757575?text=Sem+Imagem"));
            btnAnterior.setVisible(false);
            btnProxima.setVisible(false);
        }
        configurarDescricaoExpansivel();
    }

    /**
     * 4. Este é o novo método que será executado quando o botão 'Comprar' for clicado.
     */
    @FXML
    private void handleAdicionarAoCarrinho() {
        if (pecaAtual != null) {
            // Chama o serviço central para adicionar a peça atual ao carrinho.
            carrinhoService.adicionarItem(pecaAtual);

            // Exibe uma mensagem de sucesso para o usuário.
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Produto Adicionado");
            alert.setHeaderText(null);
            alert.setContentText("'" + pecaAtual.getNome() + "' foi adicionado ao seu carrinho!");
            alert.showAndWait();
        }
    }

    // --- (O resto dos métodos da classe não muda) ---
    private void configurarDescricaoExpansivel() { /* ...código existente... */ }
    @FXML private void expandirDescricao() { /* ...código existente... */ }
    private void atualizarImagemExibida() { /* ...código existente... */ }
    @FXML private void mostrarImagemAnterior() { /* ...código existente... */ }
    @FXML private void mostrarProximaImagem() { /* ...código existente... */ }
    @FXML private void fechar() { /* ...código existente... */ }
}