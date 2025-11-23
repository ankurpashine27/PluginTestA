package org.anoncraft.plugintesta.NPCInteraction;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NPCCustomCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "removenpc":
                return handleRemoveNPC(player);
            default:
                return false;
        }
    }

    private boolean handleRemoveNPC(Player player) {

        // Find the nearest mannequin within 1 block radius
        Entity target = player.getWorld().getNearbyEntities(player.getLocation(), 1, 1, 1)
                .stream()
                .filter(e -> e.getType().key().equals(NamespacedKey.minecraft("mannequin")))
                .filter(e -> e.getScoreboardTags().contains("survival_npc"))
                .findFirst()
                .orElse(null);

        if (target == null) {
            player.sendMessage("§cNo mannequin NPC within 1 block.");
            return true;
        }

        target.remove();
        player.sendMessage("§aMannequin removed successfully!");
        return true;
    }
}
