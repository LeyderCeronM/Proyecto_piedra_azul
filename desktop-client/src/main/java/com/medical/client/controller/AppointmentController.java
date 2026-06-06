package com.medical.client.controller;

import com.medical.client.dto.AppointmentDto;
import com.medical.client.service.AppointmentServiceClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AppointmentController {
    @FXML
    private TableView<AppointmentDto> appointmentsTable;

    @FXML
    private TableColumn<AppointmentDto, String> colPatient;

    @FXML
    private TableColumn<AppointmentDto, String> colProfessional;

    @FXML
    private TableColumn<AppointmentDto, String> colDate;

    private AppointmentServiceClient serviceClient;

    @FXML
    public void initialize() {
        // Configure table columns to map to AppointmentDto getters
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colProfessional.setCellValueFactory(new PropertyValueFactory<>("professionalName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        // Simple injection: provide the base URL of the appointments microservice
        String appointmentsBaseUrl = "http://localhost:8081"; // ajustar según entorno
        serviceClient = AppointmentServiceClient.getInstance(appointmentsBaseUrl);

        loadAppointmentsAsync();
    }

    private void loadAppointmentsAsync() {
        Task<List<AppointmentDto>> task = new Task<>() {
            @Override
            protected List<AppointmentDto> call() throws Exception {
                return serviceClient.getAllAppointments();
            }
        };

        task.setOnSucceeded(evt -> {
            List<AppointmentDto> list = task.getValue();
            ObservableList<AppointmentDto> items = FXCollections.observableArrayList(list);
            appointmentsTable.setItems(items);
        });

        task.setOnFailed(evt -> {
            Throwable ex = task.getException();
            showErrorAlert("No se pudo cargar las citas", ex.getMessage());
        });

        new Thread(task).start();
    }

    private void showErrorAlert(String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de conexión");
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
