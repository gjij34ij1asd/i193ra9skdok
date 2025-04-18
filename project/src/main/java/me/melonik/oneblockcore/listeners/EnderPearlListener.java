package me.melonik.oneblockcore.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class EnderPearlListener implements Listener {

    @EventHandler
    public void onPearlHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;

        if (event.getHitBlock() != null && event.getHitBlock().getType() == Material.BARRIER) {
            event.getEntity().remove();
            event.setCancelled(true);
        }
    }
}