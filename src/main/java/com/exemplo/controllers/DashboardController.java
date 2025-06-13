package com.exemplo.controllers;

import com.exemplo.models.Peca; // <-- ALTERAÇÃO 1: Import adicionado
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

    // --- Constantes para a animação ---
    private static final double EXPANDED_WIDTH = 220.0;
    private static final double COLLAPSED_WIDTH = 60.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(250);

    // --- Referências FXML ---
    @FXML
    private BorderPane borderPane;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label titleLabel;
    @FXML
    private VBox sidebar;
    @FXML
    private Label logoLabel;
    @FXML
    private Button btnInicio;
    @FXML
    private Button btnVendas;
    @FXML
    private Button btnSair;

    @FXML
    private Button btnCadastro;

    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        handleDashboard(null);
    }

    @FXML
    void handleToggleSidebar(ActionEvent event) {
        sidebarCollapsed = !sidebarCollapsed; // Inverte o estado
        animateSidebar();
    }

    private void animateSidebar() {
        double targetWidth = sidebarCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(sidebar.prefWidthProperty(), targetWidth);
        KeyFrame keyFrame = new KeyFrame(ANIMATION_DURATION, keyValue);
        timeline.getKeyFrames().add(keyFrame);
        toggleButtonTextVisibility();
        timeline.play();
    }

    private void toggleButtonTextVisibility() {
        btnInicio.setText(sidebarCollapsed ? "" : "Início");
        btnVendas.setText(sidebarCollapsed ? "" : "Vendas");
        btnCadastro.setText(sidebarCollapsed ? "" : "Cadastro");
        btnSair.setText(sidebarCollapsed ? "" : "Sair");
        logoLabel.setVisible(!sidebarCollapsed);
        logoLabel.setManaged(!sidebarCollapsed);
    }

    // --- Métodos de Navegação ---

    @FXML
    void handleDashboard(ActionEvent event) {
        titleLabel.setText("Início");
        loadPage("BoasVindas.fxml");
    }

    @FXML
    void handleVendas(ActionEvent event) {
        titleLabel.setText("Vendas");
        loadPage("VisualizarProdutos.fxml");
    }

    @FXML
    void handlecadastro(ActionEvent event) {
        titleLabel.setText("Cadastro Produto");
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

    // --- MÉTODOS DE LÓGICA INTERNA ---

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
        } catch (Exception e) {
            System.err.println("Um erro inesperado ocorreu ao carregar a página: " + fxml);
            e.printStackTrace();
        }
    }

    /**
     * Carrega a página de detalhes de um item específico.
     * Este método deve ser chamado pelo controlador "filho" (ex: VisualizarProdutosController).
     *
     * @param peca O objeto Peca com os dados a serem exibidos.
     */
    // --- ALTERAÇÃO 2: Tipo do parâmetro mudado de Object para Peca ---
    public void carregarDetalhesPeca(Peca peca) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/detalhe_peca.fxml"));
            Parent page = loader.load();

            DetalhePecaController controller = loader.getController();
            // Agora a chamada é válida, pois 'peca' é do tipo Peca.
            controller.setPeca(peca);

            titleLabel.setText("Detalhes da Peça");
            contentArea.getChildren().setAll(page);

        } catch (IOException e) {
            System.err.println("Erro ao carregar a página de detalhes da peça.");
            e.printStackTrace();
        }
    }
}
