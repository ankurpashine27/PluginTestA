package org.anoncraft.plugintesta;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // Handle the event here
        event.setJoinMessage("Welcome " + event.getPlayer().getName() + " to the server!");

        Firework firework = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation(), Firework.class);
        // You can customize the firework's effects here if desired
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.RED)           // Firework color
                .withFade(Color.GREEN)         // Fade color
                .with(FireworkEffect.Type.CREEPER) // Firework type
                .withTrail()                    // Add particle trail
                .withFlicker()                  // Flicker effect
                .build());

        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }

    @EventHandler
    public void onPlayerEntityInteractEvent(PlayerInteractEntityEvent event) {
        // Handle the event here

        if (event.getRightClicked() instanceof org.bukkit.entity.Cow) {
            Cow cow = (Cow) event.getRightClicked();
            cow.getWorld().createExplosion(cow.getLocation(), 2.5f);
        }
    }
}
