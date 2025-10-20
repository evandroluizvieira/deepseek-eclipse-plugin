package com.deepseek.plugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for configuring DeepSeek plugin settings.
 * Allows users to set their API key and other configuration options.
 */
public class DeepSeekPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    public static final String PREFERENCE_API_KEY = "DEEPSEEK_API_KEY";
    
    /**
     * Constructs the preferences page with grid layout.
     */
    public DeepSeekPreferencesPage() {
        super(GRID);
        setPreferenceStore(com.deepseek.plugin.Activator.getDefault().getPreferenceStore());
        setDescription("DeepSeek API Configuration");
    }
    
    /**
     * Creates the field editors for this preference page.
     */
    @Override
    protected void createFieldEditors() {
        StringFieldEditor apiKeyField = new StringFieldEditor(
            PREFERENCE_API_KEY, 
            "DeepSeek API Key:", 
            getFieldEditorParent()
        );
        apiKeyField.getTextControl(getFieldEditorParent()).setEchoChar('*');
        addField(apiKeyField);
        
        Label informationLabel = new Label(getFieldEditorParent(), SWT.NONE);
        informationLabel.setText("Obtain your API key from: https://platform.deepseek.com/api_keys");
    }
    
    /**
     * Initializes this preference page for the given workbench.
     *
     * @param workbench the workbench
     */
    @Override
    public void init(IWorkbench workbench) {
        
    }
}