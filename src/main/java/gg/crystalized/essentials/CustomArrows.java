package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Color.PURPLE;
import static org.bukkit.Particle.DUST;
import static org.bukkit.Particle.RAID_OMEN;
import static org.bukkit.damage.DamageType.*;
import static org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED;
import static org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD;

public class CustomArrows {

	//Originaly used set to track the UUID of player's who got hit directly by explosive arrow
	//Then I remembered that a person can get shot by 2 explosive arrows, hence removing early and exposive to damage,
	// so had to migrate to Map
		//private static final Set<UUID> STORES_DIRECT_EXPLOSIVE_HITS = new HashSet<>();

	//This is a new implementation using map with UUID and Integer, storing players UUID and amount of exploding arrows in player
	//Removes straight after the explosions happen
	private static final Map<UUID, Integer> PLAYERS_HIT_BY_EXPLOSIVE_ARROW = new HashMap<>();

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


		if (data.arrType.equals(ArrowData.arrowType.dragon)) {
			//Commented to make sure all arrows do the same damage on hit with the new formuala - Mish
			//arrow.setDamage(1);

			ItemStack item = arrow.getItemStack();
			item.setItemMeta(null);
			arrow.setItemStack(item);

			Particle.DustOptions options = new Particle.DustOptions(PURPLE, 1);
			// Configures the cloud before it becomes visible in the world, preventing the weird white particle appering briefly
			AreaEffectCloud cloud = arrow_loc.getWorld().spawn(arrow_loc, AreaEffectCloud.class,
					spawnedCloud -> {
						// The cloud exists only for the damage, so radius is set to zero
						//As particles are being spawned manualy now
						spawnedCloud.setRadius(0.0F);
						//The duration is 170 but should be removed before in the task at 150
						spawnedCloud.setDuration(170);
						//Makes sure the particcles are dust and using the options. Incase any brief particles to not look out of place
						spawnedCloud.setParticle(DUST, options);
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
							//spawns the one calculated particle at is' correct location
							loc.getWorld().spawnParticle(Particle.DUST, particleLocation,
									1,
									0.0,
									0.0,
									0.0,
									0.0,
									options
							);
						}
					}
					//Damage wasn't touched
					Collection<LivingEntity> collect = loc.getNearbyLivingEntities(2, 1);
					for (LivingEntity liv : collect) {
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
				//Get the players unique UUID
				UUID hitPlayerUUID = player.getUniqueId();
				long protectionLastsTicks;
				if (bothUsed) {
					//Only when explosives bow and arrow used together
					protectionLastsTicks= 18L;
				} else {
					//Normal just explosive arrow, or explosive bow
					protectionLastsTicks = 2L;
				}
				//Ads the player UUID to the map, with the value 1. If UUID already exist it sums them together
				//So that is why the method is called merge as it merges them together
				//Integer::sum: sums up the new and old value
				//So in this case, it will be on first hit (0+1) = 1, on second hit (1+1) = 2, on third (2+1) = 3
				PLAYERS_HIT_BY_EXPLOSIVE_ARROW.merge(hitPlayerUUID, 1, Integer::sum);
				//Sumons the explosion - same as before
				exploArrowExplosion(arrow_loc, source, bothUsed);
				//Scheduling the task to be after protection ticks expired
				Bukkit.getScheduler().runTaskLater(
						crystalized_essentials.getInstance(),
						//If UUID exists in the map then it computes this
						() -> PLAYERS_HIT_BY_EXPLOSIVE_ARROW.computeIfPresent(
								//pases om the player's UUID
								hitPlayerUUID,
								(uuid, arrowsHitThePlayer) -> {
									//If smaller or equal to 1 than player is removed from the map
									if (arrowsHitThePlayer <= 1) {
										return null;
									}
									//If is bigger than 1 than removes 1
									//It should run again because the second arrow has hit and scheduled this task
									return arrowsHitThePlayer - 1;
								}
						),
						//The delay is depeneded if there is a double explosion or not
						protectionLastsTicks
				);
				//Arrow removed same as before

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
		else {
			AbstractArrow arr = (AbstractArrow) event.getEntity();
			//Commented to make sure all arrows do the same damage on hit with the new formuala - Mish
			//arr.setDamage(1.5);
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
		return PLAYERS_HIT_BY_EXPLOSIVE_ARROW.containsKey(uuid);
	}
}
