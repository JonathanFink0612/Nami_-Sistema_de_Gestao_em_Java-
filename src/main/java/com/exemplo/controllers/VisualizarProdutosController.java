package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.PecaSupabaseClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VisualizarProdutosController {

    // --- Componentes FXML ---
    @FXML private TextField searchField;
    @FXML private CheckBox checkMostrarInativos;
    @FXML private TableView<Peca> tabelaProdutos;
    @FXML private TableColumn<Peca, Integer> colId;
    @FXML private TableColumn<Peca, String> colNome;
    @FXML private TableColumn<Peca, String> colTipo;
    @FXML private TableColumn<Peca, Double> colPreco;
    @FXML private TableColumn<Peca, Integer> colQuantidade;
    @FXML private TableColumn<Peca, Boolean> colStatus;
    @FXML private TableColumn<Peca, Void> colAcoes;
    @FXML private ProgressIndicator progressIndicator;

    // --- Serviços ---
    private final PecaSupabaseClient pecaService = new PecaSupabaseClient();
    private DashboardController dashboardController; // Para futuras integrações

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        configurarTabela();
        handleSearch(); // Carrega os dados iniciais
        // Adiciona um listener para recarregar a tabela sempre que a checkbox mudar
        checkMostrarInativos.setOnAction(event -> handleSearch());
    }

    /**
     * Configura as colunas da tabela, definindo como os dados devem ser exibidos
     * e adicionando a célula de ações customizada.
     */
    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("pecaId"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoPeca"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("ativo"));

        // Formata a célula de status para mostrar texto e cor
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Ativo" : "Inativo");
                    setStyle(item ? "-fx-text-fill: #2f855a;" : "-fx-text-fill: #c53030; -fx-font-weight: bold;");
                }
            }
        });

        // Adiciona a célula com os botões de ação
        colAcoes.setCellFactory(criarFabricaDeBotoes());
    }

    /**
     * Lida com o evento de busca, reunindo os filtros e chamando o método de carregamento.
     */
    @FXML
    private void handleSearch() {
        Map<String, Object> filtros = new HashMap<>();
        filtros.put("termo_busca_geral", searchField.getText());
        carregarProdutos(filtros, checkMostrarInativos.isSelected());
    }

    /**
     * Carrega os produtos do Supabase numa thread separada para não bloquear a UI.
     * @param filtros O mapa de filtros para a busca.
     * @param incluirInativos Se deve ou não incluir produtos inativos.
     */
    private void carregarProdutos(Map<String, Object> filtros, boolean incluirInativos) {
        if (progressIndicator != null) progressIndicator.setVisible(true);
        tabelaProdutos.setPlaceholder(new Label("A carregar produtos..."));

        new Thread(() -> {
            try {
                List<Peca> produtos = pecaService.fetchProdutosComFiltros(filtros, incluirInativos);
                Platform.runLater(() -> {
                    tabelaProdutos.getItems().setAll(produtos);
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                    if (produtos.isEmpty()) {
                        tabelaProdutos.setPlaceholder(new Label("Nenhum produto encontrado."));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (progressIndicator != null) progressIndicator.setVisible(false);
                    tabelaProdutos.setPlaceholder(new Label("Erro ao carregar produtos."));
                });
            }
        }).start();
    }

    /**
     * Cria e retorna uma fábrica de células customizada que contém os botões de ação.
     */
    private Callback<TableColumn<Peca, Void>, TableCell<Peca, Void>> criarFabricaDeBotoes() {
        return param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstoque = new Button("Add Estoque");
            private final Button btnAtivarDesativar = new Button();
            private final HBox painelAcoes = new HBox(10, btnEditar, btnEstoque, btnAtivarDesativar);

            {
                painelAcoes.setAlignment(Pos.CENTER);
                btnEditar.setOnAction(event -> handleEditar(getTableRow().getItem()));
                btnEstoque.setOnAction(event -> handleAdicionarEstoque(getTableRow().getItem()));
                btnAtivarDesativar.setOnAction(event -> {
                    Peca peca = getTableRow().getItem();
                    if (peca.isAtivo()) {
                        handleDesativar(peca);
                    } else {
                        handleReativar(peca);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }

                Peca peca = getTableRow().getItem();
                btnEditar.setDisable(!peca.isAtivo());
                if (peca.isAtivo()) {
                    btnAtivarDesativar.setText("Desativar");
                    btnAtivarDesativar.setStyle("-fx-background-color: #e53e3e; -fx-text-fill: white; -fx-font-weight: bold;");
                } else {
                    btnAtivarDesativar.setText("Reativar");
                    btnAtivarDesativar.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-font-weight: bold;");
                }
                setGraphic(painelAcoes);
            }
        };
    }

    /**
     * Abre uma janela de diálogo para editar os dados de um produto.
     */
    private void handleEditar(Peca peca) {
        Dialog<Peca> dialog = new Dialog<>();
        dialog.setTitle("Editar Produto");
        dialog.setHeaderText("A editar: " + peca.getNome());

        ButtonType btnSalvar = new ButtonType("Salvar Alterações", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nomeField = new TextField(peca.getNome());
        TextField precoField = new TextField(String.valueOf(peca.getPreco()));
        TextArea descArea = new TextArea(peca.getDescricao());

        grid.add(new Label("Nome:"), 0, 0); grid.add(nomeField, 1, 0);
        grid.add(new Label("Preço:"), 0, 1); grid.add(precoField, 1, 1);
        grid.add(new Label("Descrição:"), 0, 2); grid.add(descArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSalvar) {
                try {
                    peca.setNome(nomeField.getText());
                    peca.setPreco(Double.parseDouble(precoField.getText()));
                    peca.setDescricao(descArea.getText());
                    return peca;
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Valor de preço inválido.").show();
                    return null;
                }
            }
            return null;
        });

        Optional<Peca> result = dialog.showAndWait();
        result.ifPresent(pecaEditada -> {
            new Thread(() -> {
                try {
                    pecaService.atualizarPeca(pecaEditada);
                    Platform.runLater(this::handleSearch);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao atualizar o produto.").show());
                }
            }).start();
        });
    }

    /**
     * Abre uma janela de diálogo para adicionar mais unidades ao estoque.
     */
    private void handleAdicionarEstoque(Peca peca) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Adicionar Estoque");
        dialog.setHeaderText("Adicionar estoque para: " + peca.getNome());
        dialog.setContentText("Quantidade a adicionar:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtdStr -> {
            try {
                int quantidade = Integer.parseInt(qtdStr);
                if (quantidade > 0) {
                    new Thread(() -> {
                        try {
                            pecaService.adicionarEstoque(peca.getPecaId(), quantidade);
                            Platform.runLater(this::handleSearch);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Por favor, insira um número válido.").show();
            }
        });
    }

    /**
     * Lida com a ação de desativar (soft delete) um produto.
     */
    private void handleDesativar(Peca peca) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Desativação");
        alert.setHeaderText("Desativar produto: " + peca.getNome());
        alert.setContentText("Tem a certeza que deseja desativar este produto? Ele ficará invisível na loja.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    pecaService.desativarPeca(peca.getPecaId());
                    Platform.runLater(this::handleSearch);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao desativar: " + e.getMessage()).show());
                }
            }).start();
        }
    }

    /**
     * Lida com a ação de reativar um produto.
     */
    private void handleReativar(Peca peca) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Reativação");
        alert.setHeaderText("Reativar produto: " + peca.getNome());
        alert.setContentText("Tem a certeza que deseja reativar este produto?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    pecaService.reativarPeca(peca.getPecaId());
                    Platform.runLater(this::handleSearch);
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao reativar: " + e.getMessage()).show());
                }
            }).start();
        }
    }
}
