package org.anoncraft.plugintesta;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
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

        // Start repeating task to rotate mannequins every tick (1L). You may change to 2L or 3L to reduce load.
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateMannequinsTick();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // Make mannequins invulnerable
    @EventHandler
    public void onMannequinDamage(EntityDamageEvent event) {
        Entity e = event.getEntity();

        if (!e.getType().key().equals(NamespacedKey.minecraft("mannequin"))) return;
        if (!e.getScoreboardTags().contains("survival_npc")) return;

        event.setCancelled(true);
    }

    // right-click handler
    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (clicked == null) return;
        if (clicked.getType() != EntityType.valueOf("MANNEQUIN")) return;

        if (!clicked.getScoreboardTags().contains(mannequinTag)) return;

        Player player = event.getPlayer();

        // transfer player to target server using BungeeCord plugin messaging 'Connect' subchannel
        sendPlayerToServer(player, targetServer);
        // optionally send feedback
        player.sendMessage("§aTransferring you to §e" + targetServer + "§a...");
        event.setCancelled(true);
    }

    private void rotateMannequinsTick() {
        for (World world : Bukkit.getWorlds()) {
            List<Entity> entities = world.getEntities();
            for (Entity ent : entities) {
                if (ent.getType() != EntityType.valueOf("MANNEQUIN")) continue;
                if (!ent.getScoreboardTags().contains(mannequinTag)) continue;

                // find nearest player within lookDistance
                Optional<Player> nearest = world.getPlayers().stream()
                        .filter(p -> p.getLocation().distanceSquared(ent.getLocation()) <= (lookDistance * lookDistance))
                        .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(ent.getLocation())));

                if (nearest.isPresent()) {
                    Player p = nearest.get();
                    smoothFaceEntity(ent, p);
                }
                // else: you could set mannequin back to default rotation if you'd like
            }
        }
    }

    private void smoothFaceEntity(Entity mannequin, Player player) {
        Location mLoc = mannequin.getLocation();
        Location pLoc = player.getLocation();

        // compute direction vector and setDirection to compute target yaw/pitch
        Location tmp = mLoc.clone();
        tmp.setDirection(pLoc.toVector().subtract(mLoc.toVector()));
        float targetYaw = tmp.getYaw();
        float targetPitch = tmp.getPitch();

        float currentYaw = mLoc.getYaw();
        float currentPitch = mLoc.getPitch();

        float newYaw = rotateTowards(currentYaw, targetYaw, (float) degreesPerTick);
        float newPitch = rotateTowards(currentPitch, targetPitch, (float) degreesPerTick);

        Location newLoc = mLoc.clone();
        newLoc.setYaw(newYaw);
        newLoc.setPitch(newPitch);

        // teleport mannequin to same position but with new yaw/pitch
        // teleporting only rotation is fine here
        mannequin.teleport(newLoc);
    }

    // rotate current angle towards target by maxDelta degrees, handling angle wrap
    private float rotateTowards(float current, float target, float maxDelta) {
        float delta = wrapAngle(target - current);
        if (Math.abs(delta) <= maxDelta) return target;
        if (delta > 0) return current + maxDelta;
        return current - maxDelta;
    }

    // wrap angle to [-180,180)
    private float wrapAngle(float a) {
        a %= 360.0f;
        if (a >= 180.0f) a -= 360.0f;
        if (a < -180.0f) a += 360.0f;
        return a;
    }

    // Send player to another server via BungeeCord plugin messaging 'Connect' subchannel
    private void sendPlayerToServer(Player player, String server) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bos);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(plugin, "BungeeCord", bos.toByteArray());
            out.close();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to send plugin message for transfer: " + e.getMessage());
        }
    }
}
