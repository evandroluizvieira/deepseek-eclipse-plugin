package com.deepseek.plugin.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * Chat bubble specialized for AI messages.
 * Uses neutral very light gray background with black text.
 */
public class ChatBubbleIA extends ChatBubble {

    /**
     * Creates a new AI chat bubble.
     *
     * @param parent  the parent composite
     * @param sender  the message sender label
     * @param message the message text
     */
    public ChatBubbleIA(Composite parent, String sender, String message) {
        super(parent, ChatBubble.BubbleType.AI, sender, message);
    }

    @Override
    protected RGB getBubbleColorRGB() {
        // IA: neutral very light gray
        return new RGB(245, 245, 245); // #F5F5F5
    }

    @Override
    protected int getTextColor() {
        // Black text on light gray
        return SWT.COLOR_BLACK;
    }
}
