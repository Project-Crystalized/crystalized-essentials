package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

// Had to make them 1 class because this didn't work with multiple classes checking for the same thing, so yea this files gonna be *long* when we actually implement these things lmao. sry for my shit code

 //TODO Fix bug where PlayerInteractEvent fires twice if you have an item in your offhand

public class CustomCoalBasedItems implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        //TODO Check if player is right clicking here
        if (player.hasCooldown(Material.COAL)) {
            player.sendMessage(Component.text("This item is on cooldown!").color(NamedTextColor.RED));
        } else {
            if (player.getInventory().getItemInMainHand().getType().equals(Material.COAL) && player.getEquipment().getItemInMainHand().getItemMeta().hasCustomModelData()) {
                    // Boost Orb
                if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 1) {
                    player.sendMessage(Component.text("Boost orb isn't currently implemented yet")); //TODO

                    // Bridge Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 2) {
                    player.sendMessage(Component.text("Bridge orb isn't currently implemented yet")); //TODO

                    // Explosive Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 3) {
                    player.getPlayer().getWorld().spawnEntity(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY() + 0.8, player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()), EntityType.FIREBALL);
                    player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);

                    // Grappling Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 4) {
                    player.sendMessage(Component.text("Grappling orb isn't currently implemented yet")); //TODO

                    // Health Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 5) {
                    player.sendMessage(Component.text("Health orb isn't currently implemented yet")); //TODO
                    // It's not well known what Health Orbs did since they weren't in TubNet for very long

                    // Knockout Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 6) {
                    player.sendMessage(Component.text("Knockout orb isn't currently implemented yet")); //TODO

                    // Poison Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 7) {
                    player.sendMessage(Component.text("Poison orb isn't currently implemented yet")); //TODO
                    //It's not well known what Poison Orbs did since they weren't in TubNet for very long

                    // Winged Orb
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 8) {
                    player.sendMessage(Component.text("Winged orb isn't currently implemented yet")); //TODO

                    // Antiair Totem
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 9) {
                    player.sendMessage(Component.text("Antiair Totem isn't currently implemented yet")); //TODO

                    // Cloud Totem
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 10) {
                    player.sendMessage(Component.text("Cloud Totem isn't currently implemented yet")); //TODO
                    /*
                    Cloud Totems acted differently in different games
                    In Crystal Rush, the platform was made from glass (tinted glass?)
                    In Knockout the platform was made out of Crystal Blocks that matched your team's/leather armor's colour
                    We will need to somehow account for that by detecting what game is being played.
                    */

                    // Defense Totem
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 11) {
                    player.sendMessage(Component.text("Defence isn't currently implemented yet")); //TODO

                    // Healing Totem
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 12) {
                    player.sendMessage(Component.text("Healing Totem isn't currently implemented yet")); //TODO

                    // Launch Totem
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 13) {
                    player.sendMessage(Component.text("Launch Totem isn't currently implemented yet")); //TODO

                    // Slime Totem
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 14) {
                    player.sendMessage(Component.text("Slime Totem isn't currently implemented yet")); //TODO
                }
                    player.setCooldown(Material.COAL, 5); //quarter of a second cooldown
                }
            }
        }
    }