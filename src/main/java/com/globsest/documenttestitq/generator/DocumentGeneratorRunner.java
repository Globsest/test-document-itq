package com.globsest.documenttestitq.generator;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Утилита массового создания документов через API сервиса.
 * Читает параметр count из generator.properties (или из classpath).
 * Запуск: gradlew runGenerator или из IDE указать main class DocumentGeneratorRunner.
 */
public class DocumentGeneratorRunner {

    private static final String DEFAULT_PROPERTIES = "generator.properties";

    public static void main(String[] args) throws Exception {
        Properties props = loadProperties();
        int count = Integer.parseInt(props.getProperty("count", "100"));
        String baseUrl = props.getProperty("api.baseUrl", "http://localhost:8080").replaceAll("/$", "");
        String author = props.getProperty("author", "Generator");
        String initiator = props.getProperty("initiator", "document-generator");

        String url = baseUrl + "/api/documents";

        System.out.println("Задано к созданию документов: N = " + count);
        System.out.println("URL сервиса: " + url);
        long startTotal = System.currentTimeMillis();

        int created = 0;
        int errors = 0;

        for (int i = 0; i < count; i++) {
            Map<String, String> body = new HashMap<>();
            body.put("author", author);
            body.put("title", "Документ " + (i + 1));
            body.put("initiator", initiator);

            try {
                postDocument(url, body);
                created++;
                if ((i + 1) % 100 == 0 || i == count - 1) {
                    System.out.println("Прогресс создания: " + (i + 1) + " / " + count + " документов");
                }
            } catch (Exception e) {
                errors++;
                System.err.println("Ошибка создания документа " + (i + 1) + ": " + e.getMessage());
            }
        }

        long elapsedTotal = System.currentTimeMillis() - startTotal;
        System.out.println("Создание завершено за " + elapsedTotal + " мс: создано " + created + ", ошибок " + errors);
    }

    private static Properties loadProperties() throws Exception {
        Properties props = new Properties();
        try (InputStream is = DocumentGeneratorRunner.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES)) {
            if (is != null) {
                props.load(is);
            }
        }
        return props;
    }

    private static void postDocument(String url, Map<String, String> body) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(toJson(body)))
                .build();
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private static String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(escape(e.getKey())).append("\":\"").append(escape(e.getValue())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
