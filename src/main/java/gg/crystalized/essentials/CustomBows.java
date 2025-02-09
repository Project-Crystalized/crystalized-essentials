package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static org.bukkit.Color.*;
import static org.bukkit.Material.AIR;
import static org.bukkit.Particle.*;
import static org.bukkit.Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
import static org.bukkit.entity.EntityType.ARROW;
import static org.bukkit.entity.EntityType.SPECTRAL_ARROW;

public class CustomBows implements Listener {
	public static HashMap<Projectile, ArrowData> arrows = new HashMap<>();
	public HashMap<Integer, BukkitTask> bukkitRunnable = new HashMap<>();

	@EventHandler
	public void onBowShot(EntityShootBowEvent event) {
		if (event.isCancelled()) {
			return;
		}
		LivingEntity e = event.getEntity();
		ItemStack stack = event.getBow();

		if (stack == null) {
			return;
		}

		ItemStack arrowItem = event.getArrowItem();
		ItemMeta arrowMeta;
		if(arrowItem.hasItemMeta()){
			arrowMeta = arrowItem.getItemMeta();
		}else{
			arrowMeta = null;
		}

		HumanEntity human = (HumanEntity) e;
		ArrowData.arrowType arrType = null;

		if(arrowMeta != null && arrowMeta.hasCustomModelData() && arrowMeta.getCustomModelData() == 2){
			arrType = ArrowData.arrowType.explosive;
			human.setCooldown(arrowItem, 20*3);
		}else if(arrowMeta != null && arrowMeta.hasCustomModelData() && arrowMeta.getCustomModelData() == 1){
			arrType = ArrowData.arrowType.dragon;
		}else if(e.getType() == SPECTRAL_ARROW){
			arrType = ArrowData.arrowType.spectral;
		}
		else{
			arrType = ArrowData.arrowType.normal;
		}

		ItemMeta meta = stack.getItemMeta();
		ArrowData.bowType type = null;

		if (meta == null || !meta.hasCustomModelData()) {
			type = ArrowData.bowType.normal;
		} else if (stack.getType() == Material.BOW && meta.getCustomModelData() == 1) {
			type = ArrowData.bowType.marksman;
		} else if (stack.getType() == Material.BOW && meta.getCustomModelData() == 3) {
			type = ArrowData.bowType.ricochet;
		} else if (stack.getType() == Material.CROSSBOW && meta.getCustomModelData() == 3) {
			type = ArrowData.bowType.charged;
			event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(2));
			human.getLocation().getWorld().playSound(human.getLocation(), ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
			chargedParticleTrail((Projectile) event.getProjectile());
		} else{
			type = ArrowData.bowType.normal;
		}

		ArrowData ard = new ArrowData(e, event.getForce(), event.getHand(), type, arrType,0, startParticleTrail((Projectile) event.getProjectile(), type, arrType));
		arrows.put((Projectile) event.getProjectile(), ard);
	}

