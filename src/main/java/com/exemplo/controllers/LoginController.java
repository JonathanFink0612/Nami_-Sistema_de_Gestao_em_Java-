package com.exemplo.controllers;

import com.exemplo.services.SupabaseService;
import com.exemplo.services.CallbackServer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private GridPane rootGridPane;
    @FXML private StackPane imagePane; // Adicionada a anotação @FXML
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label statusLabel;

    private final SupabaseService supabaseService = new SupabaseService();
    private final double BREAKPOINT = 650.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Lógica para o layout responsivo
        rootGridPane.widthProperty().addListener((obs, oldVal, newVal) -> updateLayout(newVal.doubleValue()));
        Platform.runLater(() -> updateLayout(rootGridPane.getWidth()));
    }

    private void updateLayout(double width) {
        boolean isSmallScreen = width < BREAKPOINT;
        imagePane.setVisible(!isSmallScreen);
        imagePane.setManaged(!isSmallScreen);

        ColumnConstraints imageColumn = rootGridPane.getColumnConstraints().get(0);
        ColumnConstraints formColumn = rootGridPane.getColumnConstraints().get(1);

        if (isSmallScreen) {
            imageColumn.setPercentWidth(0);
            formColumn.setPercentWidth(100);
        } else {
            imageColumn.setPercentWidth(40);
            formColumn.setPercentWidth(60);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        // Lógica para login com e-mail/senha
        String email = emailField.getText();
        String senha = senhaField.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            setStatus("Preencha todos os campos.");
            return;
        }

        setStatus("Autenticando...");
        new Thread(() -> {
            try {
                boolean autenticado = supabaseService.autenticarUsuario(email, senha);
                Platform.runLater(() -> {
                    if (autenticado) {
                        redirecionarParaTela("Dashboard.fxml", "Painel Principal");
                    } else {
                        setStatus("E-mail ou senha incorretos.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Erro: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        setStatus("Iniciando login com Google...");
        new Thread(() -> {
            CallbackServer server = new CallbackServer();
            try {
                server.start();
                Desktop.getDesktop().browse(new URI(supabaseService.getGoogleSignInUrl()));
                String authCode = server.getAuthCodeFuture().join();

                supabaseService.exchangeCodeForSession(authCode)
                        .thenAccept(success -> {
                            if (success) {
                                Platform.runLater(() -> redirecionarParaTela("Dashboard.fxml", "Painel Principal"));
                            } else {
                                // Este 'else' pode ser acionado se exchangeCodeForSession retornar false no futuro.
                                Platform.runLater(() -> setStatus("Falha ao obter sessão do Supabase."));
                            }
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() -> setStatus("Erro: " + ex.getCause().getMessage()));
                            return null;
                        });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Falha no login: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void paginaCadastro(ActionEvent event) {
        redirecionarParaTela("cadastro.fxml", "Cadastro de Usuário");
    }

    private void redirecionarParaTela(String fxmlFile, String newTitle) {
        try {
            String path = "/view/" + fxmlFile;
            URL resourceUrl = getClass().getResource(path);
            if (resourceUrl == null) throw new IOException("Arquivo FXML não encontrado: " + path);
            Parent root = FXMLLoader.load(resourceUrl);
            Stage stage = (Stage) rootGridPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(newTitle);
            stage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
            setStatus("Erro crítico ao carregar a tela: " + fxmlFile);
        }
    }

    private void setStatus(String mensagem) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(mensagem));
        }
    }
    @FXML
    private void handleEsqueceuSenha(ActionEvent event) {
        // Este método simplesmente abre a nova tela de redefinição
        redirecionarParaTela("EsqueceuSenha.fxml", "Redefinir Senha");
    }

}
