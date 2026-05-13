package com.deepseek.plugin.configuration;

import org.eclipse.jface.preference.IPreferenceStore;
import com.deepseek.plugin.Activator;
import com.deepseek.plugin.preferences.DeepSeekPreferencesPage;

/**
 * Manages configuration and preferences for the DeepSeek plugin.
 * Provides access to stored API keys and plugin settings.
 */
public class ConfigurationManager {
    
    /**
     * Retrieves the stored DeepSeek API key.
     *
     * @return the API key or empty string if not configured
     */
    public static String getApiKey() {
        IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
        return preferences.getString(DeepSeekPreferencesPage.PREFERENCE_API_KEY);
    }
    
    /**
     * Checks if an API key has been configured.
     *
     * @return true if a valid API key is stored
     */
    public static boolean hasApiKey() {
        String apiKey = getApiKey();
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * Saves the API key to plugin preferences.
     *
     * @param apiKey the API key to save
     */
    public static void saveApiKey(String apiKey) {
        IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
        preferences.setValue(DeepSeekPreferencesPage.PREFERENCE_API_KEY, apiKey);
    }
}