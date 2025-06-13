package com.exemplo.controllers;

import com.exemplo.services.ServiceCadastroLogin;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class EsqueceuSenhaController {

    @FXML private VBox rootPane;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;
    @FXML private Button sendButton;
    @FXML private Button continueButton;

    private final ServiceCadastroLogin supabaseService = new ServiceCadastroLogin();

    @FXML
    private void handleEnviarLink(ActionEvent event) {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            setStatus("Por favor, digite um endereço de e-mail.", true);
            return;
        }

        // ======================= MUDANÇA AQUI (1/2) =======================
        // Definimos a URL de redirecionamento para sua página no GitHub Pages.
        final String redirectUrl = "https://jonathanfink0612.github.io/redefinir_senha/";
        // ====================================================================

        setStatus("Enviando link...", false);
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                // ======================= MUDANÇA AQUI (2/2) =======================
                // Passamos a URL junto com o e-mail para o serviço do Supabase.
                boolean sucesso = supabaseService.enviarEmailRedefinicaoSenha(email, redirectUrl);
                // ====================================================================

                Platform.runLater(() -> {
                    if (sucesso) {
                        setStatus("Se uma conta com este e-mail existir, um link de redefinição foi enviado.", false);
                        continueButton.setVisible(true);
                        continueButton.setManaged(true);
                    } else {
                        setStatus("Ocorreu um erro. Tente novamente mais tarde.", true);
                    }
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    setStatus("Erro de comunicação. Verifique sua conexão.", true);
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void voltarParaLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStatus(String mensagem, boolean isError) {
        statusLabel.setText(mensagem);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
    }
}