package org.anoncraft.plugintesta;

import org.bukkit.plugin.java.JavaPlugin;

public final class PluginTestA extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("~~ PluginTestA has been enabled!");
        //So Server starts Listening to Events here
        getServer().getPluginManager().registerEvents(new EventListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("~~ PluginTestA has been disabled!");
    }
}
