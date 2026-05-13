package com.deepseek.plugin.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * Chat bubble specialized for user messages.
 * Uses neutral medium-dark gray background with white text.
 */
public class ChatBubbleUser extends ChatBubble {

    /**
     * Creates a new user chat bubble.
     *
     * @param parent  the parent composite
     * @param sender  the message sender label
     * @param message the message text
     */
    public ChatBubbleUser(Composite parent, String sender, String message) {
        super(parent, ChatBubble.BubbleType.USER, sender, message);
    }

    @Override
    protected RGB getBubbleColorRGB() {
        // User: neutral medium-dark gray for contrast
        return new RGB(82, 85, 90); // #52555A
    }

    @Override
    protected int getTextColor() {
        // White text on dark gray
        return SWT.COLOR_WHITE;
    }
}
