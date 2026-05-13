package com.deepseek.plugin;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The main activator class for the DeepSeek Eclipse Plugin.
 * Controls the plugin lifecycle and provides access to plugin-wide services
 * and preferences storage.
 * 
 * <p>This activator manages the plugin's startup and shutdown sequences,
 * and serves as the central access point for plugin-specific functionality
 * and configuration storage.</p>
 */
public class Activator implements BundleActivator {
    
    /**
     * The shared plugin instance.
     */
    private static Activator plugin;
    
    /**
     * The preference store for plugin configuration.
     */
    private IPreferenceStore preferenceStore;
    
    /**
     * Starts this plugin and initializes its core services.
     * 
     * <p>This method is called when the plugin is activated by the OSGi framework.
     * It initializes the preference store and sets up the plugin instance
     * for global access.</p>
     *
     * @param context the bundle context provided by the OSGi framework
     * @throws Exception if plugin initialization fails
     */
    @Override
    public void start(BundleContext context) throws Exception {
        plugin = this;
        preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.deepseek.plugin");
    }
    
    /**
     * Stops this plugin and releases any allocated resources.
     * 
     * <p>This method is called when the plugin is being stopped by the OSGi framework.
     * It performs cleanup operations and nullifies the plugin instance reference.</p>
     *
     * @param context the bundle context provided by the OSGi framework
     * @throws Exception if plugin shutdown fails
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
    }
    
    /**
     * Returns the shared plugin instance.
     * 
     * <p>This method provides global access to the plugin activator instance,
     * allowing other components to access plugin services and configuration.</p>
     *
     * @return the shared plugin instance, or null if the plugin is not active
     */
    public static Activator getDefault() {
        return plugin;
    }
    
    /**
     * Returns the preference store for this plugin.
     * 
     * <p>The preference store is used to persist plugin configuration settings
     * such as API keys and user preferences across Eclipse sessions.</p>
     *
     * @return the plugin's preference store instance
     */
    public IPreferenceStore getPreferenceStore() {
        return preferenceStore;
    }
}