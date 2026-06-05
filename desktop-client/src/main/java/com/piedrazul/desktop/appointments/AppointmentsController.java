package com.piedrazul.desktop.appointments;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AppointmentsController {

    @FXML
    private TextField patientField;

    @FXML
    private TextField doctorField;

    @FXML
    private TextField dateField;

    @FXML
    public void onSaveAppointment() {
        String p = patientField.getText();
        String d = doctorField.getText();
        String date = dateField.getText();
        System.out.println("[Appointments] Guardar: " + p + " | " + d + " | " + date);

        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Cita");
        a.setHeaderText(null);
        a.setContentText("Cita guardada (simulada).\n" + p + " / " + d + " / " + date);
        a.showAndWait();
    }
}
