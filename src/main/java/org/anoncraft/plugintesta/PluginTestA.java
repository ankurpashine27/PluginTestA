package org.anoncraft.plugintesta;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginTestA extends JavaPlugin {

    private static PluginTestA instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("~~ PluginTestA has been enabled!");
        //So Server starts Listening to Events here
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        instance = this;
        // default config (server name & tag)
        saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        String targetServer = cfg.getString("transfer.target-server", "survival");
        String mannequinTag = cfg.getString("mannequin.tag", "survival_npc");
        double lookDistance = cfg.getDouble("mannequin.look-distance", 3.0);
        double degreesPerTick = cfg.getDouble("mannequin.degrees-per-tick", 10.0);

        // register outgoing plugin channel (BungeeCord channel - works for Velocity)
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // register listener and pass configuration
        getServer().getPluginManager().registerEvents(
                new NPCListener(this, targetServer, mannequinTag, lookDistance, degreesPerTick),
                this
        );

        //Command to remove mannequin NPCs
        getCommand("removenpc").setExecutor(new CustomCommands());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        instance = null;
        getLogger().info("~~ PluginTestA has been disabled!");
    }

    public static PluginTestA getInstance() {
        return instance;
    }
}
