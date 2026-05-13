package com.deepseek.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.deepseek.plugin.api.DeepSeekAPIClient;
import com.deepseek.plugin.configuration.ConfigurationManager;
import com.deepseek.plugin.ui.ChatBubble;
import com.deepseek.plugin.ui.ChatBubbleIA;
import com.deepseek.plugin.ui.ChatBubbleUser;

/**
 * Main view of the DeepSeek Eclipse Plugin.
 * Displays the conversation history and provides controls
 * to send user messages to the DeepSeek API.
 *
 * This view includes a scrollable chat interface constructed
 * using custom ChatBubble widgets, along with an input field and
 * actions for sending and canceling API requests.
 */
public class DeepSeekView extends ViewPart {

    /**
     * The Eclipse view ID for this plugin view.
     */
    public static final String ID = "com.deepseek.plugin.views.DeepSeekView";

    private Text inputText;                  // Input field for user questions
    private Button sendButton;               // Button to send user input
    private Button cancelButton;             // Button to cancel API request

    private boolean isProcessing;            // Indicates if an API request is running
    private Thread apiThread;                // Thread for API requests
    private DeepSeekAPIClient apiClient;     // Client for DeepSeek API

    private Composite messageContainer;      // Container for chat bubbles
    private ScrolledComposite scroller;      // Scrollable area for chat history

    /**
     * Creates the UI structure for the DeepSeek view.
     *
     * This method sets up the conversation history panel, input controls,
     * and the initial welcome message.
     *
     * @param parent the parent composite into which the view is created
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite main = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginWidth = 10;
        mainLayout.marginHeight = 8;
        main.setLayout(mainLayout);

        // Header
        Composite header = new Composite(main, SWT.NONE);
        header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        header.setLayout(new GridLayout(3, false));
        Label title = new Label(header, SWT.NONE);
        title.setText("DeepSeek Assistant");
        title.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        Button settingsButton = new Button(header, SWT.PUSH);
        settingsButton.setText("Settings");
        settingsButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        settingsButton.addListener(SWT.Selection, e -> {
            PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(),
                    "com.deepseek.plugin.preferences.DeepSeekPreferencesPage", null, null).open();
        });

        // Chat area
        scroller = new ScrolledComposite(main, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scroller.setExpandVertical(true);
        scroller.setExpandHorizontal(true);

        messageContainer = new Composite(scroller, SWT.NONE);
        GridLayout mcLayout = new GridLayout(1, false);
        mcLayout.marginWidth = 8;
        mcLayout.marginHeight = 8;
        messageContainer.setLayout(mcLayout);
        messageContainer.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        scroller.setContent(messageContainer);
        scroller.setMinSize(messageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // Input area: single-line input with send button to mimic chat web
        Composite inputRow = new Composite(main, SWT.NONE);
        inputRow.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        GridLayout inputLayout = new GridLayout(2, false);
        inputLayout.marginWidth = 0;
        inputLayout.marginHeight = 0;
        inputLayout.horizontalSpacing = 8;
        inputRow.setLayout(inputLayout);

        inputText = new Text(inputRow, SWT.BORDER | SWT.SINGLE);
        inputText.setMessage("Type your question...");
        inputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        sendButton = new Button(inputRow, SWT.PUSH);
        sendButton.setText("Send");
        sendButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        sendButton.addListener(SWT.Selection, e -> sendMessage());

        cancelButton = new Button(inputRow, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.setVisible(false);

        addBubble(ChatBubble.BubbleType.AI, "Welcome to DeepSeek Assistant!");
    }

    /**
     * Requests focus for the input field when the view becomes active.
     */
    @Override
    public void setFocus() {
        if (inputText != null && !inputText.isDisposed()) {
            inputText.setFocus();
        }
    }

    /**
     * Adds a new chat bubble to the conversation history.
     *
     * @param type the bubble type (USER or AI)
     * @param msg  the message content
     */
    private void addBubble(ChatBubble.BubbleType type, String msg) {
        ChatBubble bubble;
        if (type == ChatBubble.BubbleType.USER) {
            bubble = new ChatBubbleUser(messageContainer, "User", msg);
        } else {
            bubble = new ChatBubbleIA(messageContainer, "DeepSeek", msg);
        }
        GridData gd = new GridData(SWT.BEGINNING, SWT.TOP, false, false);
        if (type == ChatBubble.BubbleType.USER) {
            gd.horizontalAlignment = SWT.END;
        } else {
            gd.horizontalAlignment = SWT.BEGINNING;
        }
        // Try to size the bubble to its preferred width so alignment looks like a chat
        Point preferred = bubble.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        gd.widthHint = Math.max(200, preferred.x);
        bubble.setLayoutData(gd);
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
        if (children.length == 0) {
            return;
        }

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
     * This method creates a new thread for the API request,
     * manages UI updates, and handles success and error responses.
     */
    private void sendMessage() {
        String question = inputText.getText().trim();
        if (question.isEmpty() || isProcessing) {
            return;
        }

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
     * This method interrupts the worker thread, cancels the
     * API client request, and updates the UI accordingly.
     */
    private void cancelRequest() {
        if (!isProcessing) {
            return;
        }

        if (apiThread != null) {
            apiThread.interrupt();
        }
        
        if (apiClient != null) {
            apiClient.cancelRequest();
        }

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
}
