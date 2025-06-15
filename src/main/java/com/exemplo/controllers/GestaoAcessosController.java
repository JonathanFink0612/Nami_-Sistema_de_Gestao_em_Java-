package com.exemplo.controllers;

import com.exemplo.models.UserProfile;
import com.exemplo.services.PecaSupabaseClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class GestaoAcessosController {

    @FXML private TableView<UserProfile> tabelaUsuarios;
    @FXML private TableColumn<UserProfile, String> colEmail;
    @FXML private TableColumn<UserProfile, String> colCargo;
    @FXML private ProgressIndicator progressIndicator;

    private final PecaSupabaseClient pecaService = new PecaSupabaseClient();
    private final ObservableList<String> cargosDisponiveis = FXCollections.observableArrayList(
            "ESTAGIARIO", "NIVEL1", "NIVEL2", "SUPERVISOR"
    );

    @FXML
    public void initialize() {
        configurarTabela();
        carregarUsuarios();
    }

    private void configurarTabela() {
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Torna a coluna de cargo editável com uma ComboBox
        colCargo.setCellFactory(ComboBoxTableCell.forTableColumn(cargosDisponiveis));

        // Define o que acontece quando um cargo é alterado e confirmado
        colCargo.setOnEditCommit(event -> {
            UserProfile user = event.getRowValue();
            String novoCargo = event.getNewValue();
            if (novoCargo != null && !novoCargo.equals(user.getRole())) {
                atualizarCargoUsuario(user, novoCargo);
            }
        });
    }

    private void carregarUsuarios() {
        progressIndicator.setVisible(true);
        new Thread(() -> {
            try {
                List<UserProfile> profiles = pecaService.fetchAllProfiles();
                Platform.runLater(() -> {
                    tabelaUsuarios.setItems(FXCollections.observableArrayList(profiles));
                    progressIndicator.setVisible(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> progressIndicator.setVisible(false));
            }
        }).start();
    }

    private void atualizarCargoUsuario(UserProfile user, String novoCargo) {
        new Thread(() -> {
            try {
                pecaService.updateUserRole(user.getId(), novoCargo);
                Platform.runLater(() -> {
                    user.setRole(novoCargo); // Atualiza a visualização na tabela
                    new Alert(Alert.AlertType.INFORMATION, "Cargo de " + user.getEmail() + " atualizado para " + novoCargo).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Falha ao atualizar o cargo.").show());
            }
        }).start();
    }
}
