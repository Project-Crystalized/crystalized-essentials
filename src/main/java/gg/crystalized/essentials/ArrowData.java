package gg.crystalized.essentials;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class ArrowData {

    enum bowType{
        marksman,
        ricochet
    }

    LivingEntity shooter;
    float force;
    EquipmentSlot hand;
    bowType type;
    Vector velocity;
    int timesBounced;

    public ArrowData(LivingEntity shooter, float force, EquipmentSlot hand, bowType type, Vector velocity, int timesBounced){
        this.shooter = shooter;
        this.force = force;
        this.hand = hand;
        this.type = type;
        this.velocity = velocity;
        this.timesBounced = timesBounced;
    }
}
