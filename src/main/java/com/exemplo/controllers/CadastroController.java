package com.exemplo.controllers;

import com.exemplo.models.Senha;
import com.exemplo.models.Email;
import com.exemplo.services.ServiceCadastroLogin;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CadastroController implements Initializable {

    // IDs dos painéis que vamos controlar
    @FXML
    private GridPane rootGridPane;
    @FXML
    private StackPane imagePane;

    // IDs dos campos do formulário
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField senhaField;
    @FXML
    private Label statusLabel;

    private final ServiceCadastroLogin model;
    private final double BREAKPOINT = 650.0; // Ponto em que a imagem some

    public CadastroController() {
        this.model = new ServiceCadastroLogin();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Adiciona um "ouvinte" que reage a mudanças na largura da janela
        rootGridPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateLayout(newVal.doubleValue());
        });

        // Garante que o layout comece correto na primeira renderização
        Platform.runLater(() -> updateLayout(rootGridPane.getWidth()));
    }

    /**
     * Contém a lógica para o layout responsivo, idêntica à do LoginController.
     */
    private void updateLayout(double width) {
        boolean isSmallScreen = width < BREAKPOINT;

        imagePane.setVisible(!isSmallScreen);
        imagePane.setManaged(!isSmallScreen);

        // Pegamos as definições das duas colunas
        ColumnConstraints imageColumn = rootGridPane.getColumnConstraints().get(0);
        ColumnConstraints formColumn = rootGridPane.getColumnConstraints().get(1);

        if (isSmallScreen) {
            // TELA PEQUENA: Coluna da imagem = 0%, Coluna do form = 100%
            imageColumn.setPercentWidth(0);
            formColumn.setPercentWidth(100);
        } else {
            // TELA GRANDE: Coluna da imagem = 40%, Coluna do form = 60%
            imageColumn.setPercentWidth(40);
            formColumn.setPercentWidth(60);
        }
    }
    @FXML
    private void handleCadastro(ActionEvent event) {
        Email email = new Email(emailField.getText());
        Senha senha = new Senha(senhaField.getText());

        if (senha.estaVazio() || email.estaVazio()) {
            setStatus("Preencha todos os campos.");
            return;
        }

        if (email.temErro()) {
            setStatus(email.getErro());
            return;
        }

        setStatus("Cadastrando...");

        // Inicia o processo de cadastro em uma nova Thread para não travar a interface
        new Thread(() -> {
            try {
                boolean sucesso = model.cadastrarUsuario(email.getValor(), senha.getValor());
                if (sucesso) {
                    // Se o cadastro deu certo, atualiza a UI e redireciona
                    Platform.runLater(() -> {
                        setStatus("Cadastro realizado com sucesso!");
                        limparCampos();
                        // Chama o método de redirecionamento após um pequeno delay
                        redirecionarComDelay("login.fxml", "Login");
                    });
                } else {
                    // Se o Supabase retornou 'false'
                    atualizarStatusErro("Erro: E-mail ou senha inválidos.");
                }
            } catch (Exception e) {
                // Se ocorreu uma exceção de rede ou outra
                e.printStackTrace(); // Importante para ver o erro no console
                atualizarStatusErro(e.getMessage());
            }
        }).start();
    }

    /**
     * MÉTODO CENTRALIZADO E CORRIGIDO PARA TROCAR DE TELA.
     * Ele pega a janela (Stage) a partir de um componente qualquer da tela atual.
     *
     * @param fxmlFile O nome do arquivo FXML para carregar (ex: "login.fxml").
     * @param newTitle O novo título da janela.
     */
    /**
     * MÉTODO CENTRALIZADO E CORRIGIDO PARA TROCAR DE TELA.
     * Ele pega a janela (Stage) a partir de um componente qualquer da tela atual.
     *
     * @param fxmlFile O nome do arquivo FXML para carregar (ex: "login.fxml").
     * @param newTitle O novo título da janela.
     */
    private void redirecionarParaTela(String fxmlFile, String newTitle) {
        try {
            // CORREÇÃO IMPORTANTE: Adiciona "/view/" ao caminho.
            // O "/" no início busca a partir da raiz do 'classpath' (pasta 'resources').
            String path = "/view/" + fxmlFile;
            System.out.println("Tentando carregar FXML de: " + path);

            // CORREÇÃO PRINCIPAL: Verificamos a URL ANTES de criar o FXMLLoader.
            java.net.URL resourceUrl = getClass().getResource(path);

            // Se o caminho estiver errado, a URL será nula.
            if (resourceUrl == null) {
                throw new IOException("Não foi possível encontrar o arquivo FXML: " + path);
            }

            // Agora passamos a URL válida para o loader.
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Pega o Stage (janela) atual usando o statusLabel como referência
            Stage stage = (Stage) statusLabel.getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle(newTitle);
            stage.sizeToScene(); // Redimensiona a janela para o tamanho da nova cena
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            atualizarStatusErro("Erro crítico ao carregar a tela: " + fxmlFile);
        }
    }

    /**
     * Helper para redirecionar com um delay, para que o usuário veja a msg de sucesso.
     */
    private void redirecionarComDelay(String fxml, String title) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> redirecionarParaTela(fxml, title));
                    }
                },
                1000 // 1 segundo de delay
        );
    }

    /**
     * Manipulador para o botão/link que leva para a página de login.
     * Agora ele usa o método centralizado.
     */
    @FXML
    public void paginalogin(ActionEvent event) {
        // Para este, não precisamos de delay.
        redirecionarParaTela("login.fxml", "Login");
    }

    // --- Funções de Suporte (sem alterações, mas mantidas para clareza) ---

    private void setStatus(String mensagem) {
        // Garante que qualquer atualização no Label ocorra na Thread da UI
        Platform.runLater(() -> statusLabel.setText(mensagem));
    }

    private void atualizarStatusErro(String mensagem) {
        setStatus("Erro: " + mensagem);
    }

    private void limparCampos() {
        emailField.clear();
        senhaField.clear();
    }
}