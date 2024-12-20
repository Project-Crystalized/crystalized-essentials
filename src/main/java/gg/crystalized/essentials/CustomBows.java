package gg.crystalized.essentials;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomBows implements Listener {
    @EventHandler
    public void onBowShot(EntityShootBowEvent event){
        if(event.isCancelled()){
            return;
        }
        Entity e = event.getEntity();
        ItemStack stack = event.getBow();

        if(stack == null){
            return;
        }

        ItemMeta meta = stack.getItemMeta();

        if(meta == null){
            return;
        }

    }
}
