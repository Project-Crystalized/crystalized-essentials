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
    //This is to clean it up on joining, incase other cleans up failed if server shut down unexpectedly while player was in a circle
    //And joined another mini game etc, so LS couldn't clean it up
    private static final NamespacedKey NEGATIVE_EFFECT_IMMUNITY =
            new NamespacedKey("litestrike", "negative_effect_immunity");
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
        //This makes sure that on connection the player will not have this presistant data. As a fall black clean up
        //If LS wasn't able to clean up it propely, as most of the plugins need it, should be good to keep it here
        p.getPersistentDataContainer().remove(NEGATIVE_EFFECT_IMMUNITY);
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
        CustomArrows.removeExplosiveImmunity(e.getPlayerUniqueId());
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

                //This doesn't work and I have no idea why, test2 doesnt should for either the target or owner - Callum
                /*else if (name.equals(new NamespacedKey("crystalized", "models/knockout_orb"))) {
                    KnockoutOrb ko = crystalized_essentials.getInstance().getKnockoutOrbByEntity(en);
                    if (ko.target.equals(e.getDamager())) {
                        ko.changeTargetAndOwner(ko.owner, ko.target);
                        Bukkit.getServer().sendMessage(text("test2"));
                    } else {
                        Bukkit.getServer().sendMessage(text("test"));
                    }
                }*/
            }
        }
    }
}
