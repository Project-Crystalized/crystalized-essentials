package gg.crystalized.essentials.CustomEntity;

import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;

public class KnockoutOrb {

    Player owner;
    Player target;
    ArmorStand entity;

    public KnockoutOrb(Player o) {
        owner = o;
        List<String> playerAllies = crystalized_essentials.getInstance().getAllies(owner);

        for (Entity e : owner.getNearbyEntities(30, 30, 30)) {
            if (e instanceof Player) {
                if (!(playerAllies.contains(e.getName())) && !((Player) e).getGameMode().equals(GameMode.SPECTATOR)) {
                    target = (Player) e;
                }
            }
        }
        if (target == null) {
            owner.sendMessage(text("[!] An error occurred with your Knockout Orb, target is null."));
            crystalized_essentials plugin = crystalized_essentials.getInstance();
            plugin.getLogger().log(Level.WARNING, "" + owner.getName() + "'s Knockout Orb failed, target is null.");
            return;
        }

        entity = owner.getWorld().spawn(owner.getLocation().add(0, 2, 0), ArmorStand.class, entity -> {
            ItemStack rocket = new ItemStack(Material.CHARCOAL);
            ItemMeta rocketMeta = rocket.getItemMeta();
            rocketMeta.setItemModel(new NamespacedKey("crystalized", "models/knockout_orb"));
            rocket.setItemMeta(rocketMeta);
            entity.setItem(EquipmentSlot.HEAD, rocket);
            entity.setCustomNameVisible(true); //Make this true for debug stats above model
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD);
            entity.setDisabledSlots(EquipmentSlot.HAND);
            entity.setDisabledSlots(EquipmentSlot.OFF_HAND);
        });

        Player Target = target; //useless ass code but intellij forced me to do this
        new BukkitRunnable() {
            int timerUntilDeath = 10 * 20;
            public void run() {
                entity.customName(text("T:" + timerUntilDeath + " | Owner: " + owner.getName() + " | Target: " + Target.getName()));

                timerUntilDeath--;
                if ((timerUntilDeath == 0 || timerUntilDeath < 0) || entity.getNearbyEntities(3, 3, 3).contains(Target)) {
                    entity.getLocation().createExplosion(10, false, false);
                    entity.remove();
                    cancel();
                }

                if (target == null) { //For if the target disconnects and/or if the game ends when someone uses this orb
                    entity.remove();
                    cancel();
                }

                //TODO somehow move the projectile towards player
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

        new BukkitRunnable() {
            public void run() {
                Target.playSound(entity.getLocation(), "minecraft:block.note_block.bell", 1, 0.5F);
                if (entity.isDead()) {
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 5);
    }
}
