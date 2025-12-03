package com.deepseek.plugin.api;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client for communicating with the DeepSeek API.
 * Handles HTTP requests and responses for AI chat completions.
 */
public class DeepSeekAPIClient {
    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private final String apiKey;
    private HttpURLConnection currentConnection;
    private final AtomicBoolean isCancelled;
    
    /**
     * Constructs a new DeepSeek API client with the provided API key.
     *
     * @param apiKey the DeepSeek API authentication key
     */
    public DeepSeekAPIClient(String apiKey) {
        this.apiKey = apiKey;
        this.isCancelled = new AtomicBoolean(false);
    }
    
    /**
     * Sends a message to the DeepSeek API and returns the response.
     * Implements retry logic with exponential backoff for transient failures.
     *
     * @param message the user message to send
     * @return the AI response or error message
     */
    public String sendMessage(String message) {
        isCancelled.set(false);
        
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                URI uri = URI.create(API_URL);
                URL url = uri.toURL();
                currentConnection = (HttpURLConnection) url.openConnection();
                
                currentConnection.setConnectTimeout(45000);
                currentConnection.setReadTimeout(120000);
                currentConnection.setRequestMethod("POST");
                currentConnection.setRequestProperty("Content-Type", "application/json");
                currentConnection.setRequestProperty("Authorization", "Bearer " + apiKey);
                currentConnection.setRequestProperty("User-Agent", "DeepSeek-Eclipse-Plugin/1.0");
                currentConnection.setDoOutput(true);
                
                String jsonInput = createRequestJson(message);
                
                try (OutputStream outputStream = currentConnection.getOutputStream()) {
                    byte[] input = jsonInput.getBytes("utf-8");
                    outputStream.write(input, 0, input.length);
                }
                
                if (isCancelled.get()) {
                    return "Requisição cancelada.";
                }
                
                int responseCode = currentConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    if (responseCode == 429) {
                        return "Erro: Rate limit excedido. Tente novamente em alguns instantes.";
                    } else if (responseCode >= 500) {
                        if (attempt < 3) {
                            Thread.sleep(2000 * attempt);
                            continue;
                        }
                        return "Erro: Servidor indisponível (HTTP " + responseCode + ")";
                    } else {
                        return "Erro HTTP: " + responseCode;
                    }
                }
                
                return parseResponse(currentConnection);
                
            } catch (java.net.SocketTimeoutException timeoutException) {
                if (attempt < 3) {
                    try {
                        Thread.sleep(3000 * attempt);
                    } catch (InterruptedException ie) {
                        return "Requisição interrompida.";
                    }
                    continue;
                }
                return "Erro: Timeout - o servidor demorou muito para responder.";
                
            } catch (Exception exception) {
                if (isCancelled.get()) {
                    return "Requisição cancelada.";
                }
                
                if (attempt < 3) {
                    try {
                        Thread.sleep(2000 * attempt);
                    } catch (InterruptedException ie) {
                        return "Requisição interrompida.";
                    }
                    continue;
                }
                
                return "Erro: " + getFriendlyErrorMessage(exception);
                
            } finally {
                currentConnection = null;
            }
        }
        
        return "Erro: Todas as tentativas falharam.";
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
                if (isCancelled.get()) {
                    return "Requisição cancelada.";
                }
                response.append(responseLine.trim());
            }
            return extractContentFromJson(response.toString());
        }
    }

    /**
     * Provides user-friendly error messages for common exceptions.
     *
     * @param exception the exception that occurred
     * @return friendly error message
     */
    private String getFriendlyErrorMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            return "Erro desconhecido.";
        }
        
        if (message.contains("ConnectException") || message.contains("No route to host")) {
            return "Erro de conexão: Verifique sua internet.";
        } else if (message.contains("SSL") || message.contains("certificate")) {
            return "Erro de segurança SSL: Verifique a data/hora do sistema.";
        } else if (message.contains("timed out")) {
            return "Timeout: O servidor demorou muito para responder.";
        } else if (message.contains("401")) {
            return "Erro de autenticação: API Key inválida ou expirada.";
        } else if (message.contains("402")) {
            return "Erro de pagamento: Saldo insuficiente na conta DeepSeek.";
        } else if (message.contains("429")) {
            return "Rate limit excedido: Aguarde alguns instantes.";
        }
        
        return message;
    }
    
    /**
     * Cancels the current API request by closing the connection in a non-blocking way.
     */
    public void cancelRequest() {
        isCancelled.set(true);
        if (currentConnection != null) {
            new Thread(() -> {
                try {
                    currentConnection.disconnect();
                } catch (Exception e) {
                }
            }).start();
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
     * Extracts the message content from the JSON response.
     *
     * @param jsonResponse the raw JSON response
     * @return extracted message content
     */
    private String extractContentFromJson(String jsonResponse) {
        try {
            int contentStart = jsonResponse.indexOf("\"content\":\"");
            if (contentStart == -1) {
                return "Resposta em formato inesperado: " + jsonResponse;
            }
            
            contentStart += 11;
            
            int contentEnd = contentStart;
            int length = jsonResponse.length();
            while (contentEnd < length) {
                char currentChar = jsonResponse.charAt(contentEnd);
                
                if (currentChar == '"') {
                    if (contentEnd > 0 && jsonResponse.charAt(contentEnd - 1) == '\\') {
                        contentEnd++;
                        continue;
                    } else {
                        break;
                    }
                }
                contentEnd++;
            }
            
            if (contentEnd >= length) {
                return "Resposta incompleta ou formato inválido.";
            }
            
            String content = jsonResponse.substring(contentStart, contentEnd);
            return unescapeJsonString(content);
            
        } catch (Exception e) {
            return "Erro ao processar resposta: " + e.getMessage();
        }
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