package gg.crystalized.essentials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import java.util.HashMap;

public class CustomBows implements Listener {
	public HashMap<Projectile, ArrowData> arrows = new HashMap<>();

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
		}

		if (type == null) {
			return;
		}

		ArrowData ard = new ArrowData(e, event.getForce(), event.getHand(), type, event.getProjectile().getVelocity(), 0);
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

			if (data.timesBounced > 3) {
				return;
			}
			event.setCancelled(true);
			BlockFace face = event.getHitBlockFace();
			if (face == BlockFace.UP || face == BlockFace.DOWN) {
				loc.setY(loc.getY() + 0.4);
				velocity.setY(-velocity.getY());
			} else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
				loc.setZ(loc.getZ() + 0.4);
				velocity.setZ(-velocity.getZ());
			} else if (face == BlockFace.EAST || face == BlockFace.WEST) {
				loc.setX(loc.getX() + 0.4);
				velocity.setX(-velocity.getX());
			}
			data.timesBounced++;
			Arrow arrow = event.getEntity().getWorld().spawnArrow(loc, velocity, 1, 1);
			if (arrow.isInBlock()) {
				return;
			}
			arrows.remove(event.getEntity());
			// configure the new arrow (fire, pierce, etc)
			event.getEntity().remove();
			arrows.put(arrow, data);
		}
	}
}
