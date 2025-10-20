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
    
    /**
     * Unique identifier for this view.
     */
    public static final String ID = "com.deepseek.plugin.views.DeepSeekView";
    
    private Text inputText;
    private Text outputText;
    private Button sendButton;
    
    /**
     * Creates the controls and layout for this view.
     * Initializes the user interface components including input area,
     * send button, and response display area.
     *
     * @param parent the parent composite to contain the view components
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        
        Label inputLabel = new Label(container, SWT.NONE);
        inputLabel.setText("Pergunte ao DeepSeek:");
        
        inputText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        inputText.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, false));
        
        sendButton = new Button(container, SWT.PUSH);
        sendButton.setText("Enviar para DeepSeek");
        sendButton.addListener(SWT.Selection, e -> sendToDeepSeek());
        
        Label outputLabel = new Label(container, SWT.NONE);
        outputLabel.setText("Resposta:");
        
        outputText = new Text(container, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        outputText.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL, SWT.FILL, true, true));
    }
    
    /**
     * Handles the sending of user messages to the DeepSeek API.
     * Validates user input, checks for API key configuration,
     * and processes the API response for display.
     */
    private void sendToDeepSeek() {
        String question = inputText.getText();
        if (!question.isEmpty()) {
            if (!ConfigurationManager.hasApiKey()) {
                outputText.setText("Configure sua API Key primeiro:\nWindow → Preferences → DeepSeek Plugin");
                return;
            }
            
            outputText.setText("Chamando DeepSeek API...");
            
            String apiKey = ConfigurationManager.getApiKey();
            DeepSeekAPIClient client = new DeepSeekAPIClient(apiKey);
            String response = client.sendMessage(question);
            
            outputText.setText("DeepSeek:\n\n" + response);
        }
    }
    
    /**
     * Sets the focus to the primary input control of this view.
     */
    @Override
    public void setFocus() {
        inputText.setFocus();
    }
}