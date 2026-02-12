package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import gg.crystalized.essentials.CustomEntity.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

import static net.kyori.adventure.text.Component.text;
import static org.bukkit.Particle.DUST;

//This class name is now sort of misleading with the item model changes a long while ago lmao
public class CustomCoalBasedItems implements Listener {

	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.useItemInHand() == Result.DENY) {
			return;
		}
		Player p = e.getPlayer();
		ItemStack item = p.getInventory().getItemInMainHand();
		if (e.getHand() != EquipmentSlot.HAND || p.getGameMode().equals(GameMode.SPECTATOR)) return;
		if (e.getAction().isRightClick()) {
			if (p.hasCooldown(Material.COAL) && item.getType().equals(Material.COAL)) {
				p.sendMessage(text("[!] ᴛʜɪꜱ ɪᴛᴇᴍ ɪꜱ ᴏɴ ᴄᴏᴏʟᴅᴏᴡɴ! ᴘʟᴇᴀꜱᴇ ᴡᴀɪᴛ").color(NamedTextColor.RED)
						.append(text(" " + p.getCooldown(Material.COAL) / 20.0).color(NamedTextColor.WHITE))
						.append(text(" ꜱᴇᴄᴏɴᴅꜱ ʙᴇꜰᴏʀᴇ ᴜꜱɪɴɢ ᴛʜɪꜱ ɪᴛᴇᴍ ᴀɢᴀɪɴ!").color(NamedTextColor.RED)));
			} else {
				if (!item.hasItemMeta()) {return;}
				if (item.getItemMeta().hasItemModel()) {
					//
					switch (item.getItemMeta().getItemModel().getKey()) {
						case "boost_orb" -> {
							new BukkitRunnable(){
								double count = 0.2;
								final Location loc = p.getLocation();
								public void run(){
									if(count > 0.8){
										cancel();
									}
									circle(loc, count);
									count = count * 2;
								}
							}.runTaskTimer(crystalized_essentials.getInstance(), 0, 3);

							p.playSound(p, "minecraft:item.armor.equip_elytra", 50, 1);
							p.setVelocity(new Vector(p.getVelocity().getX(), p.getVelocity().getY(), p.getVelocity().getZ()));
							p.setVelocity(p.getLocation().getDirection().multiply(2));
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 40);
						}
						case "bridge_orb" -> {
							launchBridgeOrb(p);
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 20);
						}
						case "explosive_orb" -> {
							Vector direction = p.getEyeLocation().getDirection();
							Fireball fireball = p.launchProjectile(Fireball.class, direction);
							fireball.getLocation().add(fireball.getVelocity().normalize().multiply(3));
							fireball.setYield(1);
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 8);
						}
						case "grappling_orb" -> {
							PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
							if (pd.isUsingGrapplingOrb) {return;}
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 20);
							launchGrapplingOrb(p);
						}
						case "health_orb" -> {
							p.sendMessage(text("Health orb isn't currently implemented yet")); // TODO
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 5);
						}
						case "knockout_orb" -> {
							if (Bukkit.getOnlinePlayers().size() == 1) {
								p.sendMessage(text("[!] Knockout Orb cant be used while nobody else is online"));
							} else {
								crystalized_essentials.getInstance().knockoutOrbList.add(new KnockoutOrb(p));
								item.setAmount(item.getAmount() - 1);
								p.setCooldown(Material.COAL, 20);
							}
						}
						case "poison_orb" -> {
							launchPoisonOrb(p);
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 10);
						}
						case "winged_orb" -> {
							PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
							if (pd.isUsingWingedOrb) {return;}
							new BukkitRunnable(){
								double count = 0.2;
								final Location loc = p.getLocation();
								public void run(){
									if(count > 0.8){
										cancel();
									}
									circle(loc, count);
									count = count * 2;
								}
							}.runTaskTimer(crystalized_essentials.getInstance(), 0, 3);
							crystalized_essentials.getInstance().useWingedOrb(p);
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 40);
						}

						case "antiair_totem" -> {
							if (e.getClickedBlock() != null) {
								Location blockLoc = e.getClickedBlock().getLocation();
								if (new Location(p.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ()).getBlock().isEmpty()) {
									item.setAmount(item.getAmount() - 1);
									p.setCooldown(Material.COAL, 40);
									crystalized_essentials.getInstance().antiairTotemList.add(
											new AntiairTotem(
													p,
													new Location(p.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ())
											)
									);
								}
							}
						}
						case "cloud_totem" -> {
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 80);
							new CloudTotem(p);
						}
						case "defense_totem" -> {
							if (e.getClickedBlock() != null) {
								Location blockLoc = e.getClickedBlock().getLocation();
								if (new Location(p.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ()).getBlock().isEmpty()) {
									item.setAmount(item.getAmount() - 1);
									p.setCooldown(Material.COAL, 30);
									crystalized_essentials.getInstance().defenceTotemList.add(
											new DefenceTotem(
													p,
													new Location(p.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ())
											)
									);
								}
							}
						}
						case "healing_totem" -> {
							p.sendMessage(text("Healing Totem isn't currently implemented yet")); // TODO
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 5);
						}
						case "launch_totem" -> {
							if (e.getClickedBlock() != null) {
								Location blockLoc = e.getClickedBlock().getLocation();
								if (new Location(p.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ()).getBlock().isEmpty()) {
									item.setAmount(item.getAmount() - 1);
									p.setCooldown(Material.COAL, 30);
									new LaunchTotem(
											p,
											new Location(p.getWorld(), blockLoc.getX(), blockLoc.getY() + 1, blockLoc.getZ())
									);
								}
							}
						}
						case "slime_totem" -> {
							p.sendMessage(text("Slime Totem isn't currently implemented yet")); // TODO
							item.setAmount(item.getAmount() - 1);
							p.setCooldown(Material.COAL, 5);
						}
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
        Snowball sb = p.launchProjectile(Snowball.class, null);

        // fast & straight "hook"
        sb.setGravity(false);
        sb.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(1.8));

        // tag: robust identification on hit
        sb.getPersistentDataContainer().set(
                new NamespacedKey("crystalized", "grapple_owner"),
                org.bukkit.persistence.PersistentDataType.STRING,
                p.getUniqueId().toString()
        );
        sb.getPersistentDataContainer().set(
                new NamespacedKey("crystalized", "is_grapple"),
                org.bukkit.persistence.PersistentDataType.BYTE,
                (byte)1
        );

        // The model logic
        ItemStack item = sb.getItem();
        ItemMeta meta = item.getItemMeta();
        meta.setItemModel(new NamespacedKey("crystalized", "grappling_orb"));
        meta.displayName(text(p.getName()));
        item.setItemMeta(meta);
        sb.setItem(item);
        //sb.setCustomNameVisible(false);

        // rope trail behind the projectile (similiar to how it was on TubNet) and gravity
        new BukkitRunnable() {
            final int DROP_DELAY_TICKS = 10; // 0.5s; lower is shorter straight range
            int t = 0;
            //it has a life incase player missing
            int life = 100; // up to 5s of flight before despawn if it never hits
            boolean gravityFlipped = false;  // ensures it set gravity only once
            @Override public void run() {

                if (sb.isDead() || !sb.isValid()) { cancel(); return; }
                // after delay, enables gravity (once)
                if (!gravityFlipped && t >= DROP_DELAY_TICKS) {
                    sb.setGravity(true);
                    gravityFlipped = true;

                    // (optional) in the future potentialy change drop to be a little steeper:
                    // sb.setVelocity(sb.getVelocity().add(new Vector(0, -0.03, 0)));
                }
                // rope trails behind the projectile, a check so it doesn't mess up
                Vector v = sb.getVelocity();
                if (v.lengthSquared() > 1e-6) {
                    Location tail = sb.getLocation().clone()
                            .subtract(v.clone().normalize().multiply(0.30));
                    sb.getWorld().spawnParticle(Particle.CRIT, tail, 2, 0.02, 0.02, 0.02, 0.0);
                }
                if (--life <= 0) { sb.remove(); cancel(); }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);



        /* */




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
            //The Grapling code massive changes
            //Wasn't working sometimes so added extra checks
            if (meta.hasItemModel() && meta.getItemModel().equals(new NamespacedKey("crystalized", "grappling_orb"))) {
                // proves it's this grapple via PDC flag (PDC stands for Persistent Data Container )
                Snowball proj = (Snowball) e.getEntity();

                //boolen Check if this snowball is one of the Grappling Orbs
                // by looking for the PDC flag "crystalized:is_grapple" (stored as BYTE).
                boolean isGrapple = Boolean.TRUE.equals(
                        proj.getPersistentDataContainer().has(
                                new NamespacedKey("crystalized", "is_grapple"),
                                org.bukkit.persistence.PersistentDataType.BYTE
                        )
                );
                if (!isGrapple) {
                    // fallback: still allow old snowballs that only used item model
                    // if in the future we want PDC only, just put here : e.getEntity().remove(); return;
                }

                // Resolve owner(the player who shot the grapling hook) from PDC (robust)
                String ownerUUID = proj.getPersistentDataContainer().get(
                        new NamespacedKey("crystalized", "grapple_owner"),
                        org.bukkit.persistence.PersistentDataType.STRING
                );
                Player p = null;
                //Error handeling
                if (ownerUUID != null) {
                    try { p = Bukkit.getPlayer(java.util.UUID.fromString(ownerUUID)); } catch (IllegalArgumentException ignored) {}
                }
                // Fallback: display name (keeps older orbs working)
                //Incase the logic doesn't work properly
                if (p == null) {
                    String pname = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
                    p = Bukkit.getPlayer(pname);
                }
                //Removes if player died, not online or in spectator as well as null

                if (p == null || !p.isOnline() || p.isDead() || p.getGameMode() == GameMode.SPECTATOR) {
                    e.getEntity().remove();
                    return;
                }

                // Keeps the "using grapple" flag on for a few ticks after impact to avoid double-triggers
                // and re-firing in the same tick/frame. Cleared later via the delayed task.

                final int LOCK_TICKS = 10;


                // Grab player state and mark that this player is currently in a grapple.
                // Prevents launching another grapple until this interaction finishes (or lock expires).
                PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
                if (pd != null) pd.isUsingGrapplingOrb = true;


                // What did the graple orb hit? Caches both so it is possible to branch:
                Entity hit = e.getHitEntity();
                Block  blk = e.getHitBlock();
                // CASE A: 'hit' is a Player (victim pull)  |  CASE B: 'blk' is a Block (self pull).

                // --- CASE A: player hook: short pull, then auto-fling: Shift = instant fling -----

                //----- CASE A ------------
                if (hit instanceof Player && !((Player) hit).getUniqueId().equals(p.getUniqueId())) {

                    // player who fired the grapple
                    final Player grappler = p;
                    // victim the player pulled toward grappler
                    final Player victim   = (Player) hit;

                    /* --- Timing (ticks = 1/20s) ---
                     Gives the rope a brief pull window, then automatically "fling" the victim
                        past the grappler so they don't body-stop. Shift by the grappler triggers the
                        same fling instantly.
                     */
                    final int    AUTO_FLING_AT   = 8;     // starts auto-fling after 0.4s of pulling
                    final int    MAX_PULL_TICKS  = 20;    // hard safety stop (1s) if conditions get weird

                    /* ---Pull shaping (horizontal speed along rope)---
                     targetSpeed = clamp(BASE_V + DIST_GAIN * distance, <= V_MAX)
                        The only reason to add ACCEL_CAP per tick to avoid watchdog/fastClip stalls.
                        Units are "blocks per tick" (B/tick). 1.0 roughly 20 blocks/sec horizontally.
                     */
                    final double BASE_V          = 1.05; // base rope speed even at close range
                    final double DIST_GAIN       = 0.10;  // extra speed per block of separation
                    final double V_MAX           = 2.40; // absolute target speed cap (keep on the lower side be safe)
                    final double ACCEL_CAP       = 0.55; // per-tick acceleration clamp (prevents huge jumps)

                    /* --- Vertical assist while pulling ---
                        Small upward help each tick; a bit more when the victim is falling so they
                        arc(like a projectlie motion arc) nicely instead of face-planting (hitting blocks with face) mid-pull.
                     */

                    final double UP_TICK         = 0.10; // baseline upward bias per tick
                    final double FALL_GAIN       = 0.45; // extra up when current Y-vel is downward

                    /* ---Fling impulse (used for both Shift-fling and auto-fling)---
                         When player decides to fling,  the velocity is replaced with a forward+up impulse:
                         forward = max(current_along_rope, FLING_MIN_FWD) + FLING_EXTRA_FWD
                         then clamp with CAP_HORIZ/CAP_UP for watchdog safety.
                         Increase FLING_UP for a higher arc; increase FLING_*FWD for more carry-through.
                     */
                    final double FLING_MIN_FWD   = 1.35; // minimum forward component on fling
                    final double FLING_EXTRA_FWD = 0.35; // extra shove to guarantee pass-through the owner
                    final double FLING_UP        = 0.18; // pop upward at the moment of fling
                    final double CAP_HORIZ       = 2.40; // horizontal cap on resulting velocity
                    final double CAP_UP          = 0.60; // upward cap on resulting velocity


                    /* ---Auto-fling proximity trigger---
                        If the victim gets this close to the grappler before AUTO_FLING_AT, it flings early.
                     */
                    final double CLOSE_TRIG = 1.00;

                    // Attempt to avoid “face stop”, when the victim collides into the owner of the graple
                    final int NOCOLLIDE_T = 12; // ticks of no-collision (0.6s) during/after fling

                    //sound
                    grappler.playSound(victim.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 1.10f);

                    new BukkitRunnable() {
                        int ticks = 0;

                        @Override public void run() {
                            //Checks
                            if (!grappler.isOnline() || grappler.isDead() || grappler.getGameMode()==GameMode.SPECTATOR
                                    || !victim.isValid() || victim.isDead() || victim.getGameMode()==GameMode.SPECTATOR) {
                                finish(); return;
                            }

                            //Getting locations, vectors, and distance
                            Location vPt = victim.getLocation().add(0, 0.9, 0);
                            Location pPt = grappler.getLocation().add(0, 0.9, 0);
                            Vector toP   = pPt.toVector().subtract(vPt.toVector());
                            double dist  = Math.max(1e-4, toP.length());
                            Vector dir   = toP.multiply(1.0 / dist); // victim -> grappler

                            // Shift = instant fling
                            if (grappler.isSneaking()) {
                                //Thoes helper methods are at the end of this java file
                                beginNoCollision(grappler, victim, NOCOLLIDE_T);
                                flingVictim(victim, dir, FLING_MIN_FWD, FLING_EXTRA_FWD, FLING_UP, CAP_HORIZ, CAP_UP, 8);
                                finish(); return;
                            }

                            // auto-fling after a short pull or when very close
                            if (ticks >= AUTO_FLING_AT || dist < CLOSE_TRIG) {
                                beginNoCollision(grappler, victim, NOCOLLIDE_T);
                                flingVictim(victim, dir, FLING_MIN_FWD, FLING_EXTRA_FWD, FLING_UP, CAP_HORIZ, CAP_UP, 8);
                                finish(); return;
                            }

                            // --- Pulling phase (springy but capped/limitied) ---
                                // It accelerates the victim *along the rope direction* toward the grappler.
                                // targetSpeed grows with distance but is bounded/limitied, and per-tick acceleration
                                // is clamped/limitied so it don't spike velocity (keeps watchdog/fastClip happy).

                            double target = Math.min(BASE_V + dist * DIST_GAIN, V_MAX); // desired along-rope speed (B/tick)
                            Vector vv     = victim.getVelocity();  // current velocity
                            double along  = vv.dot(dir); // component of vv along the rope (victim -> grappler
                            double add    = Math.min(Math.max(0, target - along), ACCEL_CAP); // how much speed to add this tick (clamped)

                            if (add > 0) {
                                // Applys forward impulse plus a little vertical help so pulls feel smoother.
                                // If the victim is falling, gives extra upward assist to avoid face planting (where they will just hit the block with their face).
                                Vector impulse = dir.multiply(add);  // forward (along-rope) impulse
                                double up = UP_TICK;    // baseline up bias
                                if (vv.getY() < 0) up += Math.min(0.40, -vv.getY() * FALL_GAIN); // fall-save boost (clamped)
                                impulse.setY(impulse.getY() + up);

                                // Sets new velocity but cap horizontal and upward components for safety.
                                // capVelocity(v, CAP_HORIZ, CAP_UP) limits horizontal speed and max upward speed.
                                victim.setVelocity(capVelocity(vv.add(impulse), CAP_HORIZ, CAP_UP));
                            }

                            // Prevents weird pottential fall-damage during/after rope motion.
                            victim.setFallDistance(0f);
                            grappler.setFallDistance(0f);


                            // Hard safety: if somehow it keeps pulling too long, auto-convert to a fling
                            // so the victim passes through cleanly instead of sticking.

                            if (++ticks >= MAX_PULL_TICKS) { // safety
                                beginNoCollision(grappler, victim, NOCOLLIDE_T);
                                flingVictim(victim, dir, FLING_MIN_FWD, FLING_EXTRA_FWD, FLING_UP, CAP_HORIZ, CAP_UP, 8);
                                finish();
                            }
                        }

                        void finish() {
                            cancel();
                            PlayerData pd = crystalized_essentials.getInstance().getPlayerData(grappler.getName());
                            if (pd != null) Bukkit.getScheduler().runTaskLater(crystalized_essentials.getInstance(),
                                    () -> pd.isUsingGrapplingOrb = false, 10);
                        }
                    }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

                    e.getEntity().remove();
                    return;
                }

                // --- CASE B: graple to block -- climb-aware up-assist; Shift cancel keeps momentum with thrust; auto-cling/stick ---
                if (blk != null) {
                    final Player grappler = p;

                    // Pull feel (owners speed along rope = BASE_V + distance * DIST_GAIN, clamped by V_MAX)
                    final int    MAX_TICKS     = 100;  // rope life for block pulls
                    final double BASE_V        = 0.95; // base target m/tick even at zero distance
                    final double DIST_GAIN     = 0.055; // extra target speed per block of distance to hook
                    final double V_MAX         = 2.30; // hard cap of target speed to keep motion tame
                    final double ACCEL_CAP     = 0.45; // per-tick acceleration cap (watchdog-safe)

                    // up help
                    final double UP_TICK       = 0.12; // small up every tick to reduce being stuck in blocks
                    final double UP_SNAP       = 0.22; // extra up for first SNAP_TICKS ticks to feel snapier
                    final int    SNAP_TICKS    = 6; // how long UP_SNAP applies
                    final double FALL_GAIN     = 0.85; // when falling (vy<0): add min(0.8, -vy * FALL_GAIN)

                    // Climb-aware lift (extra help when hook is above player). Should save players
                    final double CLIMB_GAIN    = 0.035;  // extra up per block hook is above player eye
                    final double CLIMB_UP_CAP  = 0.34;   // per-tick cap for that climb assist

                    // panic when really falling
                    final double PANIC_Y       = -0.90; // if vy < PANIC_Y, treat as "plumenting"/void falling
                    final double PANIC_UP      = 0.35; // emergency up boost in that case

                    // cling (default), Shift to drop early
                    final double CLING_DIST    = 0.55; // start clinging when within this distance of hook
                    final int    CLING_TICKS   = 36; // cling duration if Shift isn’t pressed
                    final double CLING_PULL    = 0.12; // small pull toward hook while clinging
                    final double CLING_UP      = 0.05;  // small up while clinging (feels “magnetic/sticky”)

                    // Velocity caps
                    final double CAP_HORIZ     = 2.10; // clamp horizontal speed (server-friendly)
                    final double CAP_UP        = 0.85;   // a bit higher to help big climbs // clamp upward boost (avoid stalls)

                    // Shift-cancel-thrust (exit nudge when you Shift mid-air)
                    final double CANCEL_MIN_FWD   = 1.20; // ensure at least this forward after cancel
                    final double CANCEL_EXTRA_FWD = 0.25;  // small extra forward on top
                    final double CANCEL_UP        = 0.20;  // a touch more up on cancel
                    final double CANCEL_CAP_H     = 2.50; // caps used only for the cancel impulse
                    final double CANCEL_CAP_UP    = 0.85;

                    final Location hook = blk.getLocation().add(0.5, 0.5, 0.5);
                    grappler.playSound(hook, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 1.0f);

                    new BukkitRunnable() {
                        int ticks = 0;
                        boolean clinging = false;
                        int clingLeft = 0;

                        @Override public void run() {
                            //checks to stop imidetely if any of this happens
                            if (!grappler.isOnline() || grappler.isDead() || grappler.getGameMode()==GameMode.SPECTATOR) {
                                finish(); return;
                            }

                            // Vector from player eye to hook (the ropes direction)
                            Location eye = grappler.getEyeLocation();
                            Vector   toHook = hook.toVector().subtract(eye.toVector());
                            double   dist   = Math.max(1e-4, toHook.length());
                            Vector   dir    = toHook.multiply(1.0 / dist); //unit vector: player to hook

                            // MID-AIR cancel (keep momentum + adds a little thrust)
                            if (!clinging && grappler.isSneaking()) {
                                applyCancelThrust(grappler, dir,
                                        CANCEL_MIN_FWD, CANCEL_EXTRA_FWD, CANCEL_UP,
                                        CANCEL_CAP_H, CANCEL_CAP_UP);
                                finish(); return;
                            }

                            // enters cling when close
                            if (!clinging && dist <= CLING_DIST) {
                                clinging = true;
                                clingLeft = CLING_TICKS;
                            }

                            if (clinging) {
                                // while clinging: Shift or player has to wait to release (no thrust here)
                                // manual release
                                if (grappler.isSneaking()) { finish(); return; }
                                Vector v = grappler.getVelocity();
                                Vector hold = dir.multiply(CLING_PULL);// gentle pull toward hook
                                hold.setY(hold.getY() + CLING_UP); // small upward bias while clinging
                                grappler.setVelocity(v.multiply(0.88).add(hold)); // damp + hold
                                grappler.setFallDistance(0f);
                                if (--clingLeft <= 0) { finish(); } //auto relese after time
                                return;
                            }

                            // --- pull phase (with climb-aware up assist) ---
                            // Calculates target speed along rope and add a capped impulse toward the hook
                            double target = Math.min(BASE_V + dist * DIST_GAIN, V_MAX); // springy target
                            Vector vv     = grappler.getVelocity();
                            double along  = vv.dot(dir); // current speed along rope
                            double add    = Math.min(Math.max(0, target - along), ACCEL_CAP);  // capped accel

                            if (add > 0) {
                                Vector impulse = dir.multiply(add); // forward impulse

                                // base up + snap + fall save
                                double up = UP_TICK + (ticks < SNAP_TICKS ? UP_SNAP : 0);
                                if (vv.getY() < 0) up += Math.min(0.8, -vv.getY() * FALL_GAIN); // falling help

                                // extra climb help if hook is above you
                                double yDiff = hook.getY() - eye.getY();        // blocks above player eye
                                if (yDiff > 0) up += Math.min(CLIMB_UP_CAP, yDiff * CLIMB_GAIN);

                                // panic save if really plummeting/falling
                                if (vv.getY() < PANIC_Y) up += PANIC_UP;

                                //ads vertical assit and applies the clamp/limiter
                                impulse.setY(impulse.getY() + up);
                                grappler.setVelocity(capVelocity(vv.add(impulse), CAP_HORIZ, CAP_UP));
                            }

                            // slight settle near end to avoid big bounce back
                            if (dist < 1.2) grappler.setVelocity(grappler.getVelocity().multiply(0.985));

                            grappler.setFallDistance(0f);
                            if (++ticks >= MAX_TICKS) { finish(); }
                        }

                        void finish() {
                            cancel();
                            PlayerData pd = crystalized_essentials.getInstance().getPlayerData(grappler.getName());
                            if (pd != null) Bukkit.getScheduler().runTaskLater(crystalized_essentials.getInstance(),
                                    () -> pd.isUsingGrapplingOrb = false, 10);
                        }
                    }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

                    e.getEntity().remove();
                    return;
                }

                //Old graple by Callum below
                /*Old graple Code By Callum

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

					public void run() {
						timer++;

						if (timer == 5) {
							timer = 0;
							if (timesGrappled == 16 || timesGrappled > 16 || p.isSneaking() || p.getGameMode().equals(GameMode.SPECTATOR)) {
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
			*/

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


    /*-----HERE BEGINS THE METHODS ADDED TO HELP WITH THE NEW GRAPLING LOGIC-----*/

    // Temporarily disables collision between two players (safe, auto-restore)
    private void beginNoCollision(Player a, Player b, int ticks) {
        try {
            // Paper/Spigot have per-entity collisions
            a.setCollidable(false);
            b.setCollidable(false);
        } catch (Throwable ignored) { /*
        TODO NOTE FOR THE FUTURE: if the server builds lacks this, comment out and use teams instead for best results */ }

        Bukkit.getScheduler().runTaskLater(crystalized_essentials.getInstance(), () -> {
            try { a.setCollidable(true); } catch (Throwable ignored) {}
            try { b.setCollidable(true); } catch (Throwable ignored) {}
        }, ticks);
    }
    // flings the victim forward+up, with a short particle trail to show the force
    private void flingVictim(Player victim,
                             Vector dir,              // vector: victim to grappler/the player owner
                             double minForward,
                             double extraForward,
                             double upPop,
                             double capHoriz,
                             double capUp,
                             int    trailTicks) {
        Vector vv = victim.getVelocity();  // keep current momentum for continuity
        double forward = Math.max(vv.dot(dir), minForward) + extraForward;   // desired forward speed along rope: at least minForward
            // + shove so they pass through the grapler
        Vector out = dir.multiply(forward);  // builds the launch vector in the rope direction
        out.setY(Math.max(vv.getY(), 0) + upPop); // adds upward pop: never removes existing upward speed

        victim.setVelocity(capVelocity(out, capHoriz, capUp)); // applys with the safety caps (horizontal/up) to avoid watchdog/stalls
        victim.setFallDistance(0f); // prevents fall damage from this forced launch

        // quick trail
        final World w = victim.getWorld();
        new BukkitRunnable() {
            int left = trailTicks;
            @Override public void run() {
                if (!victim.isValid()) { cancel(); return; }
                w.spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1.0, 0), 6, 0.15, 0.15, 0.15, 0.0);
                if (--left <= 0) cancel();
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }

    //This method is used to cap velocity
    // clamps horiz / upward so it don't trip watchdog
    private Vector capVelocity(Vector v, double maxHoriz, double maxUp) {
        double y = Math.min(v.getY(), maxUp);
        Vector h = v.clone(); h.setY(0);
        double hl = h.length();
        if (hl > maxHoriz && hl > 0) h.multiply(maxHoriz / hl);
        return new Vector(h.getX(), y, h.getZ());
    }

    //This is used to do the fling trail
    private void startFlingTrail(Player victim, int ticks) {
        new BukkitRunnable() {
            int t = ticks;
            @Override public void run() {
                if (!victim.isValid() || victim.isDead() || t-- <= 0) { cancel(); return; }
                Location p = victim.getLocation().add(0, 1.0, 0);
                victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, p, 1, 0.0, 0.0, 0.0, 0.0);
                victim.getWorld().spawnParticle(Particle.CRIT, p, 2, 0.12, 0.12, 0.12, 0.0);
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }



    // one-shot impulse used ONLY for mid-air cancel
    private void applyCancelThrust(Player grappler,
                                   Vector dir,           // vector from player to hook
                                   double minFwd,
                                   double extraFwd,
                                   double baseUp,
                                   double capHoriz,
                                   double capUp) {
        Vector v = grappler.getVelocity();         // current momentum whic is preserved
        double along = v.dot(dir); // current forward speed along dir
        double need  = Math.max(0.0, minFwd - along); // how much is missing to reach minFwd (0 if already fast enough)

        Vector impulse = dir.multiply(need + extraFwd); // forward impulse = "catch up to min" + extra shove

        double up = baseUp; // upward pop baseline

        // if falling, adds a bit more so cancel doesn't feels like a dead drop
        if (v.getY() < 0) up += Math.min(0.35, -v.getY() * 0.6);
        impulse.setY(impulse.getY() + up);  // combines up pop with forward impulse

        Vector out = capVelocity(v.add(impulse), capHoriz, capUp); // adds impulse to existing velocity, clamp (horizontal / upward) for stability
        grappler.setVelocity(out);
        //Sets fall demage to zero
        grappler.setFallDistance(0f);
    }
    /*-----HERE ENDS THE METHODS ADDED TO HELP WITH THE NEW GRAPLING LOGIC-----*/



}

