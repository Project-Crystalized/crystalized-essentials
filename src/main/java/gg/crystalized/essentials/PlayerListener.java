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
import org.bukkit.event.player.PlayerJoinEvent;
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
    public void onPlayerConnect(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        crystalized_essentials.getInstance().addPlayerToList(p);

        p.discoverRecipe(new NamespacedKey("crystalized", "pufferfish_sword"));
        p.discoverRecipe(new NamespacedKey("crystalized", "slime_sword"));
        p.discoverRecipe(new NamespacedKey("crystalized", "charged_crossbow"));
        p.discoverRecipe(new NamespacedKey("crystalized", "marksman_bow"));
        p.discoverRecipe(new NamespacedKey("crystalized", "ricochet_bow"));
        p.discoverRecipe(new NamespacedKey("crystalized", "explosive_bow"));
        p.discoverRecipe(new NamespacedKey("crystalized", "dragon_arrow"));
        p.discoverRecipe(new NamespacedKey("crystalized", "explosive_arrow"));
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

                //might cause NPE, not my problem if people place fake totem models - callum
                if (name.equals(new NamespacedKey("crystalized", "models/antiair_totem")) || name.equals(new NamespacedKey("crystalized", "models/antiair_totem_turret"))) {
                    crystalized_essentials.getInstance().getAntiAirTotemByEntity(en).hit((Player) e.getDamager());
                } else if (name.equals(new NamespacedKey("crystalized", "models/defence_totem"))) {
                    crystalized_essentials.getInstance().getDefenceTotemByEntity(en).hit((Player) e.getDamager());
                }
            }
        }
    }
}
