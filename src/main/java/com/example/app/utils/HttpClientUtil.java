package com.example.app.utils;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpClientUtil {

    private static final int TIMEOUT_MSEC = 5000;

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(TIMEOUT_MSEC))
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String doGet(String url, Map<String, Object> paramMap) throws Exception {
        String query = "";

        if (paramMap != null && !paramMap.isEmpty()) {
            query = paramMap.entrySet()
                    .stream()
                    .map(entry -> encode(entry.getKey()) + "=" + encode(String.valueOf(entry.getValue())))
                    .collect(Collectors.joining("&"));
        }

        String requestUrl = query.isEmpty() ? url : url + "?" + query;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .timeout(Duration.ofMillis(TIMEOUT_MSEC))
                .GET()
                .build();

        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String doPost(String url, Map<String, String> paramMap) throws Exception {
        String form = "";

        if (paramMap != null && !paramMap.isEmpty()) {
            form = paramMap.entrySet()
                    .stream()
                    .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                    .collect(Collectors.joining("&"));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(TIMEOUT_MSEC))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static String doPost4Json(String url, Map<String, Object> paramMap) throws Exception {
        String json = OBJECT_MAPPER.writeValueAsString(paramMap);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(TIMEOUT_MSEC))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}