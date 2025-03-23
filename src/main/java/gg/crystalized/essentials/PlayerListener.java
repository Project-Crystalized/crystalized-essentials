package gg.crystalized.essentials;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerListener implements Listener {
    @EventHandler
    public void onInventoryMove(InventoryClickEvent e) {
        if (e.getCurrentItem().equals(crystalized_essentials.getInstance().WingedOrbElytra)) {
            e.setCancelled(true);
        }
    }
}
