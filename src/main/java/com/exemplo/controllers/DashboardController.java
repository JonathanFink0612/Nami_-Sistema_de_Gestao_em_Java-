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
    @FXML private Button btnGestaoAcessos;

    // --- Constantes para a animação ---
    private static final double EXPANDED_WIDTH = 230.0;
    private static final double COLLAPSED_WIDTH = 60.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(250);
    private boolean sidebarCollapsed = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(this::aplicarPermissoes);
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

    @FXML void handleacesso(ActionEvent event) {
        titleLabel.setText("Cargos");
        loadPage("GestaoAcessos.fxml");
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


    private void aplicarPermissoes() {
        String role = SessionManager.getInstance().getRole();

        // Fallback de segurança: se o cargo for nulo ou desconhecido, será tratado pelo 'default'.
        if (role == null) {
            role = "DESCONHECIDO";
        }

        // 1. Oculta todos os botões para garantir um estado limpo.
        btnVendas.setVisible(false);
        btnVendas.setManaged(false);
        btnPdv.setVisible(false);
        btnPdv.setManaged(false);
        btnConsultarEstoque.setVisible(false);
        btnConsultarEstoque.setManaged(false);
        btnCadastro.setVisible(false);
        btnCadastro.setManaged(false);
        btnGestaoAcessos.setVisible(false);
        btnGestaoAcessos.setManaged(false);

        boolean acessoLiberado = true;

        // 2. Aplica as permissões com base no cargo.
        switch (role) {
            case "SUPERVISOR":
                // Acesso total. Ativa seu botão exclusivo e herda o resto.
                btnGestaoAcessos.setVisible(true);
                btnGestaoAcessos.setManaged(true);
                // Fall-through intencional para herdar permissões do Nivel2

            case "NIVEL2":
                // Acesso de Nivel1 + Cadastro. Ativa seu botão e herda o resto.

                btnConsultarEstoque.setVisible(true);
                btnConsultarEstoque.setManaged(true);
                // Fall-through intencional para herdar permissões do Nivel1

            case "NIVEL1":
                // Acesso de Estagiário + Consulta de Estoque.
                btnCadastro.setVisible(true);
                btnCadastro.setManaged(true);
                // Fall-through intencional para herdar permissões de Estagiário

            case "ESTAGIARIO":
                // NOVO: Acesso limitado a Vendas e Ponto de Venda.
                btnVendas.setVisible(true);
                btnVendas.setManaged(true);
                btnPdv.setVisible(true);
                btnPdv.setManaged(true);
                break; // Termina aqui. Estagiário não tem mais permissões.

            default: // Trata cargos nulos ou não reconhecidos.
                // Acesso totalmente bloqueado.
                titleLabel.setText("Acesso Negado");
                contentArea.getChildren().setAll(new Label("Seu cargo não foi reconhecido. Contate um supervisor."));
                acessoLiberado = false;
                break;
        }

        // 3. Carrega a tela inicial se o usuário tiver qualquer acesso liberado.
        // A condição 'getChildren().isEmpty()' previne recarregar a tela desnecessariamente.
        if (acessoLiberado && contentArea.getChildren().get(0) instanceof Label) {
            handleDashboard(null);
        }
    }




}
