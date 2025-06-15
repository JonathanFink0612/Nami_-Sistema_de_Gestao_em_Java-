package com.exemplo.controllers;

import com.exemplo.services.CallbackServer;
import com.exemplo.services.SessionManager;
import com.exemplo.services.SupabaseService;
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
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class LoginController implements Initializable {

    @FXML private GridPane rootGridPane;
    @FXML private StackPane imagePane;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label statusLabel;

    private final SupabaseService supabaseService = new SupabaseService();
    private final double BREAKPOINT = 650.0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (rootGridPane != null) {
            rootGridPane.widthProperty().addListener((obs, oldVal, newVal) -> updateLayout(newVal.doubleValue()));
            Platform.runLater(() -> updateLayout(rootGridPane.getWidth()));
        }
    }

    private void updateLayout(double width) {
        if (imagePane == null || rootGridPane == null) return;

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
        String email = emailField.getText();
        String senha = senhaField.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            setStatus("Preencha todos os campos.");
            return;
        }

        setStatus("Autenticando...");
        new Thread(() -> {
            try {
                String accessToken = supabaseService.autenticarUsuario(email, senha);
                if (accessToken != null) {
                    JSONObject userData = supabaseService.obterUsuarioLogado(accessToken);
                    String userId = userData.getString("id");
                    String userEmail = userData.getString("email");
                    SessionManager.getInstance().startSession(userId, userEmail);
                    Platform.runLater(() -> redirecionarParaTela("Dashboard.fxml", "Painel Principal"));
                } else {
                    Platform.runLater(() -> setStatus("E-mail ou senha incorretos."));
                }
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

                CompletableFuture<String> sessionFuture = supabaseService.exchangeCodeForSession(authCode);
                sessionFuture.thenAccept(accessToken -> {
                    if (accessToken != null) {
                        try {
                            JSONObject userData = supabaseService.obterUsuarioLogado(accessToken);
                            String userId = userData.getString("id");
                            String userEmail = userData.getString("email");
                            SessionManager.getInstance().startSession(userId, userEmail);
                            Platform.runLater(() -> redirecionarParaTela("Dashboard.fxml", "Painel Principal"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> setStatus("Erro ao obter dados do usuário do Google."));
                        }
                    } else {
                        Platform.runLater(() -> setStatus("Falha ao obter sessão do Supabase com Google."));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> setStatus("Falha no login: " + e.getMessage()));
            }
        }).start();
    }

    @FXML public void paginaCadastro(ActionEvent event) {
        redirecionarParaTela("cadastro.fxml", "Cadastro de Usuário");
    }

    @FXML private void handleEsqueceuSenha(ActionEvent event) {
        redirecionarParaTela("EsqueceuSenha.fxml", "Redefinir Senha");
    }

    private void redirecionarParaTela(String fxmlFile, String newTitle) {
        try {
            String path = "/view/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) rootGridPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(newTitle);
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
}
