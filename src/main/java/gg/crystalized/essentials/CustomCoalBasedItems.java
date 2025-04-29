package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static net.kyori.adventure.text.Component.text;

//This class name is now sort of misleading with the item model changes a long while ago lmao
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
				player.sendMessage(text("[!] ᴛʜɪꜱ ɪᴛᴇᴍ ɪꜱ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ! ᴘʟᴇᴀꜱᴇ ᴡᴀɪᴛ").color(NamedTextColor.RED)
						.append(text(" " + player.getCooldown(Material.COAL) / 20.0).color(NamedTextColor.WHITE))
						.append(text(" ꜱᴇᴄᴏɴᴅꜱ ʙᴇꜰᴏʀᴇ ᴜꜱɪɴɢ ᴛʜɪꜱ ɪᴛᴇᴍ ᴀɢᴀɪɴ!").color(NamedTextColor.RED)));
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
						player.sendMessage(text("Bridge orb isn't currently implemented yet")); // TODO
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
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
						launchGrapplingOrb(player);

						// Health Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "health_orb"))) {
						player.sendMessage(text("Health orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
						// It's not well known what Health Orbs did since they weren't in TubNet for
						// very long

						// Knockout Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "knockout_orb"))) {
						player.sendMessage(text("Knockout orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Poison Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "poison_orb"))) {
						player.sendMessage(text("Poison orb isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Winged Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "winged_orb"))) {
						//player.sendMessage(Component.text("Winged orb isn't currently implemented yet")); // TODO
						PlayerData pd = crystalized_essentials.getInstance().getPlayerData(player.getName());
						if (pd.isUsingWingedOrb) {return;}
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setVelocity(new Vector(
								0,
								1.5,
								0)
						);
						pd.isUsingWingedOrb = true;
						pd.lastChestPlateBeforeWingedOrb = player.getInventory().getChestplate();
						player.getInventory().setChestplate(crystalized_essentials.getInstance().WingedOrbElytra);
						player.setGliding(true);

						player.setCooldown(Material.COAL, 40);

						// Antiair Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "antiair_totem"))) {
						player.sendMessage(text("Antiair Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Cloud Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "cloud_totem"))) {
						player.sendMessage(text("Cloud Totem isn't currently implemented yet")); // TODO
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
						player.sendMessage(text("Defence isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Healing Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "healing_totem"))) {
						player.sendMessage(text("Healing Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Launch Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "launch_totem"))) {
						player.sendMessage(text("Launch Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);

						// Slime Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "slime_totem"))) {
						player.sendMessage(text("Slime Totem isn't currently implemented yet")); // TODO
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 5);
					}
				}
			}
		}
	}

	@EventHandler
	public void onElytra(EntityToggleGlideEvent e) {
		if (!(e.getEntity() instanceof Player)) {return;}
		Player p = (Player) e.getEntity();
		if (p.isGliding()) { //Player would be on ground, this should be true
			PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
			if (!pd.isUsingWingedOrb) {
				return;
			} else {
				pd.isUsingWingedOrb = false;
				p.getInventory().setChestplate(pd.lastChestPlateBeforeWingedOrb);
			}
		}
	}

	public void launchGrapplingOrb(Player p) {
		Vector direction = p.getEyeLocation().getDirection();
		Snowball snowball = p.launchProjectile(Snowball.class, direction);
		//snowball.getLocation().add(snowball.getVelocity().normalize().multiply(5));
		ItemStack item = snowball.getItem();
		ItemMeta meta = item.getItemMeta();
		meta.setItemModel(new NamespacedKey("crystalized", "grappling_orb"));
		meta.displayName(text("" + p.getName())); //ProjectileHitEvent doesn't have a method for getting the player name
		item.setItemMeta(meta);
		snowball.setItem(item);
		//snowball.setCustomNameVisible(false);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		if (!(e.getEntity() instanceof Snowball)) {
			return;
		}
		Entity en = e.getHitEntity();
		Block b = e.getHitBlock();
		ItemStack item = ((Snowball) e.getEntity()).getItem();
		ItemMeta meta = item.getItemMeta();

		if (meta.hasItemModel()) {
			if (meta.getItemModel().equals(new NamespacedKey("crystalized", "grappling_orb"))) {
				enum grapplingOrbState{
					placeholderValue, //fuck you
					TowardsTarget,
					PullEntity,
				}
				grapplingOrbState state = grapplingOrbState.placeholderValue;

				String pname = PlainTextComponentSerializer.plainText().serialize(meta.displayName()); //I dont trust this, I just got this from the Paper discord
				Player p = Bukkit.getPlayer(pname);
				//Bukkit.getServer().sendMessage(text("ProjectileHitEvent for grapplingorb, Player: " + p.getName()));
				Location targetLoc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
				if (en != null) {
					Bukkit.getServer().sendMessage(text("Grappling Orb hit entity"));
					targetLoc = en.getLocation();
					state = grapplingOrbState.PullEntity;
				} else if (b != null) {
					Bukkit.getServer().sendMessage(text("Grappling Orb hit wall/block"));
					targetLoc = b.getLocation();
					state = grapplingOrbState.TowardsTarget;
				}
				boolean isCloseToTarget = false;
				grapplingOrbState finalState = state;

				Location loc = targetLoc;
				new BukkitRunnable() {
					int timer = 0;

					@Override
					public void run() {
						timer++;
						if (timer == 10) {
							timer = 0;
							if (finalState == grapplingOrbState.PullEntity) {
								int ex = (int) en.getLocation().getX();
								int ey = (int) en.getLocation().getY();
								int ez = (int) en.getLocation().getZ();
								int px = (int) p.getLocation().getX();
								int py = (int) p.getLocation().getY();
								int pz = (int) p.getLocation().getZ();

								int x = Math.abs(ex - px) - 3;
								int y = Math.abs(ey - py) - 3;
								int z = Math.abs(ez - pz) - 3;

								en.setVelocity(new Vector(x, y, z).normalize());
								//en.setVelocity(p.getLocation().getDirection().multiply(1.05));

								/*Vector pos = en.getLocation().toVector();
								Vector target = loc.toVector();
								Vector velocity = target.subtract(pos);
								en.setVelocity(velocity.normalize().multiply(2));*/

							} else if (finalState == grapplingOrbState.TowardsTarget) {

							}
						}

						if (finalState == grapplingOrbState.PullEntity) {
							for (Entity entity : p.getNearbyEntities(3, 3, 3)) {
								if (entity == en) {
									stopGrapple();
								}
							}
						} else if (finalState == grapplingOrbState.TowardsTarget) {
							cancel();
						}

						p.sendMessage(text("" + timer + ", State:" + finalState.toString()));
					}

					void stopGrapple() {
						cancel();
					}

				}.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

			}
		}
	}

}
