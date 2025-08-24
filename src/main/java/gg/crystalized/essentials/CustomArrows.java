package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

import static org.bukkit.Color.PURPLE;
import static org.bukkit.Particle.DUST;
import static org.bukkit.Particle.RAID_OMEN;
import static org.bukkit.damage.DamageType.*;
import static org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED;
import static org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD;

public class CustomArrows {

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
			spec.setDamage(spec.getDamage() - 2);
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


		if (data.arrType == ArrowData.arrowType.dragon) {
			arrow.setDamage(1);

			ItemStack item = arrow.getItemStack();
			item.setItemMeta(null);
			arrow.setItemStack(item);

			Particle.DustOptions options = new Particle.DustOptions(PURPLE, 1);
			AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity().getWorld().spawnEntity(arrow_loc, AREA_EFFECT_CLOUD, false);
			cloud.setColor(PURPLE);
			cloud.setParticle(DUST, options);

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
					}
					Collection<LivingEntity> collect = loc.getNearbyLivingEntities(2, 1);
					for (LivingEntity liv : collect) {
						liv.damage(1, source);
					}
					i++;
				}
			}.runTaskTimer(crystalized_essentials.getInstance(), 1, 15);

		}
		if (data.arrType == ArrowData.arrowType.explosive || data.type.equals(ArrowData.bowType.explosive)) {

			//Messy
			int i = 0;
			if (data.arrType == ArrowData.arrowType.explosive) {i++;}
			if (data.type.equals(ArrowData.bowType.explosive)) {i++;}
			boolean bothUsed = false;
			if (i == 2) {bothUsed = true;}

			arrow.setPickupStatus(DISALLOWED);
			arrow.setDamage(2);

			DamageSource.Builder builder = DamageSource.builder(EXPLOSION);
			builder.withCausingEntity(data.shooter);
			builder.withDirectEntity(arrow);
			builder.withDamageLocation(arrow.getLocation());
			DamageSource source = builder.build();

			Entity hit_player = event.getHitEntity();
			if (hit_player != null) {
				exploArrowExplosion(arrow_loc, source, bothUsed);
				arrow.remove();
				return;
			}
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
		} else {
			AbstractArrow arr = (AbstractArrow) event.getEntity();
			arr.setDamage(1.5);
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
}
