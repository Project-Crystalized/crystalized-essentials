package gg.crystalized.essentials;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public class ArrowData {

	enum bowType {
		marksman,
		ricochet,
		normal
	}

	enum arrowType {
		dragon,
		explosive,
		spectral,
		normal
	}

	LivingEntity shooter;
	float force;
	EquipmentSlot hand;
	bowType type;
	arrowType arrType;
	int timesBounced;
	Integer TaskID;

	public ArrowData(LivingEntity shooter, float force, EquipmentSlot hand, bowType type, arrowType arrType, int timesBounced, Integer TaskID) {
		this.shooter = shooter;
		this.force = force;
		this.hand = hand;
		this.type = type;
		this.timesBounced = timesBounced;
		this.TaskID = TaskID;
		this.arrType = arrType;
	}
}
