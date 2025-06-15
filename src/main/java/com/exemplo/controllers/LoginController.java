package com.exemplo.controllers;

import com.exemplo.services.CallbackServer;
import com.exemplo.services.PecaSupabaseClient; // IMPORT ADICIONADO
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

    // --- Componentes FXML e Serviços ---
    @FXML private GridPane rootGridPane;
    @FXML private StackPane imagePane;
    @FXML private TextField emailField;
    @FXML private PasswordField senhaField;
    @FXML private Label statusLabel;

    private final SupabaseService supabaseService = new SupabaseService();
    private final PecaSupabaseClient pecaService = new PecaSupabaseClient(); // Adicionado para buscar o cargo
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
                    // Se o login foi bem-sucedido, busca os dados do utilizador e o seu cargo
                    iniciarSessaoCompleta(accessToken);
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
        setStatus("A iniciar login com Google...");
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
                            // Se o login foi bem-sucedido, busca os dados do utilizador e o seu cargo
                            iniciarSessaoCompleta(accessToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Platform.runLater(() -> setStatus("Erro ao obter dados do utilizador do Google."));
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

    /**
     * Método centralizado que busca os dados do utilizador e o seu cargo,
     * e então inicia a sessão globalmente.
     * @param accessToken O token de acesso obtido após a autenticação.
     */
    private void iniciarSessaoCompleta(String accessToken) throws Exception {
        // 1. Usa o token para buscar os dados básicos do utilizador (ID, email).
        JSONObject userData = supabaseService.obterUsuarioLogado(accessToken);
        String userId = userData.getString("id");
        String userEmail = userData.getString("email");

        // 2. Usa o ID do utilizador para buscar o seu cargo na tabela 'profiles'.
        String userRole = pecaService.fetchUserRole(userId);
        System.out.println("[DEBUG] Login bem-sucedido. Utilizador: " + userEmail + ", Cargo: " + userRole);

        // 3. Inicia a sessão global com todas as informações.
        SessionManager.getInstance().startSession(userId, userEmail, userRole);

        // 4. Redireciona para o painel principal.
        Platform.runLater(() -> redirecionarParaTela("Dashboard.fxml", "Painel Principal"));
    }

    // --- Outros Métodos de Navegação e UI ---

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
