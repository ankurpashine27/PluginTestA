package org.anoncraft.plugintesta.NPCInteraction;

import org.anoncraft.plugintesta.Teleport.TeleportFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.Optional;

public class NPCListener implements Listener {

    private final Plugin plugin;
    private final String targetServer;
    private final String mannequinTag;
    private final double lookDistance;
    private final double degreesPerTick;

    public NPCListener(Plugin plugin, String targetServer, String mannequinTag, double lookDistance, double degreesPerTick) {
        this.plugin = plugin;
        this.targetServer = targetServer;
        this.mannequinTag = mannequinTag;
        this.lookDistance = lookDistance;
        this.degreesPerTick = degreesPerTick;

        // rotation loop — 1 tick interval
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateMannequinsTick();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // protect mannequins
    @EventHandler
    public void onMannequinDamage(EntityDamageEvent event) {
        Entity e = event.getEntity();

        // correct detection using registry key
        if (!e.getType().key().equals(NamespacedKey.minecraft("mannequin"))) return;
        if (!e.getScoreboardTags().contains(mannequinTag)) return;

        // cancel all damage
        event.setCancelled(true);
    }

    // right-click handler
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (clicked == null) return;

        // detect mannequin with registry key — FIXED
        if (!clicked.getType().key().equals(NamespacedKey.minecraft("mannequin"))) return;

        if (!clicked.getScoreboardTags().contains(mannequinTag)) return;

        Player player = event.getPlayer();

        // prevent double triggers
        event.setCancelled(true);

        // transfer to server
        TeleportFunctions.sendPlayerToServer(plugin, player, targetServer);
        player.sendMessage("§aTransferring you to §e" + targetServer + "§a...");
    }

    // rotation loop
    private void rotateMannequinsTick() {
        for (World world : Bukkit.getWorlds()) {

            for (Entity ent : world.getEntities()) {

                if (!ent.getType().key().equals(NamespacedKey.minecraft("mannequin"))) continue;
                if (!ent.getScoreboardTags().contains(mannequinTag)) continue;

                Optional<Player> nearest = world.getPlayers().stream()
                        .filter(p -> p.getLocation().distanceSquared(ent.getLocation()) <= (lookDistance * lookDistance))
                        .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(ent.getLocation())));

                nearest.ifPresent(player -> smoothFaceEntity(ent, player));
            }
        }
    }

    private void smoothFaceEntity(Entity mannequin, Player player) {
        Location mLoc = mannequin.getLocation();
        Location pLoc = player.getLocation();

        Location tmp = mLoc.clone();
        tmp.setDirection(pLoc.toVector().subtract(mLoc.toVector()));

        float targetYaw = tmp.getYaw();
        float targetPitch = tmp.getPitch();

        float newYaw = rotateTowards(mLoc.getYaw(), targetYaw, (float) degreesPerTick);
        float newPitch = rotateTowards(mLoc.getPitch(), targetPitch, (float) degreesPerTick);

        Location newLoc = mLoc.clone();
        newLoc.setYaw(newYaw);
        newLoc.setPitch(newPitch);

        mannequin.teleport(newLoc);
    }

    private float rotateTowards(float current, float target, float maxDelta) {
        float delta = wrapAngle(target - current);
        if (Math.abs(delta) <= maxDelta) return target;
        return current + Math.signum(delta) * maxDelta;
    }

    private float wrapAngle(float angle) {
        angle %= 360.0f;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }


}
