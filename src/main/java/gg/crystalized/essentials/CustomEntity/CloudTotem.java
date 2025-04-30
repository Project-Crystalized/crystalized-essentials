package gg.crystalized.essentials.CustomEntity;

import com.destroystokyo.paper.ParticleBuilder;
import gg.crystalized.essentials.CustomBows;
import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class CloudTotem {

    boolean isActive = false;
    int health = 10;
    Player owner;
    ArmorStand entity;
    TextDisplay name;
    boolean isKnockoff = false;
    Set<Block> blocks = new HashSet<>();
    int timer = 15;

    public CloudTotem(Player p) {
        owner = p;
        Location loc;
        if (p.isOnGround()) {
            loc = new Location(owner.getWorld(), owner.getLocation().getBlockX() + 0.5, owner.getLocation().getBlockY(), owner.getLocation().getBlockZ() + 0.5, 0, 0);
        } else {
            loc = new Location(owner.getWorld(), owner.getLocation().getBlockX() + 0.5, owner.getLocation().getBlockY() - 1, owner.getLocation().getBlockZ() + 0.5, 0, 0);
        }

        entity = owner.getWorld().spawn(loc, ArmorStand.class, entity -> {
            ItemStack model = new ItemStack(Material.CHARCOAL);
            ItemMeta modelMeta = model.getItemMeta();
            modelMeta.setItemModel(new NamespacedKey("crystalized", "models/cloud_totem"));
            model.setItemMeta(modelMeta);
            entity.getEquipment().setHelmet(model);
            entity.setVisible(false);
            entity.setDisabledSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
            entity.setGravity(false);
        });
        new BukkitRunnable(){
            public void run(){
                if(!isActive){
                    cancel();
                }
                helixAnimation(3, 3, entity.getLocation(), 2);
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 30);

        name = entity.getWorld().spawn(new Location(entity.getWorld(), entity.getX(), entity.getY() + 3.5, entity.getZ()), TextDisplay.class, entity -> {
            entity.text(text("name here"));
            entity.setBillboard(Display.Billboard.CENTER);
        });
        isActive = true;

        try {
            Class<?> cls = Class.forName("gg.knockoff.game.knockoff");
            isKnockoff = true;
        } catch (ClassNotFoundException e) {
            isKnockoff = false;
        }

        createBlocks();
        new BukkitRunnable() {

            public void run() {
                timer--;
                if (timer == 0) {
                    destroy();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 5, 20);

        new BukkitRunnable() {
            public void run() {
                name.text(translatable("crystalized.totem.cloud.name").append(text(" (" + timer + "s)")));
                if (!isActive) {
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 5, 1);
    }

    //I hate these 2 methods, could be optimised, but ill do for now ig
    private void createBlocks() {
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ()), 4);
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ() + 1), 4);
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ() + 2), 4);
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ() + 3), 3);
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ() - 1), 4);
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ() - 2), 4);
        createLineOfBlock(new Location(entity.getWorld(), entity.getX(), entity.getY() - 1, entity.getZ() - 3), 3);

        Location hitbox1 = new Location(entity.getWorld(), entity.getX(), entity.getY() , entity.getZ());
        hitbox1.getBlock().setType(Material.BARRIER);
        hitbox1.getBlock().getState().update();
        blocks.add(hitbox1.getBlock());
        Location hitbox2 = new Location(entity.getWorld(), entity.getX(), entity.getY() + 1 , entity.getZ());
        hitbox2.getBlock().setType(Material.BARRIER);
        hitbox2.getBlock().getState().update();
        blocks.add(hitbox2.getBlock());
        Location hitbox3 = new Location(entity.getWorld(), entity.getX(), entity.getY() + 2 , entity.getZ());
        hitbox3.getBlock().setType(Material.BARRIER);
        hitbox3.getBlock().getState().update();
        blocks.add(hitbox3.getBlock());
    }

    //size shouldn't be negative
    private void createLineOfBlock(Location middleLoc, int size) {
        new BukkitRunnable() {
            int i = 0;
            public void run(){
                changeBlock(new Location(middleLoc.getWorld(), middleLoc.getX() + i, middleLoc.getY(), middleLoc.getZ()));
                changeBlock(new Location(middleLoc.getWorld(), middleLoc.getX() - i, middleLoc.getY(), middleLoc.getZ()));
                i++;
                if (i == size || i > size) {
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }

    //Could be optimised, this is a mess but it works
    private void changeBlock(Location loc) {
        if (loc.getBlock().isEmpty()) {
            if (isKnockoff) {
                if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/blue"))) {
                    loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.EAST);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/cyan"))) {
                    loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.NORTH);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/green"))) {
                    loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.SOUTH);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/lemon"))) {
                    loc.getBlock().setType(Material.WHITE_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.WEST);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/lime"))) {
                    loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.EAST);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/magenta"))) {
                    loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.NORTH);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/orange"))) {
                    loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.SOUTH);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/peach"))) {
                    loc.getBlock().setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.WEST);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/purple"))) {
                    loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.EAST);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/white"))) {
                    loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.SOUTH);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/yellow"))) {
                    loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.WEST);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else if (inventoryContainsItemModelItem(owner, new NamespacedKey("crystalized", "block/nexus/red"))) {
                    loc.getBlock().setType(Material.GRAY_GLAZED_TERRACOTTA);
                    Directional dir = (Directional) loc.getBlock().getBlockData();
                    dir.setFacing(BlockFace.NORTH);
                    loc.getBlock().setBlockData(dir);
                    loc.getBlock().getState().update();
                } else {
                    loc.getBlock().setType(Material.AMETHYST_BLOCK);
                    loc.getBlock().getState().update();
                }
            } else {
                loc.getBlock().setType(Material.TINTED_GLASS);
                loc.getBlock().getState().update();
            }
            blocks.add(loc.getBlock());
        }
    }

    private boolean inventoryContainsItemModelItem(Player p, NamespacedKey itemModel) {
        PlayerInventory inv = p.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                if (item.hasItemMeta()) {
                    if (item.getItemMeta().hasItemModel()) {
                        if (item.getItemMeta().getItemModel().equals(itemModel)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void destroy() {
        isActive = false;
        new BukkitRunnable() {
            int i = 0;

            public void run() {
                Set<Block> remove_set = new HashSet<>();
                for (Block b : blocks) {
                    i++;
                    b.setType(Material.AIR);
                    remove_set.add(b);

                    if (i > 10) {
                        break;
                    }
                }
                blocks.removeAll(remove_set);
                if (blocks.isEmpty()) {
                    cancel();
                }
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 1, 1);

        entity.remove();
        name.remove();
    }

    public void helixAnimation(double radius, double height, Location middle, int turns){
        // X(t) = (r * cos(t) + xm | r * sin(t) + ym | (h/2 * pi) * t) + zm
        new BukkitRunnable() {
            double t = 0;
            Location loc = middle;
            public void run(){
                if(!isActive){
                    cancel();
                }
                ParticleBuilder builder = new ParticleBuilder(Particle.DUST);
                builder.color(Color.AQUA);
                builder.count(2);
                builder.offset(0, 0, 0);
                builder.extra(0);
                if(t > turns * 2 * Math.PI){
                    t = 0;
                }

                builder.location(loc);
                builder.spawn();

                builder.clone().location(helixEquation(radius, height, middle, t - 0.1));
                builder.color(Color.AQUA, 0.4F);
                builder.clone().location(helixEquation(radius, height, middle, t - 0.2));
                builder.color(Color.AQUA, 0.3F);
                builder.clone().location(helixEquation(radius, height, middle, t - 0.3));
                builder.color(Color.AQUA, 0.2F);
                builder.clone().location(helixEquation(radius, height, middle, t - 0.5));
                builder.color(Color.AQUA, 0.1F);

                loc = helixEquation(radius, height, middle, t);
                t = t + 0.1;
            }
        }.runTaskTimer(crystalized_essentials.getInstance(), 0, 1);
    }
    
    public static Location helixEquation(double radius, double height, Location middle, double t){
        double x = radius * Math.cos(t) + middle.getX();
        double y = (height/(2 * Math.PI) * t) + middle.getY();
        double z = radius * Math.sin(t) + middle.getZ();
        return new Location(middle.getWorld(), x, y, z);
    }
}
