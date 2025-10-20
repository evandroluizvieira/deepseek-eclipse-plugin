package com.deepseek.plugin.api;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Client for communicating with the DeepSeek API.
 * Handles HTTP requests and responses for AI chat completions.
 */
public class DeepSeekAPIClient {
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private final String apiKey;
    
    /**
     * Constructs a new DeepSeek API client with the provided API key.
     *
     * @param apiKey the DeepSeek API authentication key
     */
    public DeepSeekAPIClient(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Sends a message to the DeepSeek API and returns the response.
     *
     * @param message the user message to send
     * @return the AI response or error message
     */
    public String sendMessage(String message) {
        try {
        	URI uri = URI.create(API_URL);
        	URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            
            String jsonInput = createRequestJson(message);
            
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }
            
            return parseResponse(connection);
            
        } catch (Exception exception) {
            return "Error: " + exception.getMessage();
        }
    }
    
    /**
     * Creates the JSON request payload for the DeepSeek API.
     *
     * @param message the user message
     * @return formatted JSON string
     */
    private String createRequestJson(String message) {
        return String.format(
            "{\"model\": \"deepseek-chat\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}",
            escapeJson(message)
        );
    }
    
    /**
     * Escapes special characters in JSON strings.
     *
     * @param text the text to escape
     * @return escaped JSON string
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Parses the HTTP response from the DeepSeek API.
     *
     * @param connection the HTTP connection
     * @return extracted content from response
     * @throws Exception if reading response fails
     */
    private String parseResponse(HttpURLConnection connection) throws Exception {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return extractContentFromJson(response.toString());
        }
    }
    
    /**
     * Extracts the message content from the JSON response.
     *
     * @param jsonResponse the raw JSON response
     * @return extracted message content
     */
    private String extractContentFromJson(String jsonResponse) {
        if (jsonResponse.contains("\"content\"")) {
            int startIndex = jsonResponse.indexOf("\"content\":\"") + 11;
            int endIndex = jsonResponse.indexOf("\"", startIndex);
            String content = jsonResponse.substring(startIndex, endIndex);
            return unescapeJsonString(content);
        }
        return jsonResponse;
    }
    
    /**
     * Unescapes JSON string by replacing escape sequences with actual characters.
     *
     * @param escapedString the string with JSON escape sequences
     * @return unescaped string
     */
    private String unescapeJsonString(String escapedString) {
        return escapedString.replace("\\n", "\n")
                          .replace("\\r", "\r")
                          .replace("\\t", "\t")
                          .replace("\\\"", "\"")
                          .replace("\\\\", "\\");
    }
}