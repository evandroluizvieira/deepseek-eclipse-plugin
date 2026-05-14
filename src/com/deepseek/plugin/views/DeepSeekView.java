package com.deepseek.plugin.views;

import java.util.function.BiFunction;

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
 *
 * Provides a conversational interface for interacting with the DeepSeek AI API.
 * The view consists of a scrollable chat history area built with custom
 * {@link ChatBubble} components, an input field for user messages, and action
 * buttons to send requests or cancel in-progress operations.
 *
 * The view maintains a {@link RequestState} to control UI availability
 * and prevent concurrent API requests. Users can only send a new message
 * when the state is IDLE, and can cancel when the state is PROCESSING.
 *
 * @see ChatBubble
 * @see DeepSeekAPIClient
 * @see ConfigurationManager
 */
public class DeepSeekView extends ViewPart {

    /**
     * The unique Eclipse view identifier for this plugin view.
     */
    public static final String ID = "com.deepseek.plugin.views.DeepSeekView";

    /**
     * Represents the possible states of an API request lifecycle.
     */
    private enum RequestState {
        /** No request in progress; ready to accept new input. */
        IDLE,
        /** Request has been sent and is awaiting a response; can be cancelled. */
        PROCESSING,
        /** Response has been successfully received and displayed. */
        COMPLETED,
        /** Request was cancelled by the user before completion. */
        CANCELLED,
        /** An error occurred during the request or response processing. */
        ERROR
    }

    /** Input field for user messages. */
    private Text inputText;

    /** Button to send the current input to the API. */
    private Button sendButton;

    /** Button to cancel an in-progress API request. */
    private Button cancelButton;

    /** Current state of the API request lifecycle. */
    private RequestState requestState = RequestState.IDLE;

    /** Background thread for executing API requests. */
    private Thread apiThread;

    /** Client instance for communicating with the DeepSeek API. */
    private DeepSeekAPIClient apiClient;

    /** Container that holds all chat bubble widgets. */
    private Composite messageContainer;

    /** Scrollable wrapper for the message container. */
    private ScrolledComposite scroller;

    /**
     * Creates the complete UI structure for the DeepSeek view.
     *
     * Sets up three main areas: header with title and settings button,
     * scrollable chat area for message bubbles, and footer with input field
     * and action buttons. An initial welcome message is scheduled to appear
     * after the layout is fully initialized.
     *
     * @param parent the parent composite provided by the Eclipse workbench
     */
    @Override
    public void createPartControl(Composite parent) {
        /*
         * Lambda factory for creating consistently styled buttons.
         *
         * Parameters:
         * parentArg - the parent Composite to contain the button
         * text - the button label text
         *
         * Returns:
         * A Button with fixed width (70px), right-aligned, centered vertically,
         * configured with SWT.PUSH style.
         */
        BiFunction<Composite, String, Button> newButton = (parentArg, text) -> {
            final int BUTTON_WIDTH_HINT = 70;
            Button button = new Button(parentArg, SWT.PUSH);
            button.setText(text);
            GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
            gd.widthHint = BUTTON_WIDTH_HINT;
            button.setLayoutData(gd);
            return button;
        };

        final int MAIN_MARGIN_WIDTH = 10;
        final int MAIN_MARGIN_HEIGHT = 10;
        final int MAIN_SPACING_VERTICAL = 10;
        final int INPUT_HORIZONTAL_SPACING = 5;
        final int HEADER_COLUMNS = 2;
        final int CHAT_VIEW_COLUMNS = 1;
        final int FOOTER_COLUMNS = 3;

        // Main container
        Composite main = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginWidth = MAIN_MARGIN_WIDTH;
        mainLayout.marginHeight = MAIN_MARGIN_HEIGHT;
        mainLayout.verticalSpacing = MAIN_SPACING_VERTICAL;
        main.setLayout(mainLayout);

        // Header area
        Composite header = new Composite(main, SWT.NONE);
        header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        GridLayout headerLayout = new GridLayout(HEADER_COLUMNS, false);
        headerLayout.marginWidth = 0;
        headerLayout.marginHeight = 0;
        headerLayout.marginTop = 0;
        headerLayout.marginBottom = 0;
        headerLayout.marginLeft = 0;
        headerLayout.marginRight = 0;
        headerLayout.verticalSpacing = 0;
        headerLayout.horizontalSpacing = 0;
        header.setLayout(headerLayout);

        Label title = new Label(header, SWT.NONE);
        title.setText("DeepSeek Assistant");
        title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button settingsButton = newButton.apply(header, "Settings");
        settingsButton.addListener(SWT.Selection, e -> {
            PreferencesUtil.createPreferenceDialogOn(
                Display.getDefault().getActiveShell(),
                "com.deepseek.plugin.preferences.DeepSeekPreferencesPage",
                null,
                null
            ).open();
        });

        // Chat area
        scroller = new ScrolledComposite(main, SWT.V_SCROLL | SWT.BORDER);
        scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scroller.setExpandVertical(true);
        scroller.setExpandHorizontal(true);
        scroller.addListener(SWT.Resize, e -> refreshChatLayout());

        GridLayout chatViewLayout = new GridLayout(CHAT_VIEW_COLUMNS, false);
        chatViewLayout.marginWidth = 0;
        chatViewLayout.marginHeight = 0;
        chatViewLayout.marginTop = 5;
        chatViewLayout.marginBottom = 5;
        chatViewLayout.marginLeft = 5;
        chatViewLayout.marginRight = 5;
        chatViewLayout.verticalSpacing = 5;
        chatViewLayout.horizontalSpacing = 0;

        messageContainer = new Composite(scroller, SWT.NONE);
        messageContainer.setLayout(chatViewLayout);
        messageContainer.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        scroller.setContent(messageContainer);
        scroller.setMinSize(messageContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // Footer area
        Composite footer = new Composite(main, SWT.NONE);
        footer.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        GridLayout inputLayout = new GridLayout(FOOTER_COLUMNS, false);
        inputLayout.marginWidth = 0;
        inputLayout.marginHeight = 0;
        inputLayout.marginTop = 0;
        inputLayout.marginBottom = 0;
        inputLayout.marginLeft = 0;
        inputLayout.marginRight = 0;
        inputLayout.verticalSpacing = 0;
        inputLayout.horizontalSpacing = INPUT_HORIZONTAL_SPACING;
        footer.setLayout(inputLayout);

        inputText = new Text(footer, SWT.BORDER | SWT.SINGLE);
        inputText.setMessage("Type your question...");
        inputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        inputText.addListener(SWT.DefaultSelection, e -> {
            if (requestState == RequestState.IDLE) {
                sendMessage();
            }
        });

        sendButton = newButton.apply(footer, "Send");
        sendButton.addListener(SWT.Selection, e -> {
            if (requestState == RequestState.IDLE) {
                sendMessage();
            }
        });

        cancelButton = newButton.apply(footer, "Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addListener(SWT.Selection, e -> {
            if (requestState == RequestState.PROCESSING) {
                cancelRequest();
            }
        });

        // Schedule welcome message after layout initialization
        parent.getDisplay().asyncExec(() -> {
            if (!messageContainer.isDisposed() && !scroller.isDisposed()) {
                refreshChatLayout();
                addBubble(ChatBubble.BubbleType.AI, "Welcome to DeepSeek Assistant!");
            }
        });
    }

    /**
     * Refreshes the chat layout by recalculating bubble widths and scroll area.
     *
     * Safe to call from any thread; execution is deferred to the UI thread.
     * Forces all parent layouts to update, resizes all chat bubbles to fit
     * the available width, updates the scroller minimum size, and scrolls
     * to the bottom to show the latest messages.
     */
    private void refreshChatLayout() {
        if (scroller == null || scroller.isDisposed()
                || messageContainer == null || messageContainer.isDisposed()) {
            return;
        }
        Display.getDefault().asyncExec(() -> doRefreshChatLayout());
    }

    /**
     * Internal method that performs the actual layout refresh calculations.
     * Must be called from the UI thread. Use {@link #refreshChatLayout()}
     * for thread-safe access from background threads.
     */
    private void doRefreshChatLayout() {
        if (scroller.isDisposed() || messageContainer.isDisposed()) {
            return;
        }

        scroller.getParent().layout(true, true);
        scroller.layout(true, true);
        messageContainer.layout(true, true);

        int width = scroller.getClientArea().width;
        if (width > 0) {
            GridLayout mcLayout = (GridLayout) messageContainer.getLayout();
            int margins = mcLayout.marginLeft + mcLayout.marginRight + mcLayout.marginWidth * 2;
            int newBubbleWidth = width - margins;

            for (Control child : messageContainer.getChildren()) {
                if (child instanceof ChatBubble) {
                    GridData gd = (GridData) child.getLayoutData();
                    if (gd != null) {
                        gd.widthHint = newBubbleWidth;
                    }
                    child.setSize(newBubbleWidth, child.getSize().y);
                    ((ChatBubble) child).adjustBubbleToTextContent();
                }
            }
        }

        Point size = messageContainer.computeSize(
                scroller.getClientArea().width, SWT.DEFAULT);
        scroller.setMinSize(size);
        scrollToBottom();
    }

    /**
     * Updates UI control states based on the current RequestState.
     *
     * State behavior:
     * IDLE - Input and Send enabled, Cancel disabled.
     * PROCESSING - Input and Send disabled, Cancel enabled.
     * COMPLETED, CANCELLED, ERROR - Reset to IDLE state and focus input field.
     */
    private void updateUIState() {
        Display.getDefault().asyncExec(() -> {
            switch (requestState) {
                case IDLE:
                    inputText.setEditable(true);
                    inputText.setMessage("Type your question...");
                    sendButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    break;
                case PROCESSING:
                    inputText.setEditable(false);
                    inputText.setMessage("Waiting for response...");
                    sendButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    break;
                case COMPLETED:
                case CANCELLED:
                case ERROR:
                    inputText.setEditable(true);
                    inputText.setMessage("Type your question...");
                    sendButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    setRequestState(RequestState.IDLE);
                    inputText.setFocus();
                    break;
            }
        });
    }

    /**
     * Updates the current request state and refreshes the UI accordingly.
     *
     * @param newState the new request state to transition to
     */
    private void setRequestState(RequestState newState) {
        this.requestState = newState;
        updateUIState();
    }

    /**
     * Adds a new chat bubble to the conversation history.
     *
     * The bubble is automatically sized based on the available width of the
     * scrollable container. User messages use ChatBubbleUser styling,
     * while AI responses use ChatBubbleIA styling. The chat layout is
     * refreshed before and after the bubble is added.
     *
     * @param type the bubble type indicating the message source (USER or AI)
     * @param msg the message content to display
     * @return the created ChatBubble instance, or null if the container width
     *         is not yet available (bubble will be rescheduled asynchronously)
     */
    private ChatBubble addBubble(ChatBubble.BubbleType type, String msg) {
        ChatBubble bubble;
        if (type == ChatBubble.BubbleType.USER) {
            bubble = new ChatBubbleUser(messageContainer, "User", msg);
        } else {
            bubble = new ChatBubbleIA(messageContainer, "DeepSeek", msg);
        }

        refreshChatLayout();

        int availableWidth = scroller.getClientArea().width;
        if (availableWidth <= 0) {
            scroller.getDisplay().asyncExec(() -> addBubble(type, msg));
            return null;
        }

        GridLayout mcLayout = (GridLayout) messageContainer.getLayout();
        int horizontalMargins = mcLayout.marginLeft + mcLayout.marginRight + mcLayout.marginWidth * 2;
        int bubbleWidth = availableWidth - horizontalMargins;

        GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
        gd.widthHint = bubbleWidth;
        gd.heightHint = SWT.DEFAULT;
        bubble.setLayoutData(gd);
        bubble.setSize(bubbleWidth, 10);
        bubble.adjustBubbleToTextContent();

        // Update heightHint with actual size after adjustment
        gd.heightHint = bubble.getSize().y;

        // Force container to reposition all children
        messageContainer.layout(true, true);
        refreshChatLayout();

        return bubble;
    }

