package gg.crystalized.essentials;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class CustomSwords implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onLeftClick(EntityDamageByEntityEvent e) {
		if (e.isCancelled() || !(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
			return;
		}
		ItemStack held_item = ((Player) e.getDamager()).getInventory().getItemInMainHand();
		if (held_item.getType().toString().toLowerCase().contains("sword") && held_item.getItemMeta().hasItemModel()) {

			NamespacedKey item_model = held_item.getItemMeta().getItemModel();

			if (item_model.equals(new NamespacedKey("crystalized", "slime_sword"))) {
				((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 4 * 20, 0));
			} else if (item_model.equals(new NamespacedKey("crystalized", "pufferfish_sword"))) {
				((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 3 * 20, 0));
			}
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
		ItemStack held_item = p.getInventory().getItemInMainHand();
		if (!e.getAction().isRightClick()) {return;}
		if (!held_item.hasItemMeta()) {return;} //should return if nothing in hand
		if (held_item.getItemMeta().hasItemModel()) {
			NamespacedKey item_model = held_item.getItemMeta().getItemModel();
			if (item_model.equals(new NamespacedKey("crystalized", "breeze_dagger"))) {
				if (pd.BreezeDaggerDashes != 0) {
					p.setVelocity(new Vector(p.getVelocity().getX(), p.getVelocity().getY(), p.getVelocity().getZ()));
					p.setVelocity(p.getLocation().getDirection().multiply(1.05));
					for (Player every : Bukkit.getOnlinePlayers()) {
						every.playSound(p, "minecraft:item.armor.equip_elytra", 50, 1); //TODO placeholder sound. Breeze Dagger use
					}
					pd.UseBreezeDaggerDash(); //Might be messy most of the code being in pd but ehh, I cant think of a better way of doing it since I cant just put it in this class

				}
			}
		}
	}
}
