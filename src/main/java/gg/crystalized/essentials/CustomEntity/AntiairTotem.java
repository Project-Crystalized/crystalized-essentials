package gg.crystalized.essentials.CustomEntity;

import gg.crystalized.essentials.PlayerData;
import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class AntiairTotem {

    boolean isActive = false;
    Player owner;
    int health = 7;
    public ArmorStand entity;
    ArmorStand turret;
    TextDisplay name;
    TextDisplay ownerTag;
    TextDisplay healthBar;
    List<Player> allies = new ArrayList<>();

    public AntiairTotem(Player p) {
        isActive = true;
        owner = p;
        Location loc =  new Location(owner.getWorld(), owner.getLocation().getBlockX() + 0.5, owner.getLocation().getBlockY(), owner.getLocation().getBlockZ() + 0.5, 0, owner.getLocation().getPitch());
        entity = owner.getWorld().spawn(loc, ArmorStand.class, entity -> {
            ItemStack model = new ItemStack(Material.CHARCOAL);
            ItemMeta modelMeta = model.getItemMeta();
            modelMeta.setItemModel(new NamespacedKey("crystalized", "models/antiair_totem"));
            model.setItemMeta(modelMeta);
            entity.getEquipment().setHelmet(model);
            entity.setVisible(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
            entity.setGravity(false);
            entity.setInvulnerable(false);
        });
        turret = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 1.5, entity.getZ()), ArmorStand.class, entity -> {
            entity.setVisible(false);
            //TODO this entity is removed for now
            // Its planned to rotate towards hostile players who get their elytra shot down (LadyCat you could reuse the charged crossbow stuff again lol)
        });
        turret.remove();

        name = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 4, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(translatable("crystalized.totem.antiair.name"));
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

        //TODO this is probably broken, how do we do this? This is meant to function in Crystal Blitz specifically. Was thinking Scoreboard Teams but that isn't reliable
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboard().equals(owner.getScoreboard())) {
                allies.add(player);
            }
        }

        new BukkitRunnable() {
            public void run() {
                for (Entity e : entity.getNearbyEntities(5, 50, 5)) {
                    //TODO add check for allies list once above for statement is implemented
                    if (e instanceof Player p) {
                        if (p.equals(owner)) {
                            //Do nothing
                        } else {
                            PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
                            p.setGliding(false);

                            //EntityToggleGlideEvent doesn't fire here, have to do the Winged Orb logic here too, this is dumb
                            if (pd.isUsingWingedOrb) {
                                pd.isUsingWingedOrb = false;
                                p.getInventory().setChestplate(pd.lastChestPlateBeforeWingedOrb);
                            }
                        }
                    }
                }
                healthBar.text(text("#".repeat(health)).append(text("-".repeat(7 - health))));

                if (health == 0) {
                    isActive = false;
                    entity.remove();
                    turret.remove();
                    name.remove();
                    ownerTag.remove();
                    healthBar.remove();
                    crystalized_essentials.getInstance().antiairTotemList.remove(crystalized_essentials.getInstance().getAntiAirTotemByEntity(entity));
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

    }

    public void hit() {
        health--;
    }
}