    /**
     * Requests focus for the input field when this view becomes active.
     * Ensures the user can immediately start typing when the view is selected.
     */
    @Override
    public void setFocus() {
        if (inputText != null && !inputText.isDisposed()) {
            inputText.setFocus();
        }
    }

    /**
     * Sends the current user input to the DeepSeek API.
     *
     * Performs the following operations in order:
     * Validates input is not empty and state is IDLE.
     * Verifies API key is configured.
     * Clears the input field and displays the user's message as a bubble.
     * Creates a placeholder response bubble with "Thinking..." text.
     * Transitions state to PROCESSING and starts a background thread.
     * Updates the response bubble with the API result or error message.
     * Transitions state to COMPLETED or ERROR when finished.
     */
    private void sendMessage() {
        String question = inputText.getText().trim();
        if (question.isEmpty() || requestState != RequestState.IDLE) {
            return;
        }

        if (!ConfigurationManager.hasApiKey()) {
            addBubble(ChatBubble.BubbleType.AI,
                    "Error: Please configure your API key.\nWindow → Preferences → DeepSeek Plugin");
            return;
        }

        inputText.setText("");

        // 1. Bubble with the user's question
        addBubble(ChatBubble.BubbleType.USER, question);

        // 2. Temporary "Thinking..." bubble
        ChatBubble thinkingBubble = addBubble(ChatBubble.BubbleType.AI, "Thinking...");

        setRequestState(RequestState.PROCESSING);

        apiThread = new Thread(() -> {
            try {
                apiClient = new DeepSeekAPIClient(ConfigurationManager.getApiKey());
                String result = apiClient.sendMessage(question);

                if (requestState == RequestState.CANCELLED) {
                    return;
                }

                if (!apiThread.isInterrupted() && requestState != RequestState.CANCELLED) {
                    Display.getDefault().asyncExec(() -> {
                        if (requestState != RequestState.CANCELLED) {
                            // 3. Remove "Thinking..." bubble and add complete response
                            thinkingBubble.dispose();
                            addBubble(ChatBubble.BubbleType.AI, result);
                            setRequestState(RequestState.COMPLETED);
                        }
                    });
                }
            } catch (Exception ex) {
                if (!apiThread.isInterrupted() && requestState != RequestState.CANCELLED) {
                    Display.getDefault().asyncExec(() -> {
                        // Remove "Thinking..." bubble and show error
                        if (thinkingBubble != null && !thinkingBubble.isDisposed()) {
                            thinkingBubble.dispose();
                        }
                        addBubble(ChatBubble.BubbleType.AI, "Error: " + ex.getMessage());
                        setRequestState(RequestState.ERROR);
                    });
                }
            }
        });

        apiThread.start();
    }

    /**
     * Cancels the active API request if one is in progress.
     *
     * Interrupts the background thread, cancels the API client request,
     * updates the response bubble with a cancellation message, and
     * transitions state to CANCELLED which will reset to IDLE.
     */
    private void cancelRequest() {
        if (requestState == RequestState.PROCESSING) {
            setRequestState(RequestState.CANCELLED);

            if (apiClient != null) {
                apiClient.cancelRequest();
            }
            if (apiThread != null && apiThread.isAlive()) {
                apiThread.interrupt();
            }

            addBubble(ChatBubble.BubbleType.AI, "Request cancelled by user.");
        }
    }

    /**
     * Scrolls the chat view to the bottom of the conversation history.
     * Ensures the most recent messages are visible to the user.
     */
    private void scrollToBottom() {
        if (scroller != null && !scroller.isDisposed()) {
            scroller.getVerticalBar().setSelection(
                    scroller.getVerticalBar().getMaximum());
        }
    }
}