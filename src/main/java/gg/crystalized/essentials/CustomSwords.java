package gg.crystalized.essentials;

import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CustomSwords implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onLeftClick(EntityDamageByEntityEvent e) {
		if (e.isCancelled() || !(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
			return;
		}
		ItemStack held_item = ((Player) e.getDamager()).getInventory().getItemInMainHand();
		if (held_item.getType().equals(Material.STONE_SWORD) && held_item.getItemMeta().hasCustomModelData()) {
			int item_custom_model = held_item.getItemMeta().getCustomModelData();
			if (item_custom_model == 1) {
				// Slime Sword
			((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 4 * 20, 0));
			} else if (item_custom_model == 2) {
				// Pufferfish Sword
			((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 3 * 20, 0));

			} else if (item_custom_model == 4){
				e.setDamage(e.getDamage()+1);
			} else if (item_custom_model == 5){
				e.setDamage(e.getDamage()+2);
			} else if (item_custom_model == 6){
				e.setDamage(e.getDamage()+3);
			} else if (item_custom_model == 7){
				e.setDamage(e.getDamage()+4);
			}
		}
	}
}
