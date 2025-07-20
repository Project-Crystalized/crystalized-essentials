package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import gg.crystalized.essentials.CustomEntity.AntiairTotem;
import gg.crystalized.essentials.CustomEntity.CloudTotem;
import gg.crystalized.essentials.CustomEntity.KnockoutOrb;
import gg.crystalized.essentials.CustomEntity.LaunchTotem;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;
import static org.bukkit.Particle.DUST;

//This class name is now sort of misleading with the item model changes a long while ago lmao
public class CustomCoalBasedItems implements Listener {

	//TODO I should probably clean this up - Callum
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
						launchBridgeOrb(player);
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 20);

						// Explosive Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "explosive_orb"))) {
						Vector direction = player.getEyeLocation().getDirection();
						Fireball fireball = player.launchProjectile(Fireball.class, direction);
						fireball.getLocation().add(fireball.getVelocity().normalize().multiply(3));
						fireball.setYield(3);
						player.getInventory().getItemInMainHand()
								.setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 8);

						// Grappling Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "grappling_orb"))) {
                        PlayerData pd = crystalized_essentials.getInstance().getPlayerData(player.getName());
                        if (pd.isUsingGrapplingOrb) {return;}
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 20);
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
						if (Bukkit.getOnlinePlayers().size() == 1) {
							player.sendMessage(text("[!] Knockout Orb cant be used while nobody else is online"));
						} else {
							new KnockoutOrb(player);
							player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
							player.setCooldown(Material.COAL, 20);
						}

						// Poison Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "poison_orb"))) {
						launchPoisonOrb(player);
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						player.setCooldown(Material.COAL, 10);

						// Winged Orb
					} else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "winged_orb"))) {
						PlayerData pd = crystalized_essentials.getInstance().getPlayerData(player.getName());
						if (pd.isUsingWingedOrb) {return;}
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
						player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
						crystalized_essentials.getInstance().useWingedOrb(player);
						player.setCooldown(Material.COAL, 40);

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

					else if (ItemR.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized", "debug"))) {
						crystalized_essentials.getInstance().getAllies(player);
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

	public void launchBridgeOrb(Player p) {
		Snowball snowball = p.launchProjectile(Snowball.class, null);
		snowball.setGravity(false);
		snowball.setVelocity(snowball.getVelocity().multiply(0.30));
		ItemStack item = snowball.getItem();
		ItemMeta meta = item.getItemMeta();
		meta.setItemModel(new NamespacedKey("crystalized", "bridge_orb"));
		meta.displayName(text("" + p.getName()));
		item.setItemMeta(meta);
		snowball.setItem(item);

		new BukkitRunnable() {
			int timer = 6 * 20;
			public void run() {
				Location blockLoc = new Location(snowball.getWorld(), snowball.getX(), snowball.getY() - 2, snowball.getZ());
				if (blockLoc.getBlock().isEmpty()) {
					//blockLoc.getBlock().setType(Material.AMETHYST_BLOCK);
					//blockLoc.getBlock().getState().update();
					placeBridgeOrbBlock(p, blockLoc);
				}

				if (timer == 0 || timer < 0 || snowball.isDead()) {
					snowball.remove();
					cancel();
				}
				timer--;
			}
		}.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
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

	public void launchPoisonOrb(Player p) {
		Snowball snowball = p.launchProjectile(Snowball.class, null);
		ItemStack item = snowball.getItem();
		ItemMeta meta = item.getItemMeta();
		meta.setItemModel(new NamespacedKey("crystalized", "poison_orb"));
		meta.displayName(text("" + p.getName()));
		item.setItemMeta(meta);
		snowball.setItem(item);
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
							if (timesGrappled == 16 || timesGrappled > 16 || p.isSneaking()) {
								stopGrapple();
							}
                            grapple(false);
						}

						if (finalState == grapplingOrbState.PullEntity) {
							for (Entity entity : p.getNearbyEntities(2, 2, 2)) {
								if (entity == en || p.isSneaking()) {
                                    stopGrapple();
								}
							}
						} else if (finalState == grapplingOrbState.TowardsTarget) {
                            for (Entity entity : p.getNearbyEntities(3, 3, 3)) {
                                if (entity == blockLocTempEntity || p.isSneaking()) { //this is dumb but it works ig
                                    stopGrapple();
                                }
                            }
						}
						//p.sendMessage(text("" + timer + ", State:" + finalState.toString()));
					}

					void stopGrapple() {
						if (finalState == grapplingOrbState.TowardsTarget) {
							blockLocTempEntity.remove();
						}
						grapple(true);
						pd.isUsingGrapplingOrb = false;
						cancel();
					}

                    void grapple(boolean finalGrapple) {
						timesGrappled++;
                        int x;
                        int y;
                        int z;

                        p.playSound(p, "minecraft:entity.llama.spit", 1, 0.5F);
						p.sendActionBar(text("Crouch to cancel Grapple").color(NamedTextColor.RED)); //TODO make this translatable
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
			else if (meta.getItemModel().equals(new NamespacedKey("crystalized", "poison_orb"))) {

				String pname = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
				Player p = Bukkit.getPlayer(pname);
				Location loc;
				if (en != null) {
					loc = e.getHitEntity().getLocation();
				} else {
					loc = e.getHitBlock().getLocation();
				}

				new BukkitRunnable() {
					int timer = 4;
					public void run() {
						ParticleBuilder builder = new ParticleBuilder(DUST);
						builder.color(Color.GREEN);
						builder.location(loc);
						builder.count(200);
						builder.offset(1.5, 1.5, 1.5);
						builder.spawn();
						for (Player e : loc.getNearbyPlayers(2.8)) {
							if (e != p) {
								e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2 * 20, 1, false, false, true));
								e.playSound(e, "minecraft:entity.puffer_fish.sting", 1, 1);
							}
						}

						timer --;
						if (timer == 0) {
							cancel();
						}
					}
				}.runTaskTimer(crystalized_essentials.getInstance(), 0, 20);
			}
		}
	}

	public void linearParticles(Location start, Location end) {
		ParticleBuilder builder = new ParticleBuilder(DUST);
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
		ParticleBuilder builder = new ParticleBuilder(DUST);
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

	//Could be optimised, Pain in the ass, copy pasted from cloud totem, fuck you
	private void placeBridgeOrbBlock(Player owner, Location loc) {
		boolean isKnockoff = false;
		try {
			Class<?> cls = Class.forName("gg.knockoff.game.knockoff");
			isKnockoff = true;
		} catch (ClassNotFoundException e) {
			isKnockoff = false;
		}
		Location loc2 = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ());
		ItemStack item = new ItemStack(Material.WHITE_WOOL);
		//fuck this
		BlockPlaceEvent shitWorkaround = new BlockPlaceEvent(
				loc.getBlock(),
				loc.getBlock().getState(),
				loc2.getBlock(),
				item,
				owner,
				true,
				EquipmentSlot.HAND
		);
		if (shitWorkaround.isCancelled()) {
			loc.getBlock().setType(Material.AIR);
		} else {
			if (isKnockoff) {
				if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/blue"))) {
					loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.EAST);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/cyan"))) {
					loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.NORTH);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/green"))) {
					loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.SOUTH);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/lemon"))) {
					loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.WEST);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/lime"))) {
					loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.EAST);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/magenta"))) {
					loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.NORTH);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/orange"))) {
					loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.SOUTH);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/peach"))) {
					loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.WEST);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/purple"))) {
					loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.EAST);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/white"))) {
					loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.SOUTH);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/yellow"))) {
					loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.WEST);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/red"))) {
					loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
					Directional dir = (Directional) loc.getBlock().getBlockData();
					dir.setFacing(BlockFace.NORTH);
					loc.getBlock().setBlockData(dir);
					loc.getBlock().getState().update();
				} else {
					loc.getBlock().setType(Material.AMETHYST_BLOCK);
					loc.getBlock().getState().update();
				}
				owner.playSound(loc, "minecraft:block.amethyst_block.place", 1, 1);
			} else {
				owner.playSound(loc, "minecraft:block.stone.place", 1, 1);
				loc.getBlock().setType(Material.STONE);
				loc.getBlock().getState().update();
			}
		}

		new BukkitRunnable() {
			Float breaking = 0.0F;
			int timer = 0;
			int entityID = crystalized_essentials.getInstance().getRandomNumber(-9000, -1001);
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					p.sendBlockDamage(loc, breaking, entityID);
				}

				if (breaking == 1F || breaking > 1F) {
					loc.getBlock().breakNaturally(new ItemStack(Material.AIR), true, false);
					cancel();
				}

				timer++;
				if (timer == 20 || timer > 20) {
					timer = 0;
					breaking = breaking + 0.2F;
				}
			}
		}.runTaskTimer(crystalized_essentials.getInstance(), 40, 1);
	}

	//Copy pasted from Cloud totem
	private boolean inventoryContainsItemModelItem(Player p, NamespacedKey itemModel) {
		PlayerInventory inv = p.getInventory();
		for (ItemStack item : inv.getContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasItemModel()) {
						if (item.getItemMeta().getItemModel().equals(itemModel)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}

