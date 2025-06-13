package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.PecaSupabaseClient;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProdutoController {

    @FXML private TextField nomeField;
    @FXML private TextField precoField;
    @FXML private TextArea descricaoField;
    @FXML private TextField quantidadeField;
    @FXML private TextField codigoDeBarrasField;
    @FXML private Spinner<Integer> poderComputacionalSpinner;
    @FXML private ComboBox<String> tipoPecaCombo;
    @FXML private Button salvarButton;
    @FXML private VBox formContainer;
    @FXML private Button adicionarImagemButton; // NOVO
    @FXML private FlowPane imagensFlowPane;   // NOVO

    private final Map<String, Control> camposDinamicos = new HashMap<>();
    private final PecaSupabaseClient supabaseService = new PecaSupabaseClient();
    private final List<File> imagensSelecionadas = new ArrayList<>(); // NOVO

    @FXML
    public void initialize() {
        configurarFormulario();
        configurarListenerTipoPeca();
    }

    /**
     * NOVO: Abre um seletor de arquivos para o usuário escolher uma ou mais imagens.
     */
    @FXML
    void handleAdicionarImagens() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione as Imagens do Produto");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.png", "*.gif", "*.jpeg")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(adicionarImagemButton.getScene().getWindow());

        if (files != null) {
            for (File file : files) {
                // Evita adicionar imagens duplicadas
                if (imagensSelecionadas.stream().noneMatch(f -> f.getAbsolutePath().equals(file.getAbsolutePath()))) {
                    imagensSelecionadas.add(file);
                    adicionarThumbnail(file);
                }
            }
        }
    }

    /**
     * NOVO: Adiciona uma miniatura da imagem selecionada na interface.
     * @param file O arquivo de imagem a ser exibido.
     */
    private void adicionarThumbnail(File file) {
        ImageView imageView = new ImageView(new Image(file.toURI().toString()));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        Button removeButton = new Button("X");
        removeButton.setStyle("-fx-background-color: rgba(255, 0, 0, 0.7); -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 50;");

        removeButton.setOnAction(e -> {
            imagensFlowPane.getChildren().remove(imageView.getParent());
            imagensSelecionadas.remove(file);
        });

        StackPane stackPane = new StackPane(imageView, removeButton);
        StackPane.setAlignment(removeButton, Pos.TOP_RIGHT);
        imagensFlowPane.getChildren().add(stackPane);
    }

    @FXML
    private void salvarProduto() {
        if (nomeField.getText().isEmpty()) {
            exibirAlerta("Erro de Validação", "O nome do produto é obrigatório.", Alert.AlertType.WARNING);
            return;
        }

        salvarButton.setDisable(true);
        salvarButton.setText("Salvando Produto...");

        Peca novaPeca = new Peca(
                nomeField.getText(), descricaoField.getText(),
                parseDouble(precoField.getText()), parseInt(quantidadeField.getText()),
                tipoPecaCombo.getValue(), codigoDeBarrasField.getText(),
                poderComputacionalSpinner.getValue()
        );

        for (Map.Entry<String, Control> entry : camposDinamicos.entrySet()) {
            String valor = ((TextField) entry.getValue()).getText();
            if (valor != null && !valor.isEmpty()) {
                novaPeca.setCampoAdicional(entry.getKey(), valor);
            }
        }

        // --- LÓGICA DE SALVAMENTO EM DUAS ETAPAS ---

        // Etapa 1: Salvar os dados da peça principal
        Task<Integer> salvarPecaTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                // Modificado para salvar a peça e os campos especializados, retornando o ID
                return supabaseService.salvarPecaCompleta(novaPeca);
            }
        };

        salvarPecaTask.setOnSucceeded(event -> {
            int pecaId = salvarPecaTask.getValue();
            // Etapa 2: Se a peça foi salva, iniciar o upload das imagens
            if (!imagensSelecionadas.isEmpty()) {
                salvarButton.setText("Enviando Imagens...");
                uploadImagens(pecaId, new ArrayList<>(imagensSelecionadas)); // Envia uma cópia da lista
            } else {
                // Se não há imagens, o processo termina aqui
                limparFormulario();
                reabilitarBotaoSalvar();
                exibirAlerta("Sucesso", "Produto salvo com sucesso! (Sem imagens)", Alert.AlertType.INFORMATION);
            }
        });

        salvarPecaTask.setOnFailed(event -> tratarFalha(salvarPecaTask.getException()));
        new Thread(salvarPecaTask).start();
    }

    /**
     * NOVO: Inicia uma nova tarefa para fazer o upload das imagens em background.
     * @param pecaId O ID do produto ao qual as imagens pertencem.
     * @param imagensParaUpload A lista de arquivos a serem enviados.
     */
    private void uploadImagens(int pecaId, List<File> imagensParaUpload) {
        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (File imagem : imagensParaUpload) {
                    supabaseService.uploadImagemESalvarUrl(imagem, pecaId);
                }
                return null;
            }
        };

        uploadTask.setOnSucceeded(event -> {
            limparFormulario();
            reabilitarBotaoSalvar();
            exibirAlerta("Sucesso", "Produto e imagens salvos com sucesso!", Alert.AlertType.INFORMATION);
        });

        uploadTask.setOnFailed(event -> tratarFalha(uploadTask.getException()));
        new Thread(uploadTask).start();
    }

    private void reabilitarBotaoSalvar() {
        salvarButton.setDisable(false);
        salvarButton.setText("Salvar Produto");
    }

    private void tratarFalha(Throwable e) {
        exibirAlerta("Erro de Conexão", "Falha na operação: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
        reabilitarBotaoSalvar();
    }

    private void limparFormulario() {
        nomeField.clear(); descricaoField.clear(); precoField.clear();
        quantidadeField.clear(); codigoDeBarrasField.clear();
        poderComputacionalSpinner.getValueFactory().setValue(5);
        tipoPecaCombo.getSelectionModel().selectFirst();
        imagensSelecionadas.clear();
        imagensFlowPane.getChildren().clear();
        formContainer.getChildren().clear();
    }

    // --- MÉTODOS DE CONFIGURAÇÃO E CRIAÇÃO DE CAMPOS DINÂMICOS (sem alterações) ---
    private void configurarFormulario() {
        tipoPecaCombo.getItems().addAll("Peça Genérica", "Fonte de Alimentação", "Placa Mãe", "Processador", "RAM", "Placa de Vídeo");
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 5);
        poderComputacionalSpinner.setValueFactory(valueFactory);
    }
    private void configurarListenerTipoPeca() {
        tipoPecaCombo.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> atualizarCamposDinamicos(nv));
    }
    private void atualizarCamposDinamicos(String tipoPeca) {
        formContainer.getChildren().clear(); camposDinamicos.clear();
        if (tipoPeca == null) return;
        Node campos = null;
        switch (tipoPeca) {
            case "Fonte de Alimentação": campos = criarCamposFonte(); break;
            case "Placa Mãe": campos = criarCamposPlacaMae(); break;
            case "Processador": campos = criarCamposProcessador(); break;
            case "RAM": campos = criarCamposRam(); break;
            case "Placa de Vídeo": campos = criarCamposPlacaVideo(); break;
        }
        if (campos != null) formContainer.getChildren().add(campos);
    }
    private Node criarCamposFonte() { GridPane g = criarGridDinamico(); adicionarCampo(g, 0, "Capacidade (W):", "capacidade_w"); adicionarCampo(g, 1, "Selo de Eficiência:", "selo_eficiencia"); return g; }
    private Node criarCamposPlacaMae() { GridPane g = criarGridDinamico(); adicionarCampo(g, 0, "Socket:", "socket_placa_mae"); adicionarCampo(g, 1, "Tipo RAM Suportado:", "tipo_ram_suportado"); adicionarCampo(g, 2, "Versão PCI Principal:", "versao_pci_principal"); return g; }
    private Node criarCamposProcessador() { GridPane g = criarGridDinamico(); adicionarCampo(g, 0, "Socket:", "socket_processador"); adicionarCampo(g, 1, "Gasto Energético (W):", "gasto_energetico_w"); return g; }
    private Node criarCamposRam() { GridPane g = criarGridDinamico(); adicionarCampo(g, 0, "Tipo RAM:", "tipo_ram"); adicionarCampo(g, 1, "Frequência (MHz):", "frequencia_ram_mhz"); return g; }
    private Node criarCamposPlacaVideo() { GridPane g = criarGridDinamico(); adicionarCampo(g, 0, "Interface PCI:", "interface_pci_necessaria"); adicionarCampo(g, 1, "Gasto Energético (W):", "gasto_energetico_w"); return g; }
    private void adicionarCampo(GridPane g, int l, String t, String c) { Label label = new Label(t); TextField tf = new TextField(); int cl = (l%2==0)?0:2; int cf = (l%2==0)?1:3; int rg = l/2; g.add(label,cl,rg); g.add(tf,cf,rg); camposDinamicos.put(c,tf); }
    private GridPane criarGridDinamico() { GridPane g = new GridPane(); g.setHgap(15.0); g.setVgap(10.0); return g; }
    private void exibirAlerta(String t, String m, Alert.AlertType a) { Alert alert = new Alert(a); alert.setTitle(t); alert.setHeaderText(null); alert.setContentText(m); alert.showAndWait(); }
    private double parseDouble(String v) { try { return Double.parseDouble(v.replace(",",".")); } catch(Exception e){return 0.0;} }
    private int parseInt(String v) { try { return Integer.parseInt(v); } catch(Exception e){return 0;} }
}