package com.deepseek.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

import com.deepseek.plugin.api.DeepSeekAPIClient;
import com.deepseek.plugin.configuration.ConfigurationManager;

/**
 * Main view for interacting with the DeepSeek AI assistant.
 * Provides a chat interface within the Eclipse IDE.
 */
public class DeepSeekView extends ViewPart {
    
    public static final String ID = "com.deepseek.plugin.views.DeepSeekView";
    
    private Text inputText;
    private Text outputText;
    private Button sendButton;
    private Button cancelButton;
    private boolean isProcessing;
    private Thread apiThread;
    private DeepSeekAPIClient apiClient;
    
    /**
     * Creates the controls and layout for this view.
     *
     * @param parent the parent composite to contain the view components
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite mainContainer = new Composite(parent, SWT.NONE);
        mainContainer.setLayout(new GridLayout(1, false));
        
        Label outputLabel = new Label(mainContainer, SWT.NONE);
        outputLabel.setText("Histórico de Conversa:");
        outputLabel.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        outputText = new Text(mainContainer, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        org.eclipse.swt.layout.GridData outputGridData = new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true);
        outputGridData.heightHint = 300;
        outputText.setLayoutData(outputGridData);
        
        Label separator = new Label(mainContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label inputLabel = new Label(mainContainer, SWT.NONE);
        inputLabel.setText("Nova Pergunta:");
        inputLabel.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        inputText = new Text(mainContainer, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        org.eclipse.swt.layout.GridData inputGridData = new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, false);
        inputGridData.heightHint = 80;
        inputText.setLayoutData(inputGridData);
        
        Composite buttonContainer = new Composite(mainContainer, SWT.NONE);
        buttonContainer.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.CENTER, true, false));
        buttonContainer.setLayout(new GridLayout(2, true));
        
        sendButton = new Button(buttonContainer, SWT.PUSH);
        sendButton.setText("Enviar para DeepSeek");
        sendButton.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.CENTER, true, false));
        sendButton.addListener(SWT.Selection, e -> sendToDeepSeek());
        
        cancelButton = new Button(buttonContainer, SWT.PUSH);
        cancelButton.setText("Cancelar");
        cancelButton.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.addListener(SWT.Selection, e -> cancelRequest());
        cancelButton.setEnabled(false);
        
        outputText.setText("Bem-vindo ao DeepSeek Assistant!\n\n");
    }
    
    /**
     * Handles the sending of user messages to the DeepSeek API.
     */
    private void sendToDeepSeek() {
        String question = inputText.getText().trim();
        if (question.isEmpty() || isProcessing) {
            return;
        }
        
        if (!ConfigurationManager.hasApiKey()) {
            appendToOutput("Erro: Configure sua API Key primeiro em Window → Preferences → DeepSeek Plugin\n\n");
            return;
        }
        
        setProcessingState(true);
        
        appendToOutput("Você: " + question + "\n");
        appendToOutput("DeepSeek: Processando...\n");
        
        String savedQuestion = question;
        inputText.setText("");
        
        apiThread = new Thread(() -> {
            try {
                String apiKey = ConfigurationManager.getApiKey();
                apiClient = new DeepSeekAPIClient(apiKey);
                String response = apiClient.sendMessage(savedQuestion);
                
                if (!apiThread.isInterrupted()) {
                    Display.getDefault().asyncExec(() -> {
                        replaceLastLine("DeepSeek: " + response + "\n\n");
                        setProcessingState(false);
                    });
                }
                
            } catch (Exception exception) {
                if (!apiThread.isInterrupted()) {
                    Display.getDefault().asyncExec(() -> {
                        replaceLastLine("DeepSeek: Erro - " + exception.getMessage() + "\n\n");
                        setProcessingState(false);
                    });
                }
            } finally {
                apiClient = null;
            }
        });
        
        apiThread.start();
    }

    /**
     * Cancels the current API request in a non-blocking way.
     */
    private void cancelRequest() {
        if (isProcessing) {
            if (apiThread != null) {
                apiThread.interrupt();
            }
            if (apiClient != null) {
                apiClient.cancelRequest();
            }
            Display.getDefault().asyncExec(() -> {
                replaceLastLine("DeepSeek: Requisição cancelada pelo usuário.\n\n");
                setProcessingState(false);
            });
        }
    }
    
    /**
     * Appends text to the output area and scrolls to the bottom.
     *
     * @param text the text to append
     */
    private void appendToOutput(String text) {
        outputText.append(text);
        scrollToBottom();
    }
    
    /**
     * Replaces the last line in the output area with new text.
     *
     * @param newText the new text to replace the last line
     */
    private void replaceLastLine(String newText) {
        String currentText = outputText.getText();
        
        String targetLine = "DeepSeek: Processando...\n";
        int lastIndex = currentText.lastIndexOf(targetLine);
        
        if (lastIndex != -1) {
            String before = currentText.substring(0, lastIndex);
            String after = currentText.substring(lastIndex + targetLine.length());
            String updatedText = before + newText + after;
            outputText.setText(updatedText);
            scrollToBottom();
        } else {
            appendToOutput(newText);
        }
    }
    
    /**
     * Scrolls the output text area to the bottom.
     */
    private void scrollToBottom() {
        outputText.getVerticalBar().setSelection(outputText.getVerticalBar().getMaximum());
    }
    
    /**
     * Updates the UI state to reflect processing status.
     *
     * @param processing true when an API request is in progress
     */
    private void setProcessingState(boolean processing) {
        this.isProcessing = processing;
        sendButton.setEnabled(!processing);
        sendButton.setText(processing ? "Processando..." : "Enviar para DeepSeek");
        cancelButton.setEnabled(processing);
        inputText.setEnabled(!processing);
    }
    
    /**
     * Sets the focus to the primary input control of this view.
     */
    @Override
    public void setFocus() {
        inputText.setFocus();
    }
}