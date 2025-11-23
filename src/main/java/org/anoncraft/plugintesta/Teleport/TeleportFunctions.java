package org.anoncraft.plugintesta.Teleport;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TeleportFunctions {

    public static void sendPlayerToServer(Plugin plugin, Player player, String server) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);

            out.writeUTF("Connect");
            out.writeUTF(server);

            player.sendPluginMessage(plugin, "BungeeCord", bos.toByteArray());
            out.close();

        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send Velocity transfer message: " + e.getMessage());
        }
    }

}
