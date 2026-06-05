package com.piedrazul.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void onLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        errorLabel.setVisible(false);

        try {
            ObjectNode payload = mapper.createObjectNode();
            payload.put("username", user);
            payload.put("password", pass);
            String body = mapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            try {
                                String respBody = response.body();
                                // Extraer token de respuesta JSON (soporta keys comunes)
                                String token = null;
                                try {
                                    var node = mapper.readTree(respBody);
                                    if (node.has("token")) token = node.get("token").asText();
                                    else if (node.has("accessToken")) token = node.get("accessToken").asText();
                                    else if (node.has("jwt")) token = node.get("jwt").asText();
                                    // Guardar info de usuario si viene en la respuesta
                                    if (node.has("user")) {
                                        AppContext.setUser(node.get("user"));
                                    }
                                } catch (Exception ex) {
                                    // No crítico; seguimos sin token si no viene
                                }

                                if (token != null && !token.isEmpty()) {
                                    AppContext.setToken(token);
                                }

                                final String fToken = token;
                                Platform.runLater(() -> {
                                    try {
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/piedrazul/desktop/DashboardView.fxml"));
                                        Parent root = loader.load();
                                        Stage stage = new Stage();
                                        stage.setTitle("Piedrazul — Dashboard");
                                        stage.setScene(new Scene(root, 900, 600));
                                        stage.show();

                                        // Close login window
                                        Stage current = (Stage) usernameField.getScene().getWindow();
                                        current.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        errorLabel.setText("Error al abrir Dashboard: " + e.getMessage());
                                        errorLabel.setVisible(true);
                                    }
                                });
                            } catch (Throwable t) {
                                t.printStackTrace();
                                Platform.runLater(() -> {
                                    errorLabel.setText("Error procesando respuesta: " + t.getMessage());
                                    errorLabel.setVisible(true);
                                });
                            }
                        } else {
                            Platform.runLater(() -> {
                                errorLabel.setText("Credenciales inválidas (" + response.statusCode() + ")");
                                errorLabel.setVisible(true);
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            errorLabel.setText("Error de conexión: " + ex.getMessage());
                            errorLabel.setVisible(true);
                        });
                        return null;
                    });

        } catch (Throwable t) {
            t.printStackTrace();
            errorLabel.setText("Error interno: " + t.getMessage());
            errorLabel.setVisible(true);
        }
    }
}
