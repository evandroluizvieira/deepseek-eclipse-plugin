package com.deepseek.plugin.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

public class DeepSeekView extends ViewPart {
    @Override
    public void createPartControl(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("DeepSeek Assistant Working!");
    }
    
    @Override
    public void setFocus() {
    	
    }
}