package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.PecaSupabaseClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;

import java.util.List;

public class VisualizarProdutosController {

    @FXML private TextField searchField;
    @FXML private FlowPane productGrid;
    @FXML private VBox statusContainer;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    private final PecaSupabaseClient supabaseService = new PecaSupabaseClient();

    // --- INÍCIO DA MODIFICAÇÃO 1: Referência ao Controller Pai ---
    /**
     * Referência ao controlador principal (Dashboard) para permitir a comunicação
     * e navegação entre as telas.
     */
    private DashboardController dashboardController;

    /**
     * Método público para que o DashboardController possa "se apresentar"
     * a este controlador, estabelecendo a comunicação.
     * @param dashboardController A instância do controlador principal.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
    // --- FIM DA MODIFICAÇÃO 1 ---

    @FXML
    public void initialize() {
        System.out.println("[LOG] Inicializando VisualizarProdutosController...");
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            System.out.println("[LOG] Campo de busca alterado: \"" + newText + "\"");
            loadProducts(newText);
        });
        loadProducts(null);
    }

    @FXML
    private void handleSearch() {
        System.out.println("[LOG] Botão de busca pressionado.");
        loadProducts(searchField.getText());
    }

    private void loadProducts(String filter) {
        System.out.println("[LOG] Carregando produtos com filtro: " + filter);
        showLoading(true, "Carregando produtos...");
        productGrid.getChildren().clear();

        Task<List<Peca>> loadTask = new Task<>() {
            @Override
            protected List<Peca> call() throws Exception {
                return supabaseService.fetchAllProdutos(filter);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<Peca> produtos = loadTask.getValue();
            Platform.runLater(() -> {
                if (produtos.isEmpty()) {
                    System.out.println("[LOG] Nenhum produto encontrado.");
                    showLoading(true, "Nenhum produto encontrado.");
                } else {
                    System.out.println("[LOG] Produtos carregados: " + produtos.size());
                    produtos.forEach(this::createAndAddProductCard);
                    showLoading(false, "");
                }
            });
        });

        loadTask.setOnFailed(event -> {
            Throwable e = loadTask.getException();
            System.err.println("[ERRO] Erro ao carregar produtos: ");
            e.printStackTrace();
            Platform.runLater(() -> showLoading(true, "Erro ao carregar produtos."));
        });

        new Thread(loadTask).start();
    }

    private void createAndAddProductCard(Peca peca) {
        try {
            VBox card = new VBox(10);
            card.getStyleClass().add("product-card");
            card.setAlignment(Pos.CENTER);
            card.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));

            ImageView imageView = createImageViewForPeca(peca);

            Label nomeLabel = new Label(peca.getNome());
            nomeLabel.getStyleClass().add("card-title");

            Label tipoLabel = new Label("Tipo: " + (peca.getTipoPeca() != null ? peca.getTipoPeca() : "Genérico"));
            tipoLabel.getStyleClass().add("card-subtitle");

            Label poderLabel = new Label("Poder: " + peca.getPoderComputacional() + "/10");
            poderLabel.getStyleClass().add("card-power");

            card.getChildren().addAll(imageView, nomeLabel, tipoLabel, poderLabel);

            // --- INÍCIO DA MODIFICAÇÃO 2: Lógica de Clique Corrigida ---
            /**
             * Define a ação de clique no card. Em vez de criar uma nova janela,
             * ele agora chama o método público no DashboardController para carregar
             * a página de detalhes na área de conteúdo principal.
             */
            card.setOnMouseClicked(event -> {
                if (dashboardController != null) {
                    System.out.println("[LOG] Card clicado para a peça: " + peca.getNome());
                    // Chama o "pai" para fazer a navegação
                    dashboardController.carregarDetalhesPeca(peca);
                } else {
                    System.err.println("[ERRO] DashboardController não foi injetado. A navegação falhou.");
                }
            });
            // --- FIM DA MODIFICAÇÃO 2 ---

            productGrid.getChildren().add(card);

        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao criar card para produto: " + peca.getNome());
            e.printStackTrace();
        }
    }

    private ImageView createImageViewForPeca(Peca peca) {
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);

        Image placeholder = new Image("https://placehold.co/180x150/e0e0e0/757575?text=Carregando", true);
        imageView.setImage(placeholder);

        if (peca.getImageUrls() != null && !peca.getImageUrls().isEmpty()) {
            String url = peca.getImageUrls().get(0);
            Image realImage = new Image(url, true);

            realImage.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() >= 1.0) {
                    imageView.setImage(realImage);
                }
            });

            realImage.errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    System.err.println("[ERRO] Falha ao carregar imagem: " + url);
                    imageView.setImage(new Image("https://placehold.co/180x150/f44336/ffffff?text=Erro"));
                }
            });
        } else {
            imageView.setImage(new Image("https://placehold.co/180x150/e0e0e0/757575?text=Sem+Imagem"));
        }
        return imageView;
    }

    private void showLoading(boolean isLoading, String message) {
        System.out.println("[LOG] Status: " + message);
        statusLabel.setText(message);
        statusContainer.setVisible(isLoading);
        productGrid.setVisible(!isLoading);
    }
}
