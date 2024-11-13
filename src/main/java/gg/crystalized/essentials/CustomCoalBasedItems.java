package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

// Had to make them 1 class because this didn't work with multiple classes checking for the same thing, so yea this files going to be *long* when we actually implement these things lmao. sry for my shit code - Callum

public class CustomCoalBasedItems implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction().isRightClick()) {
            if (player.hasCooldown(Material.COAL)) {
                player.sendMessage(Component.text("This item is on cooldown!").color(NamedTextColor.RED));
            } else {
                if (player.getInventory().getItemInMainHand().getType().equals(Material.COAL) && player.getEquipment().getItemInMainHand().getItemMeta().hasCustomModelData()) {
                    // Boost Orb
                    if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 1) {
                        player.playSound(player, "minecraft:item.armor.equip_elytra", 50, 1); //TODO either make a sound(s) for the boost orb or figure out what exact sound(s) TubNet used.
                        player.setVelocity(new Vector(player.getVelocity().getX(), player.getVelocity().getY(), player.getVelocity().getZ()));
                        player.setVelocity(player.getLocation().getDirection().multiply(2));
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 40);

                        // Bridge Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 2) {
                        player.sendMessage(Component.text("Bridge orb isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Explosive Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 3) {
                        Vector direction = player.getEyeLocation().getDirection();
                        Fireball fireball = player.launchProjectile(Fireball.class, direction);
                        fireball.getLocation().add(fireball.getVelocity().normalize().multiply(3));
                        fireball.setYield(3);
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Grappling Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 4) {
                        player.sendMessage(Component.text("Grappling orb isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Health Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 5) {
                        player.sendMessage(Component.text("Health orb isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);
                        // It's not well known what Health Orbs did since they weren't in TubNet for very long

                        // Knockout Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 6) {
                        player.sendMessage(Component.text("Knockout orb isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Poison Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 7) {
                        player.sendMessage(Component.text("Poison orb isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);
                        //It's not well known what Poison Orbs did since they weren't in TubNet for very long

                        // Winged Orb
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 8) {
                        player.sendMessage(Component.text("Winged orb isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Antiair Totem
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 9) {
                        player.sendMessage(Component.text("Antiair Totem isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Cloud Totem
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 10) {
                        player.sendMessage(Component.text("Cloud Totem isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);
                    /*
                    Cloud Totems acted differently in different games
                    In Crystal Rush, the platform was made from glass (tinted glass?)
                    In Knockout the platform was made out of Crystal Blocks that matched your team's/leather armor's colour
                    We will need to somehow account for that by detecting what game is being played.
                    */

                        // Defense Totem
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 11) {
                        player.sendMessage(Component.text("Defence isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Healing Totem
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 12) {
                        player.sendMessage(Component.text("Healing Totem isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Launch Totem
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 13) {
                        player.sendMessage(Component.text("Launch Totem isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);

                        // Slime Totem
                    } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 14) {
                        player.sendMessage(Component.text("Slime Totem isn't currently implemented yet")); //TODO
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        player.setCooldown(Material.COAL, 5);
                    }
                }
            }
        }
    }
}