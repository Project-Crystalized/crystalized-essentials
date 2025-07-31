package gg.crystalized.essentials.CustomEntity;

import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class DefenceTotem {

    boolean isActive = false;
    Player owner;
    int health = 10;
    public ArmorStand entity;
    TextDisplay name;
    TextDisplay ownerTag;
    TextDisplay healthBar;

    public DefenceTotem(Player p, Location spawnLoc) {
        isActive = true;
        owner = p;
        Location loc =  new Location(owner.getWorld(), spawnLoc.getBlockX() + 0.5, spawnLoc.getBlockY(), spawnLoc.getBlockZ() + 0.5, owner.getLocation().getYaw() + 180, 0);
        entity = owner.getWorld().spawn(loc, ArmorStand.class, entity -> {
            ItemStack model = new ItemStack(Material.CHARCOAL);
            ItemMeta modelMeta = model.getItemMeta();
            modelMeta.setItemModel(new NamespacedKey("crystalized", "models/defence_totem"));
            model.setItemMeta(modelMeta);
            entity.getEquipment().setHelmet(model);
            entity.setVisible(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
            entity.setGravity(false);
            entity.setInvulnerable(false);
        });

        name = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 4, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(translatable("crystalized.totem.defence.name"));
            entity.setBillboard(Display.Billboard.CENTER);
        });
        ownerTag = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.75, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(owner.displayName());
            entity.setBillboard(Display.Billboard.CENTER);
        });
        healthBar = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.5, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(text("health"));
            entity.setBillboard(Display.Billboard.CENTER);
        });

        new BukkitRunnable() {
            public void run() {
                for (Entity e : entity.getNearbyEntities(3, 3, 3)) {
                    if (e instanceof Arrow || e instanceof SpectralArrow || e instanceof Snowball || e instanceof Fireball) {
                        e.remove();
                        owner.sendMessage(text("Your Defence Totem has blocked a projectile in its radius"));
                    }
                }

                healthBar.text(
                        text("\uE11A").append(text("\uE11B".repeat(health))).append(text("\uE11C".repeat(10 - health))).append(text("\uE11D"))
                );

                if (health == 0 || name.isDead() || entity.isDead() || health < 0) {
                    isActive = false;
                    entity.remove();
                    name.remove();
                    ownerTag.remove();
                    healthBar.remove();
                    crystalized_essentials.getInstance().defenceTotemList.remove(crystalized_essentials.getInstance().getDefenceTotemByEntity(entity));
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }

    public void hit(Player p) {
        health--;
        p.playSound(p, "minecraft:block.anvil.place", 1, 2);
    }
}
