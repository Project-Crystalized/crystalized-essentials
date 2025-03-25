package gg.crystalized.essentials;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class PlayerData {

    public Player player;
    public boolean isUsingWingedOrb = false;
    public boolean isUsingBreezeDagger = false;
    public ItemStack lastChestPlateBeforeWingedOrb = null; //This is to give the player after the Winged Orb ends
    public int BreezeDaggerDashes = 2; //Default value that will change, remember to change if CustomSwords.java#BreezeDaggerMaxDashes changes
    public int BreezeDaggerDefaultDashes = 2; //Should never change. ^
    public int BreezeDaggerDefaultCooldown = 100; //5 seconds

    public PlayerData(Player p) {
        player = p;

        new BukkitRunnable() { //Actionbar Text
            int BDtimer = BreezeDaggerDefaultCooldown;
            public void run() {
                ItemStack MainHandItem = p.getInventory().getItemInMainHand();

                if (MainHandItem.hasItemMeta()) { //prob unsafe this check
                    if (MainHandItem.getItemMeta().hasItemModel()) {
                        if (MainHandItem.getItemMeta().getItemModel().equals(new NamespacedKey("crystalized","breeze_dagger"))) {
                            p.sendActionBar(
                                    translatable("crystalized.sword.wind.name").append(text(" | ")).append(getBreezeDaggerDashes()).append(text(" | " + p.getCooldown(Material.STONE_SWORD)))
                            );
                        }
                    }
                }

                //I hate this, could probably cause visual bugs in LS if you buy a different sword after using a dash after a round ends
                if (BreezeDaggerDashes != BreezeDaggerDefaultDashes) {
                    if (BreezeDaggerDashes < BreezeDaggerDefaultDashes) {
                        if (p.getCooldown(Material.STONE_SWORD) == 0) {
                            BreezeDaggerDashes++;
                            p.playSound(p,"minecraft:entity.experience_orb.pickup", 50, 1); //TODO placeholder sound, breeze dagger recharge dash
                            if (BreezeDaggerDashes != BreezeDaggerDefaultDashes) {
                                p.setCooldown(Material.STONE_SWORD, BreezeDaggerDefaultCooldown);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }

    public void UseBreezeDaggerDash() {
        if (BreezeDaggerDashes == 0) {return;}
        isUsingBreezeDagger = true;
        if (!player.hasCooldown(Material.STONE_SWORD)) {
            player.setCooldown(Material.STONE_SWORD, BreezeDaggerDefaultCooldown);
        }
        BreezeDaggerDashes--;
    }



    private Component getBreezeDaggerDashes() {
        return text("\uE13A".repeat(BreezeDaggerDashes)).append(text("\uE13B".repeat(2 - BreezeDaggerDashes)));
        //return text("" + BreezeDaggerDashes).append(text("/")).append(text("" + BreezeDaggerDefaultDashes)); //debugging
    }
}
