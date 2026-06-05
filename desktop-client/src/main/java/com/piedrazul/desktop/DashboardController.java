package com.piedrazul.desktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import javafx.scene.Node;
import javafx.stage.Window;
import javafx.stage.Stage;
import com.piedrazul.desktop.AppContext;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadView("/com/piedrazul/desktop/appointments/AppointmentsView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onManageAppointments() {
        loadView("/com/piedrazul/desktop/appointments/AppointmentsView.fxml");
    }

    @FXML
    public void onClinicalRecords() {
        // Placeholder: implementar carga de Historiales Clínicos
        System.out.println("Historiales Clínicos - pendiente");
    }

    @FXML
    public void onNetwork() {
        // Placeholder: implementar carga de Red de Servicios
        System.out.println("Red de Servicios - pendiente");
    }

    @FXML
    public void onLogout() {
        // Limpiar token y volver a la pantalla de login
        AppContext.clearToken();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piedrazul/desktop/LoginView.fxml"));
            Parent login = loader.load();
            // Reemplazar la escena en la misma ventana
            Window w = contentArea.getScene().getWindow();
            if (w instanceof Stage) {
                Stage stage = (Stage) w;
                stage.getScene().setRoot(login);
                stage.setTitle("Piedrazul — Iniciar Sesión");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
