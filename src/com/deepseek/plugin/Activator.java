package com.deepseek.plugin;
<<<<<<< HEAD
=======

>>>>>>> d2a98b5 (feat: complete DeepSeek plugin implementation)
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

<<<<<<< HEAD

/**
 * Activator for DeepSeek Eclipse Plugin.
 * Manages plugin lifecycle and provides access to plugin-wide services and preferences.
 */
public class Activator implements BundleActivator {

    private static Activator plugin;           // Shared plugin instance
    private IPreferenceStore preferenceStore;  // Preference store for plugin configuration

    /**
     * Starts this plugin and initializes its core services.
=======
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
>>>>>>> d2a98b5 (feat: complete DeepSeek plugin implementation)
     * @param context the bundle context provided by the OSGi framework
     * @throws Exception if plugin initialization fails
     */
    @Override
    public void start(BundleContext context) throws Exception {
        plugin = this;
        preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, "com.deepseek.plugin");
    }
<<<<<<< HEAD

    /**
     * Stops this plugin and releases any allocated resources.
=======
    
    /**
     * Stops this plugin and releases any allocated resources.
     * 
     * <p>This method is called when the plugin is being stopped by the OSGi framework.
     * It performs cleanup operations and nullifies the plugin instance reference.</p>
     *
>>>>>>> d2a98b5 (feat: complete DeepSeek plugin implementation)
     * @param context the bundle context provided by the OSGi framework
     * @throws Exception if plugin shutdown fails
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
    }
<<<<<<< HEAD

    /**
     * Gets the singleton instance of the plugin activator.
     * @return the shared plugin instance
=======
    
    /**
     * Returns the shared plugin instance.
     * 
     * <p>This method provides global access to the plugin activator instance,
     * allowing other components to access plugin services and configuration.</p>
     *
     * @return the shared plugin instance, or null if the plugin is not active
>>>>>>> d2a98b5 (feat: complete DeepSeek plugin implementation)
     */
    public static Activator getDefault() {
        return plugin;
    }
<<<<<<< HEAD

    /**
     * Gets the preference store used for plugin configuration.
=======
    
    /**
     * Returns the preference store for this plugin.
     * 
     * <p>The preference store is used to persist plugin configuration settings
     * such as API keys and user preferences across Eclipse sessions.</p>
     *
>>>>>>> d2a98b5 (feat: complete DeepSeek plugin implementation)
     * @return the plugin's preference store instance
     */
    public IPreferenceStore getPreferenceStore() {
        return preferenceStore;
    }
}