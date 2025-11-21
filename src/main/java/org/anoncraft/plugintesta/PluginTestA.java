package org.anoncraft.plugintesta;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginTestA extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("~~ PluginTestA has been enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("~~ PluginTestA has been disabled!");
    }
}
