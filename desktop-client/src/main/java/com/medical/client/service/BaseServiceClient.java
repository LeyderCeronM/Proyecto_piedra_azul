package com.medical.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public abstract class BaseServiceClient {
    protected final HttpClient httpClient;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;

    protected BaseServiceClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper()
                .registerModules((Iterable<? extends Module>) new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected <T> T sendGet(String path, Class<T> clazz) throws ServiceException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return objectMapper.readValue(resp.body(), clazz);
            }
            throw new ServiceException(resp.statusCode(), resp.body());
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("GET request failed: " + e.getMessage(), e);
        }
    }

    protected <T> T sendGet(String path, TypeReference<T> typeRef) throws ServiceException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return objectMapper.readValue(resp.body(), typeRef);
            }
            throw new ServiceException(resp.statusCode(), resp.body());
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("GET request failed: " + e.getMessage(), e);
        }
    }

    protected <T> T sendPost(String path, Object body, Class<T> clazz) throws ServiceException {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return objectMapper.readValue(resp.body(), clazz);
            }
            throw new ServiceException(resp.statusCode(), resp.body());
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("POST request failed: " + e.getMessage(), e);
        }
    }

    protected <T> T sendPut(String path, Object body, Class<T> clazz) throws ServiceException {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                return objectMapper.readValue(resp.body(), clazz);
            }
            throw new ServiceException(resp.statusCode(), resp.body());
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("PUT request failed: " + e.getMessage(), e);
        }
    }
}
