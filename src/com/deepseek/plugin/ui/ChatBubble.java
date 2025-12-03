package com.deepseek.plugin.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * A custom chat bubble widget used to display user and AI messages.
 * Provides selectable, non-editable text and draws a rounded visual bubble
 * with padded content.
 * 
 * <p>This widget supports custom coloring based on the bubble type,
 * automatic sizing based on text content, and optional context-menu
 * operations for copy and text selection.</p>
 */
public class ChatBubble extends Composite {

    public enum BubbleType {
        USER, AI
    }

    private final BubbleType type;
    private final String message;
    private final String sender;
    private StyledText messageText;
    private Canvas bubbleCanvas;

    private Color bubbleColor;
    private Color selectionColor;
    private int padding = 15;

    /**
     * Creates a new chat bubble for displaying message content.
     * 
     * <p>The bubble is initialized with colors, layout, text components,
     * and optional context menu actions. It automatically adjusts its size
     * based on the text content.</p>
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
        initializeColors();
        createControls();

        addDisposeListener(e -> {
            if (bubbleColor != null && !bubbleColor.isDisposed()) {
                bubbleColor.dispose();
            }
            if (selectionColor != null && !selectionColor.isDisposed()) {
                selectionColor.dispose();
            }
        });
    }

    /**
     * Initializes the bubble and selection colors based on the bubble type.
     */
    private void initializeColors() {
        Display display = getDisplay();

        if (type == BubbleType.USER) {
            bubbleColor = new Color(display, 100, 170, 255);
        } else {
            bubbleColor = new Color(display, 100, 200, 100);
        }

        selectionColor = new Color(display, 220, 220, 220);
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
        bubbleCanvas.setBackground(bubbleColor);

        int style = SWT.WRAP | SWT.MULTI | SWT.READ_ONLY;
        messageText = new StyledText(bubbleCanvas, style) {
            @Override
            protected void checkSubclass() {}
        };
        messageText.setText(message);
        messageText.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));

        forceTransparency();

        messageText.setSelectionBackground(selectionColor);
        messageText.setSelectionForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        messageText.setCaret(null);
        messageText.setEditable(false);

        createContextMenu();

        bubbleCanvas.addPaintListener(e -> {
            drawBubble(e.gc, bubbleCanvas.getClientArea());
            positionText();
        });

        bubbleCanvas.addListener(SWT.Resize, e -> {
            positionText();
            bubbleCanvas.redraw();
        });

        adjustBubbleToTextContent();
    }

    /**
     * Attempts to apply transparency to the internal text widget.
     */
    private void forceTransparency() {
        try {
            messageText.setBackground(null);
        } catch (Exception e1) {
            try {
                messageText.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
            } catch (Exception e2) {
                try {
                    messageText.setBackground(bubbleColor);
                } catch (Exception e3) {
                    messageText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
                }
            }
        }
    }

    /**
     * Creates the context menu that allows copy and select-all operations.
     */
    private void createContextMenu() {
        Menu contextMenu = new Menu(messageText);

        MenuItem copyItem = new MenuItem(contextMenu, SWT.PUSH);
        copyItem.setText("Copy");
        copyItem.addListener(SWT.Selection, e -> {
            if (messageText.getSelectionText().length() > 0) {
                messageText.copy();
            }
        });

        MenuItem selectAllItem = new MenuItem(contextMenu, SWT.PUSH);
        selectAllItem.setText("Select All");
        selectAllItem.addListener(SWT.Selection, e -> {
            messageText.selectAll();
        });

        messageText.setMenu(contextMenu);
    }

    /**
     * Draws the rounded bubble background and outer border.
     *
     * @param gc   the graphics context used for drawing
     * @param area the area of the canvas to draw in
     */
    private void drawBubble(GC gc, Rectangle area) {
        gc.setAntialias(SWT.ON);

        gc.setBackground(bubbleColor);
        gc.fillRoundRectangle(area.x, area.y, area.width, area.height, 20, 20);

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
        gc.setLineWidth(1);
        gc.drawRoundRectangle(area.x, area.y, area.width - 1, area.height - 1, 20, 20);
    }

    /**
     * Positions the text widget inside the bubble with consistent padding.
     */
    private void positionText() {
        Rectangle canvasArea = bubbleCanvas.getClientArea();

        int textX = padding;
        int textY = padding;
        int textWidth = canvasArea.width - (padding * 2);
        int textHeight = canvasArea.height - (padding * 2);

        if (textWidth < 10) textWidth = 10;
        if (textHeight < 10) textHeight = 10;

        messageText.setBounds(textX, textY, textWidth, textHeight);
        messageText.redraw();
    }

    /**
     * Adjusts the bubble size based on the actual content of the message.
     */
    public void adjustBubbleToTextContent() {
        int idealHeight = computeBubbleHeight();
        Point textSize = messageText.computeSize(SWT.DEFAULT, idealHeight);

        messageText.setSize(textSize.x, idealHeight);
        this.setSize(textSize.x + 20, idealHeight + 20);

        this.layout(true, true);
        if (getParent() != null) {
            getParent().layout(true, true);
        }
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
        if (messageText != null && !messageText.isDisposed()) {
            int maxWidth = (wHint != SWT.DEFAULT
                ? wHint
                : getParent().getClientArea().width - padding * 4);

            Point textPreferredSize =
                messageText.computeSize(maxWidth, SWT.DEFAULT, true);

            int width = textPreferredSize.x + (padding * 2);
            int height = textPreferredSize.y + (padding * 2);
            return new Point(width, height);
        }

        return new Point(400, 50);
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
        if (messageText != null && !messageText.isDisposed()) {
            messageText.setText(newMessage);
            adjustBubbleToTextContent();
        }
    }

    /**
     * Computes and returns the required height for the bubble
     * based on the current font metrics and padding.
     *
     * <p>The height is calculated using the font's line height
     * plus vertical padding applied to the bubble.</p>
     *
     * @return the calculated bubble height in pixels
     */
    private int computeBubbleHeight() {
        GC gc = new GC(messageText);
        FontMetrics fm = gc.getFontMetrics();
        int lineHeight = fm.getHeight();
        gc.dispose();

        return padding * 2 + lineHeight;
    }
}
