package com.exemplo.controllers;

import com.exemplo.models.Peca;
import com.exemplo.services.CarrinhoService;
import com.exemplo.services.PecaSupabaseClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VendasController {

    // --- Componentes FXML ---
    @FXML private TilePane productGrid;
    @FXML private TextField campoBusca;
    @FXML private VBox categoriaFiltrosBox;
    @FXML private RadioButton rbPotenciaEntrada;
    @FXML private RadioButton rbPotenciaIntermediario;
    @FXML private RadioButton rbPotenciaAvancado;
    @FXML private TextField precoMinField;
    @FXML private TextField precoMaxField;
    @FXML private ToggleGroup grupoPotencia;

    // --- Serviços e Listas de Controlo ---
    private final PecaSupabaseClient pecaService = new PecaSupabaseClient();
    private final CarrinhoService carrinhoService = CarrinhoService.getInstance();
    private final List<CheckBox> listaFiltrosCategoria = new ArrayList<>();

    @FXML
    public void initialize() {
        // Associa programaticamente os RadioButtons ao ToggleGroup
        rbPotenciaEntrada.setToggleGroup(grupoPotencia);
        rbPotenciaIntermediario.setToggleGroup(grupoPotencia);
        rbPotenciaAvancado.setToggleGroup(grupoPotencia);

        criarFiltrosDeCategoria();
        aplicarFiltros();
    }

    private void criarFiltrosDeCategoria() {
        List<String> tiposDePeca = List.of("PlacaDeVideo", "Processador", "Ram", "FonteDeAlimentacao", "PlacaMae");
        for (String tipo : tiposDePeca) {
            CheckBox cb = new CheckBox(formatarNomeParaExibicao(tipo));
            cb.setUserData(tipo);
            listaFiltrosCategoria.add(cb);
            categoriaFiltrosBox.getChildren().add(cb);
        }
    }

    @FXML
    void aplicarFiltros() {
        Map<String, Object> filtrosAtivos = new HashMap<>();

        filtrosAtivos.put("termo_busca_geral", campoBusca.getText());

        List<String> categoriasSelecionadas = listaFiltrosCategoria.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.toList());
        if (!categoriasSelecionadas.isEmpty()) {
            filtrosAtivos.put("tipo_peca", categoriasSelecionadas);
        }

        if (grupoPotencia.getSelectedToggle() != null) {
            RadioButton selected = (RadioButton) grupoPotencia.getSelectedToggle();
            if (selected == rbPotenciaEntrada) {
                filtrosAtivos.put("poder_computacional.lte", 3);
            } else if (selected == rbPotenciaIntermediario) {
                filtrosAtivos.put("poder_computacional.gte", 4);
                filtrosAtivos.put("poder_computacional.lte", 7);
            } else if (selected == rbPotenciaAvancado) {
                filtrosAtivos.put("poder_computacional.gte", 8);
            }
        }

        try {
            if (!precoMinField.getText().isEmpty()) {
                filtrosAtivos.put("preco.gte", Double.parseDouble(precoMinField.getText()));
            }
            if (!precoMaxField.getText().isEmpty()) {
                filtrosAtivos.put("preco.lte", Double.parseDouble(precoMaxField.getText()));
            }
        } catch (NumberFormatException e) {
            System.err.println("Valor de preço inválido: " + e.getMessage());
        }

        // A página da Loja sempre busca apenas produtos ativos.
        carregarProdutos(filtrosAtivos, false);
    }

    @FXML
    void limparFiltroPotencia() {
        if (grupoPotencia.getSelectedToggle() != null) {
            grupoPotencia.getSelectedToggle().setSelected(false);
        }
        aplicarFiltros();
    }

    /**
     * Executa a busca no Supabase numa thread separada para não bloquear a UI.
     * @param filtros O mapa de filtros a ser enviado para o serviço.
     * @param incluirInativos Se deve ou não incluir produtos inativos na busca.
     */
    private void carregarProdutos(Map<String, Object> filtros, boolean incluirInativos) {
        productGrid.getChildren().clear();
        productGrid.getChildren().add(new Label("A carregar produtos..."));

        new Thread(() -> {
            try {
                // CORREÇÃO: A chamada ao serviço agora inclui o booleano.
                List<Peca> produtos = pecaService.fetchProdutosComFiltros(filtros, incluirInativos);
                Platform.runLater(() -> {
                    productGrid.getChildren().clear();
                    if (produtos.isEmpty()) {
                        productGrid.getChildren().add(new Label("Nenhum produto encontrado."));
                    } else {
                        produtos.forEach(p -> productGrid.getChildren().add(criarCardProduto(p)));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao carregar produtos.").show());
            }
        }).start();
    }

    private VBox criarCardProduto(Peca peca) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        card.setEffect(new DropShadow(10, 5, 5, Color.rgb(0, 0, 0, 0.08)));
        card.setPrefSize(220, 320);

        String imageUrl = peca.getImageUrls().isEmpty() ? "https://placehold.co/140" : peca.getImageUrls().get(0);
        ImageView imageView = new ImageView(new Image(imageUrl, true));
        imageView.setFitHeight(140);
        imageView.setFitWidth(140);
        imageView.setPreserveRatio(true);

        Label nomeLabel = new Label(peca.getNome());
        nomeLabel.setWrapText(true);
        nomeLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
        nomeLabel.setMinHeight(40);

        Label precoLabel = new Label(String.format("R$ %.2f", peca.getPreco()));
        precoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Button btnAdicionar = new Button("Adicionar ao Carrinho");
        btnAdicionar.setMaxWidth(Double.MAX_VALUE);
        btnAdicionar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        btnAdicionar.setOnAction(event -> {
            carrinhoService.adicionarItem(peca);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, peca.getNome() + " adicionado ao carrinho!");
            alert.setHeaderText("Sucesso");
            alert.show();
        });

        card.getChildren().addAll(imageView, nomeLabel, precoLabel, btnAdicionar);
        return card;
    }

    private String formatarNomeParaExibicao(String nomeTecnico) {
        return nomeTecnico.replaceAll("([A-Z])", " $1").trim();
    }
}
