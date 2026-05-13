package com.deepseek.plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * Activator for DeepSeek Eclipse Plugin.
 * Manages plugin lifecycle and provides access to plugin-wide services and preferences.
 */
public class Activator implements BundleActivator {

    private static Activator plugin;           // Shared plugin instance
    private IPreferenceStore preferenceStore;  // Preference store for plugin configuration

    /**
     * Starts this plugin and initializes its core services.
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
     * @param context the bundle context provided by the OSGi framework
     * @throws Exception if plugin shutdown fails
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
    }

    /**
     * Gets the singleton instance of the plugin activator.
     * @return the shared plugin instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Gets the preference store used for plugin configuration.
     * @return the plugin's preference store instance
     */
    public IPreferenceStore getPreferenceStore() {
        return preferenceStore;
    }
}