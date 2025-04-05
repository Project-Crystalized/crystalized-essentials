package gg.crystalized.essentials;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.N;

import static net.kyori.adventure.text.Component.text;

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
		ItemStack held_item = p.getInventory().getItemInMainHand();
		if (!e.getAction().isRightClick()) {return;}
		if (!held_item.hasItemMeta()) {return;} //should return if nothing in hand
		if (held_item.getItemMeta().hasItemModel()) {
			NamespacedKey item_model = held_item.getItemMeta().getItemModel();

			if (item_model.equals(new NamespacedKey("crystalized", "breeze_dagger"))) {
				NamespacedKey key = new NamespacedKey("namespace", "key"); //TODO LadyCat please make better key names than just "namespace:key", This will conflict and get confusing if we add more values to the dagger - Callum
				NamespacedKey rechargeKey = new NamespacedKey("crystalized", "bd_recharge");
				PersistentDataContainer pdc = held_item.getItemMeta().getPersistentDataContainer();
				boolean recharge = false;
				if (pdc.has(rechargeKey)) {
					recharge = pdc.get(rechargeKey, PersistentDataType.BOOLEAN); //If this causes a NPE its not my problem
				}
				//This is ugly and possibly confusing
                if (!recharge) {pd.BreezeDaggerDisableRecharge = true;} else {pd.BreezeDaggerDisableRecharge = false;}

				p.sendMessage(text("" + pd.BreezeDaggerDisableRecharge));
				Integer dashes = held_item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
				int d = 0 + dashes; //Hopefully fix a NPE
				pd.BreezeDaggerDashes = d;
				if (dashes > 0) {
					double y = 0;
					if (!p.isOnGround()) { //Fixes a bug where dashing while on ground lifts you up in the air slightly
						y = 0.60;
					} else {
						y = p.getVelocity().getY();
					}
					p.setVelocity(new Vector(
							p.getLocation().getDirection().getX() * 1.05,
							y,
							p.getLocation().getDirection().getZ() * 1.05)
					);
					for (Player every : Bukkit.getOnlinePlayers()) {
						every.playSound(p, "minecraft:item.armor.equip_elytra", 50, 1); //TODO placeholder sound. Breeze Dagger use
					}
					dashes--;
					ItemMeta meta = held_item.getItemMeta();
					meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, dashes);
					held_item.setItemMeta(meta);
					pd.UseBreezeDaggerDash();
					if (!recharge) {return;}
					new BukkitRunnable() {
						public void run() {
							if (pd.BreezeDaggerDashes != pd.BreezeDaggerDefaultDashes) {
								if (pd.BreezeDaggerDashes < pd.BreezeDaggerDefaultDashes) {
									if (p.getCooldown(Material.STONE_SWORD) == 0) {
										pd.BreezeDaggerDashes++;
										pdc.set(key, PersistentDataType.INTEGER, pd.BreezeDaggerDashes);

										p.playSound(p,"minecraft:entity.experience_orb.pickup", 50, 1); //TODO placeholder sound, breeze dagger recharge dash
										if (pd.BreezeDaggerDashes != pd.BreezeDaggerDefaultDashes) {
											p.setCooldown(Material.STONE_SWORD, pd.BreezeDaggerDefaultCooldown);
										}
									}
								}
							}
						}
					}.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
				}
			}
		}
	}
}
