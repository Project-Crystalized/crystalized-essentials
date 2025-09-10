package gg.crystalized.essentials.CustomEntity;

import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;

public class KnockoutOrb {

    public Player owner;
    public Player target;
    public ArmorStand entity;

    public KnockoutOrb(Player o) {
        owner = o;
        List<String> playerAllies = crystalized_essentials.getInstance().getAllies(owner);

        for (Entity e : owner.getNearbyEntities(80, 80, 80)) { //womp womp if this lags the server
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
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(entity, "minecraft:entity.bat.takeoff", 1, 0.5f);
        }

        entity = owner.getWorld().spawn(owner.getLocation().add(0, 2, 0), ArmorStand.class, entity -> {
            ItemStack rocket = new ItemStack(Material.CHARCOAL);
            ItemMeta rocketMeta = rocket.getItemMeta();
            rocketMeta.setItemModel(new NamespacedKey("crystalized", "models/knockout_orb"));
            rocket.setItemMeta(rocketMeta);
            entity.setItem(EquipmentSlot.HEAD, rocket);
            //entity.setCustomNameVisible(true); //Make this true for debug stats above model
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            //entity.setGravity(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD);
            entity.setDisabledSlots(EquipmentSlot.HAND);
            entity.setDisabledSlots(EquipmentSlot.OFF_HAND);
        });

        new BukkitRunnable() {
            int timerUntilDeath = 13 * 20;
            public void run() {
                entity.customName(text("T:" + timerUntilDeath + " | Owner: " + owner.getName() + " | Target: " + target.getName()));

                timerUntilDeath--;
                if ((timerUntilDeath == 0 || timerUntilDeath < 0) || entity.getNearbyEntities(0.3, 0.3, 0.3).contains(target) || target.getGameMode().equals(GameMode.SPECTATOR)) {
                    entity.getLocation().createExplosion(3, false, false);
                    crystalized_essentials.getInstance().knockoutOrbList.remove(crystalized_essentials.getInstance().getKnockoutOrbByEntity(entity));
                    entity.remove();
                    cancel();
                }
                if (target == null) { //For if the target disconnects and/or if the game ends when someone uses this orb
                    crystalized_essentials.getInstance().knockoutOrbList.remove(crystalized_essentials.getInstance().getKnockoutOrbByEntity(entity));
                    entity.remove();
                    cancel();
                }

                for (Block b : getNearbyBlocks(entity.getLocation(), 3, 4, 3)) {
                    b.setType(Material.AIR);
                    entity.getWorld().playSound(b.getLocation(), "minecraft:block.amethyst_block.break", 1, 1);
                }

                //set the facing direction towards target
                //TODO gradually set the direction to target instead of doing this instantly
                org.bukkit.util.Vector entityLocVector = entity.getLocation().toVector();
                org.bukkit.util.Vector targetLocVector = target.getLocation().toVector();
                entity.teleport(entity.getLocation().setDirection(targetLocVector.subtract(entityLocVector)));
                entity.setHeadPose(new EulerAngle(0, 0, 0)); //TODO head rotation

                //move towards target
                entity.setVelocity(entity.getLocation().getDirection().multiply(0.75));
            }

            Set<Block> getNearbyBlocks(Location center, int x, int y, int z) {
                Set<Block> list = new HashSet<>();
                Location loc = center.subtract(x/2, y/2, z/2);
                int X = x;
                while (X != 0) {
                    int Y = y;
                    while (Y != 0) {
                        int Z = z;
                        while (Z != 0) {
                            list.add(loc.clone().add(X, Y, Z).getBlock());
                            Z--;
                        }
                        Y--;
                    }
                    X--;
                }

                return list;
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

        new BukkitRunnable() {
            public void run() {
                target.playSound(entity.getLocation(), "minecraft:block.note_block.bell", 1, 0.5F);
                if (entity.isDead()) {
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 5);
    }

    public void changeTargetAndOwner(Player newTarget, Player newOwner) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(entity, "crystalized:effect.nexus_crystal_destroyed", 1, 2);
        }
        target = newTarget;
        owner = newOwner;
    }
}
