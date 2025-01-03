package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

import static org.bukkit.Color.*;
import static org.bukkit.Particle.*;

public class CustomBows implements Listener {
	public HashMap<Projectile, ArrowData> arrows = new HashMap<>();
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

		ItemMeta meta = stack.getItemMeta();

		if (meta == null || !meta.hasCustomModelData()) {
			return;
		}

		int model = meta.getCustomModelData();
		ArrowData.bowType type = null;
		// note: model data of silver bow
		if (stack.getType() == Material.BOW && model == 1) {
			type = ArrowData.bowType.marksman;
		} else if (stack.getType() == Material.BOW && model == 3) {
			type = ArrowData.bowType.ricochet;
		} else{
			type = ArrowData.bowType.normal;
		}

		if (type == null) {
			return;
		}

		ArrowData ard = new ArrowData(e, event.getForce(), event.getHand(), type, 0, startParticleTrail((Projectile) event.getProjectile(), type));
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

		if (data.type == ArrowData.bowType.marksman) {
			LivingEntity e = (LivingEntity) event.getHitEntity();

			if (e == null) {
				return;
			}

			Location shooterLoc = data.shooter.getLocation();
			Location hitLoc = e.getLocation();
			double distance = Math.floor(shooterLoc.distance(hitLoc) / 10);
			double damage = ar.getDamage();
			damage = damage + distance * 0.5;
			e.damage(damage, pro);

		} else if (data.type == ArrowData.bowType.ricochet) {
			if (event.getHitBlock() == null) {
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
			arrow.setShooter(pro.getShooter());
			arrows.remove(event.getEntity());
			event.getEntity().remove();
			// configure the new arrow (fire, pierce, etc)
			arrows.put(arrow, data);
			return;
		}
		if(data.TaskID != null) {
			stopParticleTrail(data);
		}
	}

	public Integer startParticleTrail(Projectile pro, ArrowData.bowType type){
		if(type != ArrowData.bowType.normal) {
			BukkitTask buk = new BukkitRunnable() {
				public void run() {
					Location loc = pro.getLocation();
					ParticleBuilder builder = new ParticleBuilder(DUST);
					if (type == ArrowData.bowType.marksman) {
						builder.color(ORANGE);
					} else if (type == ArrowData.bowType.ricochet) {
						builder.color(LIME);
					}
					builder.location(loc);
					builder.count(3);
					builder.offset(0, 0, 0);
					builder.spawn();
				}
			}.runTaskTimerAsynchronously(crystalized_essentials.getInstance(), 1, 1);

			int id = buk.getTaskId();
			bukkitRunnable.put(id, buk);
			return id;
		}
		return null;
	}

	public void stopParticleTrail(ArrowData data){
		BukkitTask task = bukkitRunnable.get(data.TaskID);
		task.cancel();
	}
}
