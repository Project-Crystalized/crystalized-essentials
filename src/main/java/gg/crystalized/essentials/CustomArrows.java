package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.*;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Color.PURPLE;
import static org.bukkit.Particle.DUST;
import static org.bukkit.Particle.RAID_OMEN;
import static org.bukkit.damage.DamageType.*;
import static org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED;

public class CustomArrows {

	// the integer is the server tick at which the immunity should end
	// so it is set to getCurrentTick() +2 for example, so this will have hight numbers
	private static final Map<UUID, Integer> EXPLOSIVE_ARROW_IMMUNITY = new HashMap<>();

	//This is the name space key which players have when they are immune to damage, it is added in LS presistant data type
	//So that essentials know when to stop custom effects like puffer fish, or stop dragon arrow damage etc
	private static final NamespacedKey NEGATIVE_EFFECT_IMMUNITY =
			new NamespacedKey("litestrike", "negative_effect_immunity");

	public static void onArrowHit(ProjectileHitEvent event) {
		if (event.isCancelled()) {
			return;
		}
		ArrowData data = CustomBows.arrows.get(event.getEntity());
		if (!(event.getEntity() instanceof AbstractArrow)) {
			return;
		}
		AbstractArrow arrow = (AbstractArrow) event.getEntity();
		Location arrow_loc = event.getEntity().getLocation();

		if (data.arrType == ArrowData.arrowType.spectral) {
			SpectralArrow spec = (SpectralArrow) event.getEntity();
			spec.setGlowingTicks(40);
			/* Commented to make sure all arrows do the same damage on hit with the new formuala - Mish
			if (spec.getDamage() < 0) {
				spec.setDamage(spec.getDamage() - 2);
			}*/
			/*
			ParticleBuilder builder = new ParticleBuilder(DUST);
			builder.color(Color.YELLOW);
			builder.location(arrow_loc);
			builder.count(50);
			builder.offset(3, 3, 3);
			builder.spawn();
			for (Player e : arrow_loc.getNearbyPlayers(3)) {
				e.addPotionEffect(new PotionEffect(GLOWING, 10 * 20, 0, false, false, true));
			}
			 */
			return;
		}
		//All the logic is happening in LS for the supportive arrow, as it needs to know temates etc
		if (data.arrType == ArrowData.arrowType.supportive) {
			return;
		}


		if (data.arrType.equals(ArrowData.arrowType.dragon)) {
			//Commented to make sure all arrows do the same damage on hit with the new formuala - Mish
			//arrow.setDamage(1);

			ItemStack item = arrow.getItemStack();
			item.setItemMeta(null);
			arrow.setItemStack(item);
			//gets the team which own the dragon arrow
			String dragonOwnerTeam = ArrowData.getPlayerTeam(data.shooter);
			//Particle.DustOptions options = new Particle.DustOptions(PURPLE, 1);
			// Configures the cloud before it becomes visible in the world, preventing the weird white particle appering briefly
			AreaEffectCloud cloud = arrow_loc.getWorld().spawn(arrow_loc, AreaEffectCloud.class,
					spawnedCloud -> {
						// The cloud exists only for the damage, so radius is set to zero
						//As particles are being spawned manualy now
						spawnedCloud.setRadius(0.0F);
						//The duration is 170 but should be removed before in the task at 150
						spawnedCloud.setDuration(170);
						//Changed it so no particle is displayed form a ms when the arrow hits the ground,
						//otherwise there would be one dragon particle of purple for a 1ms, which was weird
						spawnedCloud.setParticle(Particle.BLOCK, Material.AIR.createBlockData());
					}
			);
			//Old lagy cloud
			//AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity().getWorld().spawnEntity(arrow_loc, AREA_EFFECT_CLOUD, false);
			//cloud.setRadius(0.0F);
			//cloud.setDuration(150);
			//cloud.setColor(PURPLE);
			//cloud.setParticle(DUST, options);

			//Damage source as it was
			DamageSource.Builder builder = DamageSource.builder(DRAGON_BREATH);
			builder.withCausingEntity(data.shooter);
			builder.withDirectEntity(cloud);
			builder.withDamageLocation(arrow_loc);
			DamageSource source = builder.build();
			new BukkitRunnable() {
				int i = 0;
				final Location loc = event.getEntity().getLocation();

				public void run() {
					if (i >= 10) {
						cloud.remove();
						cancel();
						//Ensures that it stop, so no extra damage
						return;
					}
					// Draws two inner and one outer ring for a circle, so it is easier to see
					//changed to be 3 rings as it made it much easier to see
					//Still much better for perfomance then before
					double[] ringRadius = {1.0, 1.5, 2.0};
					//This so the 20 particles are placed in each of the circle ring.
					int particlePoints = 20;

					//This for loop is for creation of three circles, two inner one outter circle with total of 60 particles
					//Significant decrease compared to the cloud.
					//The particles are synced with damage now creating a nice effect of when it is dealing damage particles are stronger
					for (double radius : ringRadius) {
						//This for loop is for calculating each particle point and placing it in a circle
						for (int particlePoint = 0; particlePoint < particlePoints; particlePoint++) {

							//Calculates the angle in radiant.
							//2*PI radiants is a complete circle
							//Multipliying by the current particle point to sellect it's angle
							//Dividing by particle points to space out evenly the particles around the circle
							double angle = (Math.PI * 2.0 * particlePoint) / particlePoints;
							//Converts the angle into x and z positions, so that they could be used to offset the posion of particle
							double x = Math.cos(angle) * radius;
							double z = Math.sin(angle) * radius;

							//Adds the offset of the x and z to the particles location
							//Adds a small y offset to keep it slightly above the ground
							Location particleLocation = loc.clone().add(x, 0.15, z);
							//goes through all the players in the world and spawns the particle for them depending on the team
							for (Player viewer : loc.getWorld().getPlayers()) {
								//gets the viewers team
								String viewerTeam = viewer.getPersistentDataContainer().get(ArrowData.TEAM_KEY, PersistentDataType.STRING);
								Color colour;

								//depnding on a team assigns a color
								if (dragonOwnerTeam != null) {
									colour = ArrowData.getTeamParticleColour(dragonOwnerTeam, viewerTeam, PURPLE, Color.fromRGB(0, 120, 50));
								} else {
									//The purple fall backl
									colour = PURPLE;
								}

								//spawns the one calculated particle for viewer is' correct location
								viewer.spawnParticle(Particle.DUST, particleLocation,
										1,
										0.0,
										0.0,
										0.0,
										0.0,
										new Particle.DustOptions(colour, 1.0F)
								);
							}
						}
					}
					//Damage wasn't touched
					Collection<LivingEntity> collect = loc.getNearbyLivingEntities(2, 1);
					for (LivingEntity liv : collect) {
						//If the livining enetity has been maked as immune than the damage will not happen
						//This will be when the supporting arrow overlaps with dragon arrow, supporting arrow wins
						if (liv.getPersistentDataContainer().has(NEGATIVE_EFFECT_IMMUNITY)) {
							continue;
						}
						liv.damage(1, source);
					}
					i++;
				}
			}.runTaskTimer(crystalized_essentials.getInstance(), 1, 15);

		}
		else if (data.arrType.equals(ArrowData.arrowType.wind)) {

			for (Player p : Bukkit.getOnlinePlayers()) {
				p.playSound(arrow, "entity.creeper.primed", 2, 1);
			}

			new BukkitRunnable() {
				public void run() {
					WindCharge wC = arrow_loc.getWorld().spawn(arrow_loc, WindCharge.class, entity -> {});
					wC.explode();
					arrow.remove();
					cancel();
				}
			}.runTaskTimer(crystalized_essentials.getInstance(), 20, 1);
		}
		else if (data.arrType.equals(ArrowData.arrowType.explosive) || data.type.equals(ArrowData.bowType.explosive)) {

			//Messy
			int i = 0;
			if (data.arrType == ArrowData.arrowType.explosive) {i++;}
			if (data.type.equals(ArrowData.bowType.explosive)) {i++;}
			boolean bothUsed = false;
			if (i == 2) {bothUsed = true;}

			arrow.setPickupStatus(DISALLOWED);
			//Commented to make sure all arrows do the same damage on hit with the new formuala - Mish
			//arrow.setDamage(2);

			DamageSource.Builder builder = DamageSource.builder(EXPLOSION);
			builder.withCausingEntity(data.shooter);
			builder.withDirectEntity(arrow);
			builder.withDamageLocation(arrow.getLocation());
			DamageSource source = builder.build();


			Entity hitPlayer = event.getHitEntity();
			//Checks specificly if it is a LivingEntity instanse that has been hit
			//Mostly for players but in the future if there are games with mobs I made it work with LivingEntities last minute
		if (hitPlayer instanceof LivingEntity player) {
			UUID hitPlayerUUID = player.getUniqueId();
			recordDirectExplosiveHit(hitPlayerUUID, bothUsed);
			exploArrowExplosion(arrow_loc, source, bothUsed);

			arrow.remove();
			return;
		}
			/*
			Older implementation
			if (hit_player != null) {
				exploArrowExplosion(arrow_loc, source, bothUsed);
				arrow.remove();
				return;
			}*/
			arrow.setGlowing(true);

			boolean bothused1 = bothUsed; //This is dumb
			new BukkitRunnable() {
				int i = 0;

				public void run() {
					if (i >= 3) {
						cancel();
						return;
					}
					arrow_loc.getWorld().spawnParticle(RAID_OMEN, arrow_loc, 3);
					if (bothused1) {
						new BukkitRunnable() {
							public void run() {
								arrow_loc.getWorld().playSound(arrow_loc, "entity.parrot.imitate.creeper", 2f, 1);
								cancel();
							}
						}.runTaskTimer(crystalized_essentials.getInstance(), 10, 1); //This looks ugly imo
					}
					arrow_loc.getWorld().playSound(arrow_loc, "entity.creeper.primed", 2f, 1);
					i++;
				}
			}.runTaskTimer(crystalized_essentials.getInstance(), 0, 20);

			new BukkitRunnable() {
				public void run() {
					exploArrowExplosion(arrow_loc, source, bothused1);
					arrow.remove();
				}
			}.runTaskLater(crystalized_essentials.getInstance(), 3 * 20);
		}
	}


	private static void exploArrowExplosion(Location explo_loc, DamageSource source, Boolean explosiveBowUsed) {
		Collection<LivingEntity> nearby = explo_loc.getNearbyLivingEntities(2);
		Collection<LivingEntity> notSoNearby = explo_loc.getNearbyLivingEntities(4);

		notSoNearby.removeAll(nearby);

		if (explosiveBowUsed) {
			new BukkitRunnable() {
				int timer = 2;
				public void run() {
					switch (timer) {
						case 2, 1 -> {
							explo_loc.createExplosion(source.getCausingEntity(), (float) 1.5, false, false);
							applyExplosiveKnockback(explo_loc);
						}
						case 0 -> {
							cancel();
						}
					}
					timer--;
				}
			}.runTaskTimer(crystalized_essentials.getInstance(), 0, 15);
		} else {
			explo_loc.createExplosion(source.getCausingEntity(), (float) 1.5, false, false);
			applyExplosiveKnockback(explo_loc);
		}

		ParticleBuilder builder = new ParticleBuilder(DUST);
		builder.color(Color.RED);
		builder.offset(1, 1, 1);
		builder.count(300);
		builder.location(explo_loc);
		builder.spawn();
	}
	//This is a method that returns true if the uuid of the player is contained in the map
	//If so then the protection against the explosion damage is activated in CustomBows
	public static boolean isDirectExplosiveHit(UUID uuid) {
		Integer until = EXPLOSIVE_ARROW_IMMUNITY.get(uuid);
		if (until == null) {
			return false;
		}
		if (until < Bukkit.getCurrentTick()) {
			EXPLOSIVE_ARROW_IMMUNITY.remove(uuid);
			return false;
		}
		return true;
	}

	public static void recordDirectExplosiveHit(UUID uuid, boolean bothUsed) {
		EXPLOSIVE_ARROW_IMMUNITY.put(uuid, Bukkit.getCurrentTick() + (bothUsed ? 18 : 2));
	}

	public static void removeExplosiveImmunity(UUID uuid) {
		EXPLOSIVE_ARROW_IMMUNITY.remove(uuid);
	}

	public static Vector explosiveKnockback(Location blastCenter, Location victimEyeLocation) {
		Vector dir = victimEyeLocation.toVector().subtract(blastCenter.toVector());
		if (dir.lengthSquared() > 25.0) {
			return new Vector(0, 0, 0);
		}
		double closeness = 1 - Math.max(dir.length(), 0.5) / 5.0;
		dir.setY(0);
		if (dir.lengthSquared() < 1e-6) {
			return new Vector(0, 1.3 * closeness * closeness, 0);
		}
		Vector vel = dir.normalize().multiply(1.3 * closeness + 0.35);
		vel.setY(1.3 * closeness * closeness);
		return vel;
	}

	private static void applyExplosiveKnockback(Location explo_loc) {
		for (Player p : explo_loc.getNearbyPlayers(5.0)) {
			p.setVelocity(explosiveKnockback(explo_loc, p.getEyeLocation()));
		}
	}
}
