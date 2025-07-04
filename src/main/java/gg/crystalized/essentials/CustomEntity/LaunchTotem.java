package gg.crystalized.essentials.CustomEntity;

import com.destroystokyo.paper.ParticleBuilder;
import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class LaunchTotem {

    boolean isActive = true;
    Player owner;
    public ArmorStand entity;
    TextDisplay name;
    TextDisplay ownerTag;
    int ticksUntilDestroy = (4 * 20) + 2;

    public LaunchTotem(Player p, Location spawnLoc) {
        owner = p;
        Location loc =  new Location(owner.getWorld(), spawnLoc.getBlockX() + 0.5, spawnLoc.getBlockY() , spawnLoc.getBlockZ() + 0.5, owner.getLocation().getYaw() + 180, 0);
        entity = owner.getWorld().spawn(loc, ArmorStand.class, entity -> {
            ItemStack model = new ItemStack(Material.CHARCOAL);
            ItemMeta modelMeta = model.getItemMeta();
            modelMeta.setItemModel(new NamespacedKey("crystalized", "models/launch_totem"));
            model.setItemMeta(modelMeta);
            entity.getEquipment().setHelmet(model);
            entity.setVisible(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
            entity.setGravity(false);
            entity.setInvulnerable(false);
        });

        name = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.5, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(translatable("crystalized.totem.launch.name"));
            entity.setBillboard(Display.Billboard.CENTER);
        });
        ownerTag = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.25, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(owner.displayName());
            entity.setBillboard(Display.Billboard.CENTER);
        });

        new BukkitRunnable() {
            int timer = 1;
            public void run() {

                timer--;
                if (timer == 0) {
                    timer = 2 * 20;
                    for (Entity e : entity.getNearbyEntities(5, 5, 5)) {
                        if (e instanceof Player) {
                            crystalized_essentials.getInstance().useWingedOrb((Player) e);
                            Player pl = (Player) e;
                            pl.playSound(entity, "minecraft:entity.breeze.jump", 1, 0.5F);

                            new BukkitRunnable(){
                                double count = 0.2;
                                final Location loc = pl.getLocation();
                                public void run(){
                                    if(count > 0.8){
                                        cancel();
                                    }
                                    circle(loc, count);
                                    count = count * 2;
                                }
                            }.runTaskTimer(crystalized_essentials.getInstance(), 0, 3);
                        }
                    }
                }

                ticksUntilDestroy--;
                if (ticksUntilDestroy == 0) {
                    entity.remove();
                }
                name.text(translatable("crystalized.totem.launch.name").append(text(" (" + (ticksUntilDestroy / 20) + "s / ").append(text(timer + " ticks)"))));

                if (name.isDead() || entity.isDead()) {
                    isActive = false;
                    entity.remove();
                    name.remove();
                    ownerTag.remove();
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }


    //These 2 methods are copy pasted from CustomCoalBasedItems, thanks LadyCat
    private void circle(Location middle, double radius) {
        double t = 0;
        ParticleBuilder builder = new ParticleBuilder(Particle.DUST);
        builder.color(Color.WHITE);
        Location loc;
        while (t <= 2 * Math.PI) {
            loc = circleEquation(middle, radius, t);
            builder.location(loc);
            builder.spawn();
            t = t + 0.3;
        }
    }

    private Location circleEquation(Location middle, double radius, double t){
        double x = radius * Math.cos(t) + middle.getX();
        double y = middle.getY();
        double z = radius * Math.sin(t) + middle.getZ();
        return new Location(middle.getWorld(), x, y, z);
    }
}
