package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.models.Venda;
import com.exemplo.models.VendaItem;
import com.exemplo.services.CarrinhoService;
import com.exemplo.services.PecaSupabaseClient;
import com.exemplo.services.SessionManager;
import com.exemplo.services.TransacaoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PdvController {

    // --- Componentes FXML ---
    @FXML private TextField buscaField;
    @FXML private TableView<Peca> tabelaBusca;
    @FXML private TableColumn<Peca, String> colBuscaNome;
    @FXML private TableColumn<Peca, Double> colBuscaPreco;
    @FXML private TableColumn<Peca, Integer> colBuscaEstoque;
    @FXML private TableColumn<Peca, Void> colBuscaAcao;

    @FXML private TableView<VendaItem> tabelaVenda;
    @FXML private TableColumn<VendaItem, String> colVendaNome;
    @FXML private TableColumn<VendaItem, Integer> colVendaQtd;
    @FXML private TableColumn<VendaItem, Double> colVendaPrecoUnit;
    @FXML private TableColumn<VendaItem, Double> colVendaSubtotal;
    @FXML private TableColumn<VendaItem, Void> colVendaAcao;

    @FXML private ComboBox<String> comboPagamento;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private Button btnFinalizarVenda;
    @FXML private Label statusLabel;
    @FXML private TextArea observacoesField;

    // --- Serviços e Utilitários ---
    private final PecaSupabaseClient pecaService = new PecaSupabaseClient();
    private final TransacaoService transacaoService = new TransacaoService();
    private final CarrinhoService carrinhoService = CarrinhoService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        configurarTabelas();
        comboPagamento.getItems().addAll("Dinheiro", "Pix", "Cartão de Débito", "Cartão de Crédito", "Mercado Livre");
        comboPagamento.getSelectionModel().selectFirst();
        tabelaVenda.setItems(carrinhoService.getItens());
        carrinhoService.getItens().addListener((ListChangeListener<VendaItem>) c -> atualizarTotais());
        atualizarTotais();
    }

    private void configurarTabelas() {
        colBuscaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colBuscaPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colBuscaEstoque.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        addButtonToTable(colBuscaAcao, "Adicionar", this::adicionarAoCarrinho);

        colVendaNome.setCellValueFactory(new PropertyValueFactory<>("nomePeca"));
        colVendaQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colVendaPrecoUnit.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colVendaSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        addButtonToTable(colVendaAcao, "Remover", this::removerDoCarrinho);

        formatPriceCell(colBuscaPreco);
        formatPriceCell(colVendaPrecoUnit);
        formatPriceCell(colVendaSubtotal);
    }

    /**
     * Lida com a busca de produtos na tela de Ponto de Venda.
     */
    @FXML
    private void handleBusca() {
        String termo = buscaField.getText();

        Map<String, Object> filtros = new HashMap<>();
        filtros.put("termo_busca_geral", termo);

        new Thread(() -> {
            try {
                // CORREÇÃO APLICADA AQUI: Adicionado o segundo argumento 'false'.
                // Isto diz ao serviço para buscar apenas produtos ativos.
                List<Peca> pecasEncontradas = pecaService.fetchProdutosComFiltros(filtros, false);

                Platform.runLater(() -> {
                    tabelaBusca.setItems(FXCollections.observableArrayList(pecasEncontradas));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleFinalizarVenda() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            exibirAlerta("Erro de Sessão", "Nenhum usuário está logado.\n\nPor favor, saia e faça o login novamente para registar a venda.", Alert.AlertType.ERROR);
            return;
        }

        if (carrinhoService.getItens().isEmpty()) {
            exibirAlerta("Erro", "O carrinho está vazio. Adicione produtos para continuar.", Alert.AlertType.WARNING);
            return;
        }
        if (comboPagamento.getValue() == null) {
            exibirAlerta("Erro", "Selecione um método de pagamento.", Alert.AlertType.WARNING);
            return;
        }

        btnFinalizarVenda.setDisable(true);
        statusLabel.setText("A processar venda...");

        Venda novaVenda = new Venda(
                carrinhoService.getItens().stream().mapToDouble(VendaItem::getSubtotal).sum(),
                comboPagamento.getValue()
        );
        novaVenda.setItens(carrinhoService.getItensCopia());
        novaVenda.setObservacoes(observacoesField.getText());

        new Thread(() -> {
            try {
                transacaoService.registrarVenda(novaVenda);
                Platform.runLater(() -> {
                    exibirAlerta("Sucesso", "Venda registada com sucesso!", Alert.AlertType.INFORMATION);
                    limparVenda();
                });
            } catch (Exception e) {
                Platform.runLater(() -> exibirAlerta("Erro de Conexão", "Falha ao registar a venda: " + e.getMessage(), Alert.AlertType.ERROR));
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    btnFinalizarVenda.setDisable(false);
                    statusLabel.setText("");
                });
            }
        }).start();
    }

    private void adicionarAoCarrinho(Peca peca) {
        carrinhoService.adicionarItem(peca);
    }

    private void removerDoCarrinho(VendaItem item) {
        carrinhoService.removerItem(item);
    }

    private void atualizarTotais() {
        double total = carrinhoService.getItens().stream().mapToDouble(VendaItem::getSubtotal).sum();
        lblSubtotal.setText(currencyFormat.format(total));
        lblTotal.setText(currencyFormat.format(total));
    }

    private void limparVenda() {
        carrinhoService.limparCarrinho();
        buscaField.clear();
        tabelaBusca.getItems().clear();
        comboPagamento.getSelectionModel().selectFirst();
        observacoesField.clear();
    }

    private <T> void addButtonToTable(TableColumn<T, Void> column, String buttonText, java.util.function.Consumer<T> action) {
        Callback<TableColumn<T, Void>, TableCell<T, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<T, Void> call(final TableColumn<T, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button(buttonText);
                    {
                        btn.setOnAction(event -> {
                            T data = getTableView().getItems().get(getIndex());
                            action.accept(data);
                        });
                    }
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        column.setCellFactory(cellFactory);
    }

    private <T> void formatPriceCell(TableColumn<T, Double> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(price));
                }
            }
        });
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
