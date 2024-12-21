package gg.crystalized.essentials;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public class ArrowData {

    enum bowType{
        marksman,
        ricochet
    }

    LivingEntity shooter;
    float force;
    EquipmentSlot hand;
    bowType type;

    public ArrowData(LivingEntity shooter, float force, EquipmentSlot hand, bowType type){
        this.shooter = shooter;
        this.force = force;
        this.hand = hand;
        this.type = type;
    }
}
