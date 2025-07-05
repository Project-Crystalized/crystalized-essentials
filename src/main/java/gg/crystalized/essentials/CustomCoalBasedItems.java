package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import gg.crystalized.essentials.CustomEntity.AntiairTotem;
import gg.crystalized.essentials.CustomEntity.CloudTotem;
import gg.crystalized.essentials.CustomEntity.LaunchTotem;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
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
			if (player.hasCooldown(Material.COAL) && player.getInventory().getItemInMainHand().equals(Material.COAL)) {
				player.sendMessage(text("[!] ᴛʜɪꜱ ɪᴛᴇᴍ ɪꜱ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ! ᴘʟᴇᴀꜱᴇ ᴡᴀɪᴛ").color(NamedTextColor.RED)
						.append(text(" " + player.getCooldown(Material.COAL) / 20.0).color(NamedTextColor.WHITE))
						.append(text(" ꜱᴇᴄᴏɴᴅꜱ ʙᴇꜰᴏʀᴇ ᴜꜱɪɴɢ ᴛʜɪꜱ ɪᴛᴇᴍ ᴀɢᴀɪɴ!").color(NamedTextColor.RED)));
			} else {
				ItemStack ItemR = player.getInventory().getItemInMainHand();
				if (!ItemR.hasItemMeta()) {return;}
				if (ItemR.getItemMeta().hasItemModel()) {
					// Boost Orb
					if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "boost_orb"))) {
						new BukkitRunnable(){
							double count = 0.2;
							final Location loc = player.getLocation();
							public void run(){
								if(count > 0.8){
									cancel();
								}
								circle(loc, count);
								count = count * 2;
							}
						}.runTaskTimer(crystalized_essentials.getInstance(), 0, 3);

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
                        PlayerData pd = crystalized_essentials.getInstance().getPlayerData(player.getName());
                        if (pd.isUsingGrapplingOrb) {return;}
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
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
						new BukkitRunnable(){
							double count = 0.2;
							final Location loc = player.getLocation();
							public void run(){
								if(count > 0.8){
									cancel();
								}
								circle(loc, count);
								count = count * 2;
							}
						}.runTaskTimer(crystalized_essentials.getInstance(), 0, 3);

						PlayerData pd = crystalized_essentials.getInstance().getPlayerData(player.getName());
						if (pd.isUsingWingedOrb) {return;}
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						crystalized_essentials.getInstance().useWingedOrb(player);

						// Antiair Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "antiair_totem"))) {
                        if (event.getClickedBlock() != null) {
							Location blockLoc = event.getClickedBlock().getLocation();
							if (new Location(player.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ()).getBlock().isEmpty()) {
								player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
								player.setCooldown(Material.COAL, 40);
								crystalized_essentials.getInstance().antiairTotemList.add(
										new AntiairTotem(
												player,
												//event.getClickedBlock().getLocation()
												new Location(player.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ())
										)
								);
							}
                        }

						// Cloud Totem
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "cloud_totem"))) {
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 80);
						new CloudTotem(player);

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
						if (event.getClickedBlock() != null) {
							Location blockLoc = event.getClickedBlock().getLocation();
							if (new Location(player.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ()).getBlock().isEmpty()) {
								player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
								player.setCooldown(Material.COAL, 30);
								new LaunchTotem(
										player,
										new Location(player.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ())
								);
							}
						}

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
		//Vector direction = p.getEyeLocation().getDirection();
		Snowball snowball = p.launchProjectile(Snowball.class, null);
        snowball.getLocation().add(snowball.getVelocity().normalize().multiply(1.5));
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
                PlayerData pd = crystalized_essentials.getInstance().getPlayerData(pname);
                pd.isUsingGrapplingOrb = true;
				//Bukkit.getServer().sendMessage(text("ProjectileHitEvent for grapplingorb, Player: " + p.getName()));
				if (en != null) {
					state = grapplingOrbState.PullEntity;
				} else if (b != null) {
					state = grapplingOrbState.TowardsTarget;
				}
				boolean isCloseToTarget = false;
				grapplingOrbState finalState = state;
                TextDisplay blockLocTempEntity = p.getWorld().spawn(new Location(Bukkit.getWorld("world"), 0, 0, 0), TextDisplay.class, entity -> {
                    //entity.text(text("[debug] Grapple Location"));
                });
                if (state.equals(grapplingOrbState.TowardsTarget)) {
                    blockLocTempEntity.teleport(b.getLocation());
                } else {
                    blockLocTempEntity.remove();
                }
				new BukkitRunnable() {
					int timesGrappled = 0;
					int timer = 0;

					@Override
					public void run() {
						timer++;

						if (timer == 5) {
							timer = 0;
							if (timesGrappled == 16 || timesGrappled > 16) {
								if (finalState == grapplingOrbState.TowardsTarget) {
									blockLocTempEntity.remove();
								}
								grapple(true);
								pd.isUsingGrapplingOrb = false;
								cancel();
								p.playSound(p, "minecraft:item.shield.break", 50, 1);
							}
                            grapple(false);
						}

						if (finalState == grapplingOrbState.PullEntity) {
							for (Entity entity : p.getNearbyEntities(2, 2, 2)) {
								if (entity == en) {
                                    grapple(true);
                                    pd.isUsingGrapplingOrb = false;
									cancel();
								}
							}
						} else if (finalState == grapplingOrbState.TowardsTarget) {
                            for (Entity entity : p.getNearbyEntities(3, 3, 3)) {
                                if (entity == blockLocTempEntity) { //this is dumb but it works ig
                                    blockLocTempEntity.remove();
                                    grapple(true);
                                    pd.isUsingGrapplingOrb = false;
                                    cancel();
                                }
                            }
						}
						//p.sendMessage(text("" + timer + ", State:" + finalState.toString()));
					}

                    void grapple(boolean finalGrapple) {
						timesGrappled++;
                        int x;
                        int y;
                        int z;

                        p.playSound(p, "minecraft:item.spyglass.use", 100, 1);
                        if (finalState == grapplingOrbState.PullEntity) {
                            int ex = (int) en.getLocation().getX();
                            int ey = (int) en.getLocation().getY();
                            int ez = (int) en.getLocation().getZ();
                            int px = (int) p.getLocation().getX();
                            int py = (int) p.getLocation().getY();
                            int pz = (int) p.getLocation().getZ();

							linearParticles(p.getLocation(), en.getLocation());

                            x = px - ex;
                            y = (int) (py - ey + 0.5);
                            z = pz - ez;
                            if (finalGrapple) {
                                en.setVelocity(new Vector(x, y, z).normalize().multiply(1.2));
                            } else {
                                en.setVelocity(new Vector(x, y, z).normalize());
                            }


                        } else if (finalState == grapplingOrbState.TowardsTarget) {
                            int bx = (int) blockLocTempEntity.getLocation().getX();
                            int by = (int) blockLocTempEntity.getLocation().getY();
                            int bz = (int) blockLocTempEntity.getLocation().getZ();
                            int px = (int) p.getLocation().getX();
                            int py = (int) p.getLocation().getY();
                            int pz = (int) p.getLocation().getZ();

							linearParticles(p.getLocation(), blockLocTempEntity.getLocation());

                            x = bx - px;
                            y = (int) (by - py + 0.5);
                            z = bz - pz;

                            p.setVelocity(new Vector(x, y, z).normalize());
                        }
                    }

				}.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

			}
		}
	}

	public void linearParticles(Location start, Location end) {
		ParticleBuilder builder = new ParticleBuilder(Particle.DUST);
		builder.color(Color.GRAY);
		builder.count(5);
		builder.offset(0, 0, 0);
		builder.extra(0);
		Vector v = new Vector(end.getX() - start.getX(), end.getY() - start.getY(), end.getZ() - start.getZ());
		int count = 0;
		Vector vec = v.multiply(0.1 * v.length());
		while (vec.length() <= v.length() && count < 100) {
			builder.location(start);
			builder.spawn();
			start = new Location(start.getWorld(), vec.getX() + start.getX(), vec.getY() + start.getY(), vec.getZ() + start.getZ());
			vec = vec.add(vec);
			count++;
		}
	}

	public void circle(Location middle, double radius) {
		double t = 0;
		ParticleBuilder builder = new ParticleBuilder(Particle.DUST);
		builder.color(Color.WHITE);
		Location loc;
		while (t <= 2 * Math.PI) {
			loc = circleEquation(middle, radius, t);
			builder.location(loc);
			builder.spawn();
			t = t + 0.3;
		}
	}



	public Location circleEquation(Location middle, double radius, double t){
		double x = radius * Math.cos(t) + middle.getX();
		double y = middle.getY();
		double z = radius * Math.sin(t) + middle.getZ();
		return new Location(middle.getWorld(), x, y, z);
	}
}

