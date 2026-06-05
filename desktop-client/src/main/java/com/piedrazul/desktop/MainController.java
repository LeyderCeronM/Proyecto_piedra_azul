package com.piedrazul.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import com.piedrazul.desktop.AppContext;

public class MainController {

    @FXML
    public TextArea reportsTextArea;

    @FXML
    public Button btnCheckReports;

    @FXML
    public Button btnListAppointments;

    @FXML
    public ListView<String> appointmentsListView;

    private HttpClient client;

    private ObjectMapper mapper;
    private ObjectWriter writer;

    public MainController() {
        try {
            this.client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            this.mapper = new ObjectMapper();
            this.writer = mapper.writerWithDefaultPrettyPrinter();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        try {
            reportsTextArea.setWrapText(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @FXML
    public void onCheckReports() {
        String uri = "http://localhost:8080/api/v1/reports/statistics?from=2026-01-01&to=2026-06-01";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .timeout(Duration.ofSeconds(10));
        String token = AppContext.getToken();
        if (token != null && !token.isEmpty()) builder.header("Authorization", "Bearer " + token);
        HttpRequest request = builder.build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    try {
                        JsonNode json = mapper.readTree(body);
                        String pretty = writer.writeValueAsString(json);
                        Platform.runLater(() -> reportsTextArea.setText(pretty));
                    } catch (Exception e) {
                        Platform.runLater(() -> reportsTextArea.setText("Error parseando JSON:\n" + body));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> reportsTextArea.setText("Error en petición: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void onListAppointments() {
        String uri = "http://localhost:8080/api/v1/appointments";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .timeout(Duration.ofSeconds(10));
        String token = AppContext.getToken();
        if (token != null && !token.isEmpty()) builder.header("Authorization", "Bearer " + token);
        HttpRequest request = builder.build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    try {
                        JsonNode json = mapper.readTree(body);
                        Platform.runLater(() -> {
                            appointmentsListView.getItems().clear();
                            if (json.isArray()) {
                                for (JsonNode node : json) {
                                    String id = node.has("id") ? node.get("id").asText() : "-";
                                    String patient = node.has("patient") ? node.get("patient").asText() : "-";
                                    String time = node.has("time") ? node.get("time").asText() : "-";
                                    appointmentsListView.getItems().add(String.format("%s — %s — %s", id, patient, time));
                                }
                            } else {
                                // If single object, show keys
                                Iterator<String> it = json.fieldNames();
                                while (it.hasNext()) {
                                    String f = it.next();
                                    appointmentsListView.getItems().add(f + ": " + json.get(f).asText());
                                }
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> appointmentsListView.getItems().setAll("Error parseando respuesta", e.getMessage()));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> appointmentsListView.getItems().setAll("Error en petición: " + ex.getMessage()));
                    return null;
                });
    }
}
