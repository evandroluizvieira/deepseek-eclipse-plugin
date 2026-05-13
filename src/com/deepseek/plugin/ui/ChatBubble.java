package com.deepseek.plugin.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * Base class for chat bubble widgets used to display user and AI messages.
 * Provides selectable, non-editable text and draws a rounded visual bubble
 * with padded content.
 *
 * This widget supports custom coloring based on the bubble type,
 * automatic sizing based on text content, and optional context-menu
 * operations for copy and text selection.
 */
public abstract class ChatBubble extends Composite {

    private final BubbleType type;         // The bubble type (USER or AI)
    private String message;                 // The message text
    private final String sender;           // The sender label
    private Canvas bubbleCanvas;           // The canvas for the bubble
    private Color bubbleColor;             // The bubble background color
    private Color selectionColor;          // The selection color
    private Color borderColor;             // The border color
    private int padding = 12;              // Padding for the bubble

    /**
     * Represents the type of chat bubble.
     * Can be USER for user messages or AI for assistant messages.
     */
    public enum BubbleType {
        USER,   // User message bubble
        AI      // AI message bubble
    }
    
    /**
     * Template method to get bubble background color.
     * Subclasses must implement this to define their specific color.
     */
    protected abstract RGB getBubbleColorRGB();
    
    /**
     * Template method to get text color.
     * Subclasses must implement this to define their specific text color.
     */
    protected abstract int getTextColor();

    /**
     * Creates a new chat bubble for displaying message content.
     *
     * The bubble is initialized with colors, layout, text components,
     * and optional context menu actions. It automatically adjusts its size
     * based on the text content.
     *
     * @param parent  the parent composite
     * @param type    the bubble type (USER or AI)
     * @param sender  the message sender label
     * @param message the message text
     */
    public ChatBubble(Composite parent, BubbleType type, String sender, String message) {
        super(parent, SWT.NONE);
        this.type = type;
        this.sender = sender;
        this.message = message;

        setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        createControls();

        addDisposeListener(e -> {
            if (bubbleColor != null && !bubbleColor.isDisposed()) {
                bubbleColor.dispose();
            }
            if (selectionColor != null && !selectionColor.isDisposed()) {
                selectionColor.dispose();
            }
            if (borderColor != null && !borderColor.isDisposed()) {
                borderColor.dispose();
            }
        });
    }

    /**
     * Computes the preferred bubble size used by SWT layout managers.
     *
     * @param wHint   width hint
     * @param hHint   height hint
     * @param changed whether the control has changed
     * @return the preferred size of this control
     */
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int maxWidth;
        if (wHint != SWT.DEFAULT) {
            maxWidth = wHint;
        } else {
            int parentWidth = getParent().getClientArea().width;
            if (parentWidth <= 0) parentWidth = 800;
            maxWidth = Math.max(200, (int) (parentWidth * 0.6)) - padding * 4;
        }

        TextLayout layout = new TextLayout(getDisplay());
        layout.setText(message != null ? message : "");
        layout.setWidth(maxWidth);
        org.eclipse.swt.graphics.Rectangle bounds = layout.getBounds();
        int width = bounds.width + padding * 2;
        int height = bounds.height + padding * 2;
        layout.dispose();
        return new Point(width, Math.max(32, height));
    }

    /**
     * Initializes the bubble and selection colors based on the bubble type.
     * Called lazily to avoid calling abstract methods in constructor.
     */
    private void initializeColors() {
        if (bubbleColor != null) return; // Already initialized
        
        Display display = getDisplay();

        // Use template method to get bubble color from subclass
        RGB colorRGB = getBubbleColorRGB();
        bubbleColor = new Color(display, colorRGB.red, colorRGB.green, colorRGB.blue);

        // Border: same as bubble fill for invisible border effect
        borderColor = new Color(display, colorRGB.red, colorRGB.green, colorRGB.blue);

        selectionColor = new Color(display, 200, 200, 200);
    }

    /**
     * Creates and configures the internal controls such as the bubble canvas
     * and the text component.
     */
    private void createControls() {
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);
        bubbleCanvas = new Canvas(this, SWT.DOUBLE_BUFFERED);
        GridData canvasData = new GridData(SWT.FILL, SWT.FILL, true, true);
        bubbleCanvas.setLayoutData(canvasData);
        if (getParent() != null && !getParent().isDisposed()) {
            bubbleCanvas.setBackground(getParent().getBackground());
        }

        bubbleCanvas.addPaintListener(e -> {
            drawBubble(e.gc, bubbleCanvas.getClientArea());
            drawText(e.gc, bubbleCanvas.getClientArea());
        });

        bubbleCanvas.addListener(SWT.Resize, e -> bubbleCanvas.redraw());

        adjustBubbleToTextContent();
    }
    /**
     * Draws the rounded bubble background and outer border.
     *
     * @param gc   the graphics context used for drawing
     * @param area the area of the canvas to draw in
     */
    private void drawBubble(GC gc, Rectangle area) {
        initializeColors(); // Ensure colors are initialized
        gc.setAntialias(SWT.ON);
        gc.setBackground(bubbleColor);
        // Only fill rounded rect (no outline) so the bubble blends with parent
        // background at the corners, creating an invisible border look.
        gc.fillRoundRectangle(area.x, area.y, area.width, area.height, 20, 20);
    }
    private void drawText(GC gc, Rectangle area) {
        initializeColors(); // Ensure colors are initialized
        if (message == null) return;
        TextLayout layout = new TextLayout(getDisplay());
        layout.setText(message);
        layout.setWidth(Math.max(10, area.width - padding * 2));
        // Use template method to get text color from subclass
        gc.setForeground(getDisplay().getSystemColor(getTextColor()));
        layout.draw(gc, area.x + padding, area.y + padding);
        layout.dispose();
    }

    private int computeBubbleHeight() {
        TextLayout layout = new TextLayout(getDisplay());
        layout.setText(message != null ? message : "");
        int parentWidth = getParent() != null ? getParent().getClientArea().width : 800;
        layout.setWidth(Math.max(10, Math.max(200, (int) (parentWidth * 0.6)) - padding * 4));
        int height = layout.getBounds().height;
        layout.dispose();
        return padding * 2 + height;
    }

    public void adjustBubbleToTextContent() {
        int parentWidth = getParent() != null ? getParent().getClientArea().width : 800;
        int maxTextWidth = Math.max(200, (int) (parentWidth * 0.6));
        TextLayout layout = new TextLayout(getDisplay());
        layout.setText(message != null ? message : "");
        layout.setWidth(maxTextWidth);
        org.eclipse.swt.graphics.Rectangle bounds = layout.getBounds();
        int width = Math.min(bounds.width, maxTextWidth);
        int height = bounds.height;
        layout.dispose();
        this.setSize(width + padding * 2, height + padding * 2);
        this.layout(true, true);
        if (getParent() != null) {
            getParent().layout(true, true);
        }
    }

    /**
     * Returns the bubble type.
     *
     * @return the bubble type
     */
    public BubbleType getBubbleType() {
        return type;
    }

    /**
     * Returns the message content.
     *
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the sender label.
     *
     * @return the sender name
     */
    public String getSender() {
        return sender;
    }

    /**
     * Updates the bubble's message text and re-sizes the bubble accordingly.
     *
     * @param newMessage the updated message content
     */
    public void updateMessage(String newMessage) {
        this.message = newMessage;
        adjustBubbleToTextContent();
        if (bubbleCanvas != null && !bubbleCanvas.isDisposed()) {
            bubbleCanvas.redraw();
        }
    }
}
