package gg.crystalized.essentials;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    @EventHandler
    public void onInventoryMove(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {return;}
        if (e.getCurrentItem().equals(crystalized_essentials.getInstance().WingedOrbElytra)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
				if (!(e.getEntity() instanceof Player)) {
						return;
				}
        Player p = (Player) e.getEntity();
        PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
        if ((e.getCause().equals(DamageType.FALL) || p.isOnGround()) && pd.isUsingBreezeDagger) { // p.isOnGround() is vulnerable to bug out with people using hacked clients, not my problem tho
            e.setCancelled(true);
            pd.isUsingBreezeDagger = false;
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerConnectionCloseEvent e) {
        crystalized_essentials.getInstance().DisconnectPlayerToList(e.getPlayerName());
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        if (e.getEntity() instanceof ArmorStand en) {
            ItemStack item = en.getEquipment().getHelmet();
            if (item != null) {
                if (!item.hasItemMeta()) {return;}
                if (!item.getItemMeta().hasItemModel()) {return;}
                NamespacedKey name = item.getItemMeta().getItemModel();
                if (name.equals(new NamespacedKey("crystalized", "models/antiair_totem")) || name.equals(new NamespacedKey("crystalized", "models/antiair_totem_turret"))) {
                    crystalized_essentials.getInstance().getAntiAirTotemByEntity(en).hit((Player) e.getDamager());
                }
            }
        }
    }
}
