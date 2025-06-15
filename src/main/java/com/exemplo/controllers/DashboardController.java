package com.exemplo.controllers;

import com.exemplo.models.Peca;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // --- Referências FXML Atualizadas ---
    @FXML private BorderPane borderPane;
    @FXML private StackPane contentArea;
    @FXML private Label titleLabel;
    @FXML private VBox sidebar;
    @FXML private Label logoLabel;
    @FXML private Button btnInicio;
    @FXML private Button btnVendas; // Corresponde ao botão "Loja" no FXML
    @FXML private Button btnConsultarEstoque;
    @FXML private Button btnPdv;
    @FXML private Button btnCadastro;
    @FXML private Button btnSair;

    // --- Constantes para a animação ---
    private static final double EXPANDED_WIDTH = 230.0;
    private static final double COLLAPSED_WIDTH = 60.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(250);
    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        handleDashboard(null);
    }

    @FXML
    void handleToggleSidebar(ActionEvent event) {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;

        btnInicio.setText(sidebarCollapsed ? "" : "Início");
        btnVendas.setText(sidebarCollapsed ? "" : "Loja");
        btnPdv.setText(sidebarCollapsed ? "" : "Ponto de Venda");
        btnCadastro.setText(sidebarCollapsed ? "" : "Cadastro de Produtos");
        btnConsultarEstoque.setText(sidebarCollapsed ? "" : "Consultar Estoque");
        btnSair.setText(sidebarCollapsed ? "" : "Sair");
        logoLabel.setVisible(!sidebarCollapsed);
        logoLabel.setManaged(!sidebarCollapsed);

        Timeline timeline = new Timeline(new KeyFrame(ANIMATION_DURATION, new KeyValue(sidebar.prefWidthProperty(), targetWidth)));
        timeline.play();
    }

    // --- MÉTODOS DE NAVEGAÇÃO ---

    @FXML void handleDashboard(ActionEvent event) {
        titleLabel.setText("Painel Principal");
        loadPage("BoasVindas.fxml");
    }

    /**
     * NOME CORRIGIDO: de handleLoja para handleVendas
     * Este método agora corresponde ao onAction no FXML para o botão "Loja".
     */
    @FXML void handleVendas(ActionEvent event) {
        titleLabel.setText("Loja");
        loadPage("Vendas.fxml");
    }

    @FXML void handlePdv(ActionEvent event) {
        titleLabel.setText("Ponto de Venda");
        loadPage("PdvView.fxml");
    }

    @FXML void handleConsultarEstoque(ActionEvent event) {
        titleLabel.setText("Consulta de Estoque");
        loadPage("VisualizarProdutos.fxml");
    }

    @FXML void handlecadastro(ActionEvent event) {
        titleLabel.setText("Cadastro de Produto");
        loadPage("Produto.fxml");
    }

    @FXML
    void handleSair(ActionEvent event) {
        try {
            Stage currentStage = (Stage) borderPane.getScene().getWindow();
            currentStage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Login");
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent page = loader.load();
            Object controller = loader.getController();

            if (controller instanceof VisualizarProdutosController) {
                ((VisualizarProdutosController) controller).setDashboardController(this);
            }

            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a página: " + fxml);
            e.printStackTrace();
        }
    }

    public void carregarDetalhesPeca(Peca peca) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/detalhe_peca.fxml"));
            Parent page = loader.load();
            DetalhePecaController controller = loader.getController();
            controller.setPeca(peca);
            titleLabel.setText("Detalhes da Peça");
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("Erro ao carregar a página de detalhes da peça.");
            e.printStackTrace();
        }
    }
}
