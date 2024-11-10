package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;


 // Had to make them 1 class because this didn't work with multiple classes checking for the same thing, so yea this files gonna be *long* when we actually implement these things lmao. sry for my shit code

 //TODO Implement all of this lmao
 //TODO When you use an item, take away one from the player
 //TODO add cooldowns. There currently is a bug where if you place a block and use an item at the same time, it activates twice at the same time

public class CustomCoalBasedItems implements Listener {

    @EventHandler
    //public void onRightClick(RIGHT_CLICK_AIR event) {
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType().equals(Material.COAL) && player.getEquipment().getItemInMainHand().getItemMeta().hasCustomModelData() == true) {

            // Boost Orb
            if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 1) {
                player.sendMessage(Component.text("Boost orb isn't currently implemented yet"));

                // Bridge Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 2) {
                player.sendMessage(Component.text("Bridge orb isn't currently implemented yet"));

                // Explosive Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 3) {
                player.sendMessage(Component.text("Explosive orb isn't currently implemented yet"));

                // Grappling Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 4) {
                player.sendMessage(Component.text("Grappling orb isn't currently implemented yet"));

                // Health Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 5) {
                player.sendMessage(Component.text("Health orb isn't currently implemented yet"));
                // It's not well known what Health Orbs did since they weren't in TubNet for very long

                // Knockout Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 6) {
                player.sendMessage(Component.text("Knockout orb isn't currently implemented yet"));

                // Poison Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 7) {
                player.sendMessage(Component.text("Poison orb isn't currently implemented yet"));
                //It's not well known what Poison Orbs did since they weren't in TubNet for very long

                // Winged Orb
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 8) {
                player.sendMessage(Component.text("Winged orb isn't currently implemented yet"));

                // Antiair Totem
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 9) {
                player.sendMessage(Component.text("Antiair Totem isn't currently implemented yet"));


                // Cloud Totem
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 10) {
                player.sendMessage(Component.text("Cloud Totem isn't currently implemented yet"));
                /*
                Cloud Totems acted differently in different games
                In Crystal Rush, the platform was made from glass (tinted glass?)
                In Knockout the platform was made out of Crystal Blocks that matched your team's/leather armor's colour
                We will need to somehow account for that by detecting what game is being played.
                 */

                // Defense Totem
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 11) {
                player.sendMessage(Component.text("Defence isn't currently implemented yet"));
                    // Healing Totem
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 12) {
                player.sendMessage(Component.text("Healing Totem isn't currently implemented yet"));
                    // Launch Totem
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 13) {
                player.sendMessage(Component.text("Launch Totem isn't currently implemented yet"));
                    // Slime Totem
            } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 14) {
                player.sendMessage(Component.text("Slime Totem isn't currently implemented yet"));
            }
        }
    }
}

