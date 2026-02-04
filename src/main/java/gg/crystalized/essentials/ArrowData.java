package gg.crystalized.essentials;

import org.bukkit.Bukkit;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Color.*;
import static org.bukkit.Particle.*;

import com.destroystokyo.paper.ParticleBuilder;

public class ArrowData {

	enum bowType {
		marksman,
		ricochet,
		charged,
		normal,
		normalCrossbow,
		explosive,
		grapplingBow,
		preciseCrossbow
	}

	enum arrowType {
		dragon,
		explosive,
		spectral,
		normal,
	}

	public LivingEntity shooter;
	public EquipmentSlot hand;
	public bowType type;
	public arrowType arrType;
	public int timesBounced;

	public ArrowData(LivingEntity shooter, bowType type, arrowType arrType, int timesBounced) {
		this.shooter = shooter;
		this.type = type;
		this.timesBounced = timesBounced;
		this.arrType = arrType;
	}

	public static void particle_trails() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (AbstractArrow arrow : Bukkit.getWorld("world").getEntitiesByClass(AbstractArrow.class)) {
					ArrowData arrow_data = CustomBows.arrows.get(arrow);
					if (arrow_data == null) {
						return;
					}

					ParticleBuilder builder = null;
					ParticleBuilder builder2 = null;
					if (arrow_data.type != ArrowData.bowType.normal || arrow_data.type != ArrowData.bowType.charged) {
						if (arrow_data.type == ArrowData.bowType.marksman) {
							builder = new ParticleBuilder(DUST);
							builder.color(ORANGE);
							builder.count(5);
						} else if (arrow_data.type == ArrowData.bowType.ricochet) {
							builder = new ParticleBuilder(DUST);
							builder.color(LIME);
							builder.count(5);
						}
					}
					if (arrow_data.arrType != ArrowData.arrowType.normal && arrow_data.arrType != ArrowData.arrowType.spectral) {
						builder2 = new ParticleBuilder(DUST);
						if (arrow_data.arrType == ArrowData.arrowType.dragon) {
							builder2.color(PURPLE);
						} else if (arrow_data.arrType == ArrowData.arrowType.explosive) {
							builder2.color(RED);
						}
					}

					if (builder != null) {
						builder.location(arrow.getLocation());
						builder.offset(0, 0, 0);
						builder.extra(0);
						builder.spawn();
					}
					if (builder2 != null) {
						builder2.location(arrow.getLocation());
						builder2.count(5);
						builder2.offset(0, 0, 0);
						builder2.spawn();
					}
				}
			}
		}.runTaskTimer(crystalized_essentials.getInstance(), 1, 1);
		// }.runTaskTimerAsynchronously(crystalized_essentials.getInstance(), 0, 1);
	}
}
