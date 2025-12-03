package com.deepseek.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.deepseek.plugin.api.DeepSeekAPIClient;
import com.deepseek.plugin.configuration.ConfigurationManager;
import com.deepseek.plugin.ui.ChatBubble;

/**
 * Main view of the DeepSeek Eclipse Plugin.
 * Displays the conversation history and provides controls
 * to send user messages to the DeepSeek API.
 * 
 * <p>This view includes a scrollable chat interface constructed
 * using custom ChatBubble widgets, along with an input field and
 * actions for sending and canceling API requests.</p>
 */
public class DeepSeekView extends ViewPart {

    /** The Eclipse view ID. */
    public static final String ID = "com.deepseek.plugin.views.DeepSeekView";

    private Text inputText;
    private Button sendButton;
    private Button cancelButton;

    private boolean isProcessing;
    private Thread apiThread;
    private DeepSeekAPIClient apiClient;

    private Composite messageContainer;
    private ScrolledComposite scroller;

    /**
     * Creates the UI structure for the DeepSeek view.
     *
     * <p>This method sets up the conversation history panel, input controls,
     * and the initial welcome message.</p>
     *
     * @param parent the parent composite into which the view is created
     */
    @Override
    public void createPartControl(Composite parent) {

        Composite main = new Composite(parent, SWT.NONE);
        main.setLayout(new GridLayout(1, false));

        Label outputLabel = new Label(main, SWT.NONE);
        outputLabel.setText("History:");
        outputLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        scroller = new ScrolledComposite(main, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scroller.setExpandVertical(true);
        scroller.setExpandHorizontal(true);

        messageContainer = new Composite(scroller, SWT.NONE);
        messageContainer.setLayout(new GridLayout(1, false));
        messageContainer.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        scroller.setContent(messageContainer);
        scroller.setMinSize(messageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        Label inputLabel = new Label(main, SWT.NONE);
        inputLabel.setText("New Question:");
        inputLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        inputText = new Text(main, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData inputGD = new GridData(SWT.FILL, SWT.TOP, true, false);
        inputGD.heightHint = 80;
        inputText.setLayoutData(inputGD);

        Composite buttonBar = new Composite(main, SWT.NONE);
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        buttonBar.setLayout(new GridLayout(2, true));

        sendButton = new Button(buttonBar, SWT.PUSH);
        sendButton.setText("Send");
        sendButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        sendButton.addListener(SWT.Selection, e -> sendMessage());

        cancelButton = new Button(buttonBar, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.setEnabled(false);
        cancelButton.addListener(SWT.Selection, e -> cancelRequest());

        addBubble(ChatBubble.BubbleType.AI, "Welcome to DeepSeek Assistant!");
    }

    /**
     * Adds a new chat bubble to the conversation history.
     *
     * @param type the bubble type (USER or AI)
     * @param msg  the message content
     */
    private void addBubble(ChatBubble.BubbleType type, String msg) {
        ChatBubble bubble = new ChatBubble(
                messageContainer,
                type,
                type == ChatBubble.BubbleType.USER ? "User" : "DeepSeek",
                msg
        );

        bubble.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        bubble.adjustBubbleToTextContent();

        messageContainer.layout(true, true);
        scroller.setMinSize(messageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrollToBottom();
    }

    /**
     * Replaces the latest bubble with new message content.
     *
     * @param newMessage the new text to display in the last bubble
     */
    private void replaceLastBubble(String newMessage) {
        Control[] children = messageContainer.getChildren();
        if (children.length == 0) return;

        Control last = children[children.length - 1];
        if (last instanceof ChatBubble bubble) {
            bubble.updateMessage(newMessage);
            messageContainer.layout(true, true);
            scroller.setMinSize(messageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            scrollToBottom();
        }
    }

    /**
     * Sends a user question to the DeepSeek API.
     *
     * <p>This method creates a new thread for the API request,
     * manages UI updates, and handles success and error responses.</p>
     */
    private void sendMessage() {
        String question = inputText.getText().trim();
        if (question.isEmpty() || isProcessing) return;

        if (!ConfigurationManager.hasApiKey()) {
            addBubble(ChatBubble.BubbleType.AI,
                    "Erro: Configure sua API Key primeiro.\nWindow → Preferences → DeepSeek Plugin");
            return;
        }

        inputText.setText("");
        addBubble(ChatBubble.BubbleType.USER, question);
        addBubble(ChatBubble.BubbleType.AI, "Processando...");

        setProcessingState(true);

        apiThread = new Thread(() -> {
            try {
                apiClient = new DeepSeekAPIClient(ConfigurationManager.getApiKey());
                String result = apiClient.sendMessage(question);

                if (!apiThread.isInterrupted()) {
                    Display.getDefault().asyncExec(() -> {
                        replaceLastBubble(result);
                        setProcessingState(false);
                    });
                }

            } catch (Exception ex) {
                if (!apiThread.isInterrupted()) {
                    Display.getDefault().asyncExec(() -> {
                        replaceLastBubble("Erro: " + ex.getMessage());
                        setProcessingState(false);
                    });
                }
            }
        });

        apiThread.start();
    }

    /**
     * Cancels the active API request, if any.
     *
     * <p>This method interrupts the worker thread, cancels the
     * API client request, and updates the UI accordingly.</p>
     */
    private void cancelRequest() {
        if (!isProcessing) return;

        if (apiThread != null) apiThread.interrupt();
        if (apiClient != null) apiClient.cancelRequest();

        replaceLastBubble("Requisição cancelada pelo usuário.");
        setProcessingState(false);
    }

    /**
     * Scrolls the view to the bottom of the conversation history.
     */
    private void scrollToBottom() {
        scroller.getVerticalBar().setSelection(
            scroller.getVerticalBar().getMaximum()
        );
    }

    /**
     * Updates the processing state and enables/disables UI controls.
     *
     * @param p true if an API request is running
     */
    private void setProcessingState(boolean p) {
        this.isProcessing = p;
        sendButton.setEnabled(!p);
        cancelButton.setEnabled(p);
    }

    /**
     * Sets focus on the input field when the view becomes active.
     */
    @Override
    public void setFocus() {
        inputText.setFocus();
    }
}
