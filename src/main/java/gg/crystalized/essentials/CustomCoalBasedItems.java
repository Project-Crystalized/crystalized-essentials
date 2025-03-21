package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.security.spec.NamedParameterSpec;

// Had to make them 1 class because this didn't work with multiple classes checking for the same thing, so yea this files going to be *long* when we actually implement these things lmao. sry for my shit code - Callum

public class CustomCoalBasedItems implements Listener {

	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY) {
			return;
		}
		Player player = event.getPlayer();
		if (event.getHand() != EquipmentSlot.HAND)
			return;
		if (event.getAction().isRightClick()) {
			if (player.hasCooldown(Material.COAL)) {
				player.sendMessage(Component.text("[!] ᴛʜɪꜱ ɪᴛᴇᴍ ɪꜱ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ! ᴘʟᴇᴀꜱᴇ ᴡᴀɪᴛ").color(NamedTextColor.RED)
						.append(Component.text(" " + player.getCooldown(Material.COAL) / 20.0).color(NamedTextColor.WHITE))
						.append(Component.text(" ꜱᴇᴄᴏɴᴅꜱ ʙᴇꜰᴏʀᴇ ᴜꜱɪɴɢ ᴛʜɪꜱ ɪᴛᴇᴍ ᴀɢᴀɪɴ!").color(NamedTextColor.RED)));
			} else {
				ItemStack ItemR = player.getInventory().getItemInMainHand();
				if (!ItemR.hasItemMeta()) {return;}
				if (ItemR.getItemMeta().hasItemModel()) {
					// Boost Orb
					// TODO make particles for when you launch
					if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "boost_orb"))) {
						player.playSound(player, "minecraft:item.armor.equip_elytra", 50, 1);
						player.setVelocity(new Vector(player.getVelocity().getX(), player.getVelocity().getY(), player.getVelocity().getZ()));
						player.setVelocity(player.getLocation().getDirection().multiply(2));
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 40);

						// Bridge Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "bridge_orb"))) {
						player.sendMessage(Component.text("Bridge orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Explosive Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "explosive_orb"))) {
						Vector direction = player.getEyeLocation().getDirection();
						Fireball fireball = player.launchProjectile(Fireball.class, direction);
						fireball.getLocation().add(fireball.getVelocity().normalize().multiply(3));
						fireball.setYield(3);
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Grappling Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "grappling_orb"))) {
						player.sendMessage(Component.text("Grappling orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
						/*
						 * Vector direction = player.getEyeLocation().getDirection();
						 * Snowball snowball = player.launchProjectile(Snowball.class, direction);
						 * snowball.getLocation().add(snowball.getVelocity().normalize().multiply(3));
						 */

						// Health Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "health_orb"))) {
						player.sendMessage(Component.text("Health orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
						// It's not well known what Health Orbs did since they weren't in TubNet for
						// very long

						// Knockout Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "knockout_orb"))) {
						player.sendMessage(Component.text("Knockout orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Poison Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "poison_orb"))) {
						player.sendMessage(Component.text("Poison orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
						// It's not well known what Poison Orbs did since they weren't in TubNet for
						// very long

						// Winged Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "winged_orb"))) {
						player.sendMessage(Component.text("Winged orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Antiair Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "antiair_totem"))) {
						player.sendMessage(Component.text("Antiair Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Cloud Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "cloud_totem"))) {
						player.sendMessage(Component.text("Cloud Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
						/*
						 * Cloud Totems acted differently in different games
						 * In Crystal Rush, the platform was made from glass (tinted glass?)
						 * In Knockout the platform was made out of Crystal Blocks that matched your
						 * team's/leather armor's colour
						 * We will need to somehow account for that by detecting what game is being
						 * played.
						 */

						// Defense Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "defense_totem"))) {
						player.sendMessage(Component.text("Defence isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Healing Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "healing_totem"))) {
						player.sendMessage(Component.text("Healing Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Launch Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "launch_totem"))) {
						player.sendMessage(Component.text("Launch Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Slime Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "slime_totem"))) {
						player.sendMessage(Component.text("Slime Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
					}
				}
			}
		}
	}
}
