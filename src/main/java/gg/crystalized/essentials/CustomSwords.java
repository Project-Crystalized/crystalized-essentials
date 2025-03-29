package gg.crystalized.essentials;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
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

			if (item_model.equals(new NamespacedKey("crystalized", "underdog_sword"))) {
				int item_custom_model= held_item.getItemMeta().getCustomModelData();
				if (item_custom_model == 1) {
					Bukkit.getLogger().severe("1");
					e.setDamage(e.getDamage() + 1);
				} else if (item_custom_model == 2) {
					Bukkit.getLogger().severe("2");
					e.setDamage(e.getDamage() + 2);
				} else if (item_custom_model == 3) {
					Bukkit.getLogger().severe("3");
					e.setDamage(e.getDamage() + 3);
				} else if (item_custom_model == 4) {
					Bukkit.getLogger().severe("4");
					e.setDamage(e.getDamage() + 4);
				}
			}
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if(e.getHand() == EquipmentSlot.OFF_HAND){
			return;
		}
		Player p = e.getPlayer();
		PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
		pd.BreezeDaggerDisableRecharge = true;
		ItemStack held_item = p.getInventory().getItemInMainHand();
		if (!e.getAction().isRightClick()) {return;}
		if (!held_item.hasItemMeta()) {return;} //should return if nothing in hand
		if (held_item.getItemMeta().hasItemModel()) {
			NamespacedKey item_model = held_item.getItemMeta().getItemModel();
			if (item_model.equals(new NamespacedKey("crystalized", "breeze_dagger"))) {
				NamespacedKey key = new NamespacedKey("namespace", "key");
				Integer dashes = held_item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
				pd.BreezeDaggerDashes = dashes;
				if (dashes > 0) {
					double y = 0;
					if (!p.isOnGround()) { //Fixes a bug where dashing while on ground lifts you up in the air slightly
						y = 0.60;
					} else {
						y = p.getVelocity().getY();
					}
					p.setVelocity(new Vector(
							p.getLocation().getDirection().getX() * 1.05, //0.60
							y,
							p.getLocation().getDirection().getZ() * 1.05)
					);
					//p.setVelocity(p.getLocation().getDirection().multiply(1.05));
					for (Player every : Bukkit.getOnlinePlayers()) {
						every.playSound(p, "minecraft:item.armor.equip_elytra", 50, 1); //TODO placeholder sound. Breeze Dagger use
					}
					 //Might be messy most of the code being in pd but ehh, I cant think of a better way of doing it since I cant just put it in this class
					dashes--;
					ItemMeta meta = held_item.getItemMeta();
					meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, dashes);
					held_item.setItemMeta(meta);
					pd.UseBreezeDaggerDash();
				}
			}
		}
	}
}