	@EventHandler
	public void onArrowHit(ProjectileHitEvent event) {
		Projectile pro = event.getEntity();

		if (!(pro instanceof Arrow)) {
			return;
		}

		Arrow ar = (Arrow) pro;
		ArrowData data = arrows.get(pro);

		if (data == null) {
			return;
		}
		LivingEntity e = (LivingEntity) event.getHitEntity();
		Vector v = ar.getVelocity();

		if (data.type == ArrowData.bowType.marksman) {

			if (e == null) {
				if(data.TaskID != null) {
					stopParticleTrail(data);
				}
				return;
			}

			Location shooterLoc = data.shooter.getLocation();
			Location hitLoc = e.getLocation();
			double distance = Math.floor(shooterLoc.distance(hitLoc) / 10);
			double damage = ar.getDamage();
			damage = damage + distance * 0.5;
			e.damage(damage, pro);
			e.setVelocity(v.multiply(0.5));
		}else if (data.type == ArrowData.bowType.charged){
			if(event.getHitEntity() == null){
				if(data.TaskID != null) {
					stopParticleTrail(data);
				}
				return;
			}
			Location eloc = event.getHitEntity().getLocation();
			Location arrloc = pro.getLocation();
			if(arrloc.getY() - eloc.getY() >= 1.5 && arrloc.getY() - eloc.getY() <= 2){
				e.damage(2.5, pro);
				e.setVelocity(v.multiply(1/4));
			}else{
				e.setVelocity(v.multiply(1/4));
			}

		} else if (data.type == ArrowData.bowType.ricochet) {
			if (event.getHitBlock() == null) {
				if(data.TaskID != null) {
					stopParticleTrail(data);
				}
				return;
			}
			if (ar.isInBlock()) {
				return;
			}
			Location loc = event.getEntity().getLocation();
			Vector velocity = event.getEntity().getVelocity();
			loc.subtract(velocity);
			velocity.multiply(0.5);

			if (data.timesBounced >= 3) {
				if(data.TaskID != null) {
					stopParticleTrail(data);
				}
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
			ItemStack item = arrow.getItemStack();
			item.setItemMeta(syncArrowMeta((Arrow) event.getEntity(), arrow));
			arrow.setItemStack(item);
			arrow.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
			arrow.setShooter(pro.getShooter());
			arrows.remove(event.getEntity());
			event.getEntity().remove();
			if(data.TaskID != null) {
				stopParticleTrail(data);
			}
			data.TaskID = startParticleTrail(arrow, data.type, data.arrType);
			// configure the new arrow (fire, pierce, etc)
			arrows.put(arrow, data);
			return;
		}
		CustomArrows.onArrowHit(event);
		if(data.TaskID != null) {
			stopParticleTrail(data);
		}
	}

	public Integer startParticleTrail(Projectile pro, ArrowData.bowType type, ArrowData.arrowType arrType){
		BukkitTask buk = new BukkitRunnable() {
			public void run() {
				Location loc = pro.getLocation();
				ParticleBuilder builder = null;
				ParticleBuilder builder2 = null;
				if(type != ArrowData.bowType.normal || type != ArrowData.bowType.charged) {
					if (type == ArrowData.bowType.marksman) {
						builder = new ParticleBuilder(DUST);
						builder.color(ORANGE);
						builder.count(5);
					} else if (type == ArrowData.bowType.ricochet) {
						builder = new ParticleBuilder(DUST);
						builder.color(LIME);
						builder.count(5);
					}
				}
				if(arrType != ArrowData.arrowType.normal && arrType != ArrowData.arrowType.spectral) {
					builder2 = new ParticleBuilder(DUST);
					if (arrType == ArrowData.arrowType.dragon) {
						builder2.color(PURPLE);
					} else if (arrType == ArrowData.arrowType.explosive) {
						builder2.color(RED);
					}
				}

				if(builder == null && builder2 == null){
					return;
				}

				if(builder != null){
					builder.location(loc);
					builder.offset(0, 0, 0);
					builder.extra(0);
					builder.spawn();
				}
				if(builder2 != null){
					builder2.location(loc);
					builder2.count(5);
					builder2.offset(0, 0, 0);
					builder2.spawn();
				}
			}
		}.runTaskTimerAsynchronously(crystalized_essentials.getInstance(), 0, 0);

			int id = buk.getTaskId();
			bukkitRunnable.put(id, buk);
			return id;

	}

	public void stopParticleTrail(ArrowData data){
		if(data.TaskID == null){
			return;
		}
		BukkitTask task = bukkitRunnable.get(data.TaskID);
		task.cancel();
	}

	public ItemMeta syncArrowMeta(Arrow entity, Arrow arrow){
		ItemStack arrowItem = arrow.getItemStack();
		ItemMeta meta = arrowItem.getItemMeta();

		ItemStack entityItem = entity.getItemStack();
		ItemMeta entMeta = entityItem.getItemMeta();
		if(entMeta.hasCustomModelData()){
			meta.setCustomModelData(entMeta.getCustomModelData());
		}
		meta.displayName(entMeta.displayName());
		meta.lore(entMeta.lore());
		return meta;
	}

	public void chargedParticleTrail(Projectile pro){
		LivingEntity shooter = (LivingEntity)pro.getShooter();
		if(shooter == null){
			return;
		}
		Location loc = pro.getLocation();
		Vector v = pro.getVelocity().normalize();
		double t = 0;
		Material material = loc.getBlock().getType();

		ParticleBuilder builder = new ParticleBuilder(SOUL_FIRE_FLAME);
		builder.count(5);
		builder.offset(0, 0, 0);
		builder.extra(0);

		Collection<LivingEntity> collect = loc.getNearbyLivingEntities(1);
		while((collect.isEmpty() || (collect.size() == 1 && collect.contains(shooter))) && material == AIR && t <= 10){
			builder.location(loc);
			builder.spawn();
			loc = new Location(loc.getWorld(), lineEquation(loc.getX(), t, v.getX()), lineEquation(loc.getY(), t, v.getY()), lineEquation(loc.getZ(), t, v.getZ()));
			t = t + 0.1;
		}
	}

	public double lineEquation(double g, double t, double v){
		return g+(t*v);
	}
}
