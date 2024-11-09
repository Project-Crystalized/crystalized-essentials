package gg.crystalized.essentials;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomSwords implements Listener {

    @EventHandler
    public void onLeftClick(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();

            if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD) && player.getEquipment().getItemInMainHand().getItemMeta().hasCustomModelData() == true) {
                if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 1) {
                    PotionEffect potion = new PotionEffect(PotionEffectType.SLOWNESS, 4*20, 2);
                    potion.apply((LivingEntity) event.getAttacked());
                } else if (player.getEquipment().getItemInMainHand().getItemMeta().getCustomModelData() == 2) {
                    PotionEffect potion = new PotionEffect(PotionEffectType.POISON, 4*20, 3);
                    potion.apply((LivingEntity) event.getAttacked());
                }
            }
        }
    }

