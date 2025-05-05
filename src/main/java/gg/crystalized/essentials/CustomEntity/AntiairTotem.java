package gg.crystalized.essentials.CustomEntity;

import com.destroystokyo.paper.ParticleBuilder;
import gg.crystalized.essentials.PlayerData;
import gg.crystalized.essentials.crystalized_essentials;
import io.papermc.paper.math.Rotations;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class AntiairTotem {

    boolean isActive = false;
    Player owner;
    int health = 7;
    public ArmorStand entity;
    public ArmorStand turretBase;
    public ArmorStand turret;
    TextDisplay name;
    TextDisplay ownerTag;
    TextDisplay healthBar;
    List<Player> allies = new ArrayList<>();

    public AntiairTotem(Player p, Location spawnLoc) {
        isActive = true;
        owner = p;
        Location loc =  new Location(owner.getWorld(), spawnLoc.getBlockX() + 0.5, spawnLoc.getBlockY(), spawnLoc.getBlockZ() + 0.5, owner.getLocation().getYaw() + 180, 0);
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

        name = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.5, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(translatable("crystalized.totem.antiair.name"));
            entity.setBillboard(Display.Billboard.CENTER);
        });
        ownerTag = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.25, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(owner.displayName());
            entity.setBillboard(Display.Billboard.CENTER);
        });
        healthBar = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(text("health"));
            entity.setBillboard(Display.Billboard.CENTER);
        });
        turretBase = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 1, entity.getZ(), owner.getLocation().getYaw(), 0), ArmorStand.class, entity -> {
            entity.setVisible(false);
            ItemStack model = new ItemStack(Material.CHARCOAL);
            ItemMeta modelMeta = model.getItemMeta();
            modelMeta.setItemModel(new NamespacedKey("crystalized", "models/antiair_totem_turret"));
            model.setItemMeta(modelMeta);
            entity.getEquipment().setHelmet(model);
            entity.setGravity(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
        });
        turret = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 0, entity.getZ(), owner.getLocation().getYaw(), 0), ArmorStand.class, entity -> {
            entity.setVisible(false);
            ItemStack model = new ItemStack(Material.CHARCOAL);
            ItemMeta modelMeta = model.getItemMeta();
            modelMeta.setItemModel(new NamespacedKey("crystalized", "models/antiair_totem_turret"));
            modelMeta.setCustomModelData(1);
            model.setItemMeta(modelMeta);
            entity.getEquipment().setHelmet(model);
            entity.setGravity(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
        });
        owner.playSound(owner, "minecraft:block.anvil.place", 0, 0.75F);

        //TODO this is probably broken, how do we do this? This is meant to function in Crystal Blitz specifically. Was thinking Scoreboard Teams but that isn't reliable
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboard().equals(owner.getScoreboard())) {
                allies.add(player);
            }
        }

        new BukkitRunnable() {
            int i = 2;
            public void run() {
                switch (i) {
                    case 2 -> {
                        turretBase.teleport(new Location(turretBase.getWorld(), entity.getX(), entity.getY() + 1.25, entity.getZ(), entity.getLocation().getYaw(), 0));
                        turret.teleport(new Location(turret.getWorld(), entity.getX(), entity.getY() + 0.25, entity.getZ(), entity.getLocation().getYaw(), 0));
                        owner.playSound(owner, "minecraft:item.crossbow.quick_charge_1", 1, 1.25F);
                    }
                    case 1 -> {
                        turretBase.teleport(new Location(turretBase.getWorld(), entity.getX(), entity.getY() + 1.75, entity.getZ(), entity.getLocation().getYaw(), 0));
                        turret.teleport(new Location(turret.getWorld(), entity.getX(), entity.getY() + 0.75, entity.getZ(), entity.getLocation().getYaw(), 0));
                    }
                    default -> {
                        owner.playSound(owner, "minecraft:item.crossbow.loading_end", 1, 1.25F);
                        cancel();
                    }
                }
                i--;
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 20, 10);

        new BukkitRunnable() {
            public void run() {
                for (Entity e : entity.getNearbyEntities(7, 70, 7)) {
                    //TODO add check for allies list once above for statement is implemented
                    if (e instanceof Player p && p.isGliding()) {
                        if (p.equals(owner)) {
                            //Do nothing
                        } else {
                            PlayerData pd = crystalized_essentials.getInstance().getPlayerData(p.getName());
                            p.setGliding(false);
                            visualShoot(p.getLocation(), p);

                            //EntityToggleGlideEvent doesn't fire here, have to do the Winged Orb logic here too, this is dumb
                            if (pd.isUsingWingedOrb) {
                                pd.isUsingWingedOrb = false;
                                p.getInventory().setChestplate(pd.lastChestPlateBeforeWingedOrb);
                            }
                        }
                    }
                }
                healthBar.text(text("#".repeat(health)).append(text("-".repeat(7 - health))));

                if (health == 0 || name.isDead() || entity.isDead() || health < 0) {
                    isActive = false;
                    entity.remove();
                    turret.remove();
                    turretBase.remove();
                    name.remove();
                    ownerTag.remove();
                    healthBar.remove();
                    crystalized_essentials.getInstance().antiairTotemList.remove(crystalized_essentials.getInstance().getAntiAirTotemByEntity(entity));
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);

    }

    public void hit(Player p) {
        health--;
        p.playSound(p, "minecraft:block.anvil.place", 1, 2);
    }

    public void visualShoot(Location loc, Player hP) {
        owner.playSound(owner, "minecraft:item.trident.thunder", 1, 1); //TODO replace this at some point
        hP.playSound(hP, "minecraft:item.trident.thunder", 1, 1); //TODO replace this at some point

        //TODO make the turret rotate towards hP (this doesn't work and i dont understand this)
        //turret.setHeadRotations(Rotations.ofDegrees(loc.getX(), loc.getY(), loc.getZ()));

        entity.setGlowing(true);
        owner.sendMessage(text("[!] Your antiair totem was triggered by ").append(hP.displayName())); //TODO make this translatable
        linearParticles(loc, turret.getLocation());

        new BukkitRunnable() {
            public void run() {
                entity.setGlowing(false);
                cancel();
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 40, 1);
    }

    //TODO this is mostly copy pasted from the grappling orb but its barley noticable because of how little particles it spawns
    public void linearParticles(Location start, Location end) {
        ParticleBuilder builder = new ParticleBuilder(Particle.DUST);
        builder.color(Color.AQUA);
        builder.count(20);
        builder.offset(0, 0, 0);
        builder.extra(0);
        Vector v = new Vector(end.getX() - start.getX(), end.getY() - start.getY(), end.getZ() - start.getZ());
        int count = 0;
        Vector vec = v.multiply(0.1 * v.length());
        while (vec.length() <= v.length() && count < 100) {
            builder.location(start);
            builder.spawn();
            start = new Location(start.getWorld(), vec.getX() + start.getX(), vec.getY() + start.getY(), vec.getZ() + start.getZ());
            vec = vec.add(vec);
            count++;
        }
    }
}
