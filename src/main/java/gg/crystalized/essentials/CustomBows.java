package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import static org.bukkit.Material.AIR;
import static org.bukkit.damage.DamageType.ARROW;

public class CustomBows implements Listener {
	public static HashMap<Projectile, ArrowData> arrows = new HashMap<>();

	@EventHandler(priority = EventPriority.HIGH)
	public void onBowShot(EntityShootBowEvent event) {
		if (event.isCancelled()) {
			return;
		}
		ItemStack bow_item = event.getBow();

		if (bow_item == null) {
			return;
		}

		ArrowData.arrowType arrType = get_arrow_type(event.getConsumable());
		if (arrType == ArrowData.arrowType.explosive) {
			((Player) event.getEntity()).setCooldown(bow_item, 20 * 3);
		}

		ArrowData.bowType type = get_bow_type(bow_item);
		if (type == ArrowData.bowType.charged) {
			event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(4));
			((Player) event.getEntity()).setCooldown(bow_item, 20 * 5);
			chargedParticleTrail((Projectile) event.getProjectile());
		}

		ArrowData ard = new ArrowData(event.getEntity(), type, arrType, 0);
		arrows.put((Projectile) event.getProjectile(), ard);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Projectile)){
			return;
		}

		ArrowData data = arrows.get((Projectile)e.getDamager());
		if (data == null) {
			return;
		}

		// deal extra damge for marksman
		if (data.type == ArrowData.bowType.marksman) {
			Location shooterLoc = data.shooter.getLocation();
			Location hitLoc = e.getEntity().getLocation();
			double distance = Math.floor(shooterLoc.distance(hitLoc) / 10);
			((LivingEntity) e.getEntity()).damage(distance);

		}else if(data.type == ArrowData.bowType.charged){
			e.setCancelled(true);
			Location eloc = e.getEntity().getLocation();
			Location arrloc = e.getDamager().getLocation();
			if (arrloc.getY() - eloc.getY() >= 1.7 && arrloc.getY() - eloc.getY() <= 2) {
				((LivingEntity) e.getEntity()).damage(10);
			}else{
				((LivingEntity) e.getEntity()).damage(6);
			}
			e.getDamager().getLocation().getWorld().playSound(e.getDamager().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
			e.getDamager().remove();

		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onArrowHit(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Arrow) || !(event.getEntity() instanceof SpectralArrow)) {
			return;
		}

		Arrow ar = (Arrow) event.getEntity();
		ArrowData data = arrows.get(event.getEntity());

		if (data == null) {
			return;
		}

		else if (data.type == ArrowData.bowType.ricochet) {
			if (ar.isInBlock()) {
				return;
			}
			Location loc = event.getEntity().getLocation();
			Vector velocity = event.getEntity().getVelocity();
			loc.subtract(velocity);
			velocity.multiply(0.5);

			if (data.timesBounced >= 3) {
				CustomArrows.onArrowHit(event);
				return;
			}
			event.setCancelled(true);
			BlockFace face = event.getHitBlockFace();
			if (face == BlockFace.UP || face == BlockFace.DOWN) {
				velocity.setY(-velocity.getY());
			} else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
				velocity.setZ(-velocity.getZ());
			} else if (face == BlockFace.EAST || face == BlockFace.WEST) {
				velocity.setX(-velocity.getX());
			}

			data.timesBounced++;
			Arrow arrow = event.getEntity().getWorld().spawnArrow(loc, velocity, (float) velocity.length(), 1);
			arrow.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
			arrow.setShooter(event.getEntity().getShooter());
			arrows.remove(event.getEntity());
			event.getEntity().remove();
			arrows.put(arrow, data);
			return;
		}
		CustomArrows.onArrowHit(event);
	}

	public ArrowData.arrowType get_arrow_type(ItemStack item) {
		ItemMeta arrowMeta = item.getItemMeta();
		if (arrowMeta == null) {
			return ArrowData.arrowType.normal;
		}
		if (arrowMeta.hasCustomModelData() && arrowMeta.getCustomModelData() == 2) {
			return ArrowData.arrowType.explosive;
		} else if (arrowMeta.hasCustomModelData() && arrowMeta.getCustomModelData() == 1) {
			return ArrowData.arrowType.dragon;
		} else if (item.getType() == Material.SPECTRAL_ARROW) {
			return ArrowData.arrowType.spectral;
		} else {
			return ArrowData.arrowType.normal;
		}
	}

	public ArrowData.bowType get_bow_type(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.hasCustomModelData()) {
			return ArrowData.bowType.normal;
		} else if (item.getType() == Material.BOW && meta.getCustomModelData() == 1) {
			return ArrowData.bowType.marksman;
		} else if (item.getType() == Material.BOW && meta.getCustomModelData() == 3) {
			return ArrowData.bowType.ricochet;
		} else if (item.getType() == Material.CROSSBOW && meta.getCustomModelData() == 3) {
			return ArrowData.bowType.charged;
		} else {
			return ArrowData.bowType.normal;
		}
	}

	@EventHandler
	public void onArrowPickup(PlayerPickupArrowEvent event) {
		ItemMeta meta = event.getArrow().getItemStack().getItemMeta();
		if (meta != null && meta.hasCustomModelData()) {
			if (meta.getCustomModelData() == 2) {
				event.setCancelled(true);
			}
		}
	}

	public void chargedParticleTrail(Projectile pro) {
		LivingEntity shooter = (LivingEntity) pro.getShooter();
		if (shooter == null) {
			return;
		}
		Location loc = pro.getLocation();
		Vector v = pro.getVelocity().normalize();
		double t = 0;
		Material material = loc.getBlock().getType();

		ParticleBuilder builder = new ParticleBuilder(Particle.SOUL_FIRE_FLAME);
		builder.count(5);
		builder.offset(0, 0, 0);
		builder.extra(0);

		Collection<LivingEntity> collect = loc.getNearbyLivingEntities(1);
		while ((collect.isEmpty() || (collect.size() == 1 && collect.contains(shooter))) && material == AIR && t <= 10) {
			builder.location(loc);
			builder.spawn();
			loc = new Location(loc.getWorld(), lineEquation(loc.getX(), t, v.getX()), lineEquation(loc.getY(), t, v.getY()),
					lineEquation(loc.getZ(), t, v.getZ()));
			t = t + 0.1;
		}
	}

	public double lineEquation(double g, double t, double v) {
		return g + (t * v);
	}
}
