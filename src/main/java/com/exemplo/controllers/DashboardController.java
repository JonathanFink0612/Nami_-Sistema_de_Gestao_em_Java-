package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    // --- Componentes FXML ---
    @FXML private BorderPane borderPane;
    @FXML private StackPane contentArea;
    @FXML private Label titleLabel;
    @FXML private VBox sidebar;
    @FXML private Label logoLabel;
    @FXML private Button btnInicio;
    @FXML private Button btnVendas;
    @FXML private Button btnConsultarEstoque;
    @FXML private Button btnPdv;
    @FXML private Button btnCadastro;
    @FXML private Button btnGestaoAcessos;
    @FXML private Button btnSair;

    // --- Constantes para animação ---
    private static final double EXPANDED_WIDTH = 230.0;
    private static final double COLLAPSED_WIDTH = 60.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(250);
    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(this::aplicarPermissoes);
        handleDashboard(null);
    }

    private void aplicarPermissoes() {
        String role = SessionManager.getInstance().getRole();

        if (role == null) {
            role = "ESTAGIARIO"; // Segurança: assume o cargo mais restritivo.
        }

        // 1. Oculta todos os botões funcionais por defeito.
        btnVendas.setVisible(false); btnVendas.setManaged(false);
        btnPdv.setVisible(false); btnPdv.setManaged(false);
        btnConsultarEstoque.setVisible(false); btnConsultarEstoque.setManaged(false);
        btnCadastro.setVisible(false); btnCadastro.setManaged(false);
        btnGestaoAcessos.setVisible(false); btnGestaoAcessos.setManaged(false);

        boolean acessoBloqueado = false;

        // 2. Aplica as permissões com base no cargo (hierarquia).
        switch (role) {
            case "SUPERVISOR":
                btnGestaoAcessos.setVisible(true);
                btnGestaoAcessos.setManaged(true);
            case "NIVEL2":
                btnConsultarEstoque.setVisible(true);
                btnConsultarEstoque.setManaged(true);
            case "NIVEL1":
                btnCadastro.setVisible(true);
                btnCadastro.setManaged(true);
            case "ESTAGIARIO":
                btnVendas.setVisible(true);
                btnVendas.setManaged(true);
                btnPdv.setVisible(true);
                btnPdv.setManaged(true);
                break;
            default: // Trata cargos nulos ou não reconhecidos.
                acessoBloqueado = true;
                titleLabel.setText("Acesso Pendente");
                contentArea.getChildren().setAll(new Label("O seu acesso precisa de ser libertado por um supervisor."));
                break;
        }

        if (!acessoBloqueado && contentArea.getChildren().get(0) instanceof Label) {
            handleDashboard(null);
        }
    }

    @FXML
    void handleToggleSidebar(ActionEvent event) {
        sidebarCollapsed = !sidebarCollapsed;
        double targetWidth = sidebarCollapsed ? COLLAPSED_WIDTH : EXPANDED_WIDTH;

        btnInicio.setText(sidebarCollapsed ? "" : "Início");
        btnVendas.setText(sidebarCollapsed ? "" : "Loja");
        btnConsultarEstoque.setText(sidebarCollapsed ? "" : "Consultar Estoque");
        btnPdv.setText(sidebarCollapsed ? "" : "Ponto de Venda");
        btnCadastro.setText(sidebarCollapsed ? "" : "Cadastro de Produtos");
        if (btnGestaoAcessos != null) btnGestaoAcessos.setText(sidebarCollapsed ? "" : "Gestão de Acessos");
        btnSair.setText(sidebarCollapsed ? "" : "Sair");
        logoLabel.setVisible(!sidebarCollapsed);
        logoLabel.setManaged(!sidebarCollapsed);

        Timeline timeline = new Timeline(new KeyFrame(ANIMATION_DURATION, new KeyValue(sidebar.prefWidthProperty(), targetWidth)));
        timeline.play();
    }

    // --- MÉTODOS DE NAVEGAÇÃO ---

    @FXML void handleDashboard(ActionEvent event) {
        titleLabel.setText("Painel Principal");
        loadGenericPage("BoasVindas.fxml");
    }

    @FXML void handleVendas(ActionEvent event) {
        titleLabel.setText("Loja");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Vendas.fxml")); // Ajuste o caminho se necessário
            Parent page = loader.load();
            VendasController vendasController = loader.getController();
            System.out.println("[LOG] Injetando DashboardController em VendasController...");
            vendasController.setDashboardController(this);
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("[ERRO CRÍTICO] Falha ao carregar a página de Vendas.fxml: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Não foi possível carregar a loja. Verifique o console para mais detalhes.").show();
        }
    }

    @FXML void handleConsultarEstoque(ActionEvent event) {
        titleLabel.setText("Consulta de Estoque");
        loadGenericPage("VisualizarProdutos.fxml");
    }

    @FXML void handlePdv(ActionEvent event) {
        titleLabel.setText("Ponto de Venda");
        loadGenericPage("PdvView.fxml");
    }

    @FXML void handlecadastro(ActionEvent event) {
        titleLabel.setText("Cadastro de Produto");
        loadGenericPage("Produto.fxml");
    }

    /**
     * CORREÇÃO: Método renomeado de 'handleGestaoAcessos' para 'handleacesso'
     * para corresponder ao onAction definido no arquivo Dashboard.fxml.
     */
    @FXML
    void handleacesso(ActionEvent event) {
        titleLabel.setText("Gestão de Acessos");
        loadGenericPage("GestaoAcessos.fxml");
    }

    @FXML
    void handleSair(ActionEvent event) {
        try {
            SessionManager.getInstance().endSession();
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

    private void loadGenericPage(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("[ERRO] Falha ao carregar a página genérica: " + fxml);
            e.printStackTrace();
        }
    }

    public void carregarDetalhesPeca(Peca peca) {
        System.out.println("[LOG] DashboardController recebeu o chamado para carregar detalhes da peça: " + peca.getNome());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/detalhe_peca.fxml")); // Ajuste o caminho se necessário
            Parent page = loader.load();
            DetalhePecaController controller = loader.getController();
            controller.setPeca(peca);
            titleLabel.setText("Detalhes da Peça");
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            System.err.println("[ERRO CRÍTICO] Falha ao carregar a página de detalhes da peça.");
            e.printStackTrace();
        }
    }
}
