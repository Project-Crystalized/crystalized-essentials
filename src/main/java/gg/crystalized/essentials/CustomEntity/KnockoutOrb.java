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


        entity = owner.getWorld().spawn(owner.getLocation().add(0, 2, 0), ArmorStand.class, entity -> {
            ItemStack rocket = new ItemStack(Material.CHARCOAL);
            ItemMeta rocketMeta = rocket.getItemMeta();
            rocketMeta.setItemModel(new NamespacedKey("crystalized", "models/knockout_orb"));
            rocket.setItemMeta(rocketMeta);
            entity.setItem(EquipmentSlot.HEAD, rocket);
            //entity.setCustomNameVisible(true); //Make this true for debug stats above model
            entity.setInvisible(true);
            entity.setInvulnerable(true);

            // Tried making it false didn't work, the thing didn't fly - MT
            entity.setGravity(true);

            entity.setDisabledSlots(EquipmentSlot.HEAD);
            entity.setDisabledSlots(EquipmentSlot.HAND);
            entity.setDisabledSlots(EquipmentSlot.OFF_HAND);
        });

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(entity.getLocation(), "minecraft:entity.bat.takeoff", 1f, 0.5f); // was using 'entity' before it existed
        }

        new BukkitRunnable() {
            int timerUntilDeath = 13 * 20;

            //Tuning constants (For tweaking purproses)
            final double SPEED = 0.85;                 // blocks/tick (≈17 b/s at 20tps)
            final double MAX_TURN_DEG = 9.0;           // max yaw/pitch change per tick
            final boolean USE_LEAD = true;             // aim slightly ahead of moving targets
            final double MODEL_YAW_OFFSET_DEG = 0.0;   // set to 180 if your model points backward
            final double MODEL_PITCH_OFFSET_DEG = -90.0; // set to 0 or +90 depending on model
            // Knock Off Orb homming rockect loggic

            public void run() {
                entity.customName(text("T:" + timerUntilDeath + " | Owner: " + owner.getName() + " | Target: " + target.getName()));

                timerUntilDeath--;
                if ((timerUntilDeath <= 0)
                        || entity.getNearbyEntities(0.3, 0.3, 0.3).contains(target)
                        || target.getGameMode().equals(GameMode.SPECTATOR)) {
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
                    if (!b.isEmpty()) {
                        b.setType(Material.AMETHYST_BLOCK);
                        entity.getWorld().playSound(b.getLocation(), "minecraft:block.amethyst_block.break", 1, 1);
                        b.breakNaturally(true);
                    }
                }

                //set the facing direction towards target
                //TODO gradually set the direction to target instead of doing this instantly
                //TODO NEW: Improve the homming rocket logic of Knock Off Orb


                // =====================================================================
                // Rocket homming for knock off start here
                // =====================================================================

                // Choose aim point/target (Optionally with simple lead)
                org.bukkit.Location standLoc = entity.getLocation();
                org.bukkit.util.Vector targetPos = target.getEyeLocation().toVector();

                if (USE_LEAD) {
                    double dist = standLoc.toVector().distance(targetPos);
                    double leadTime = dist / (SPEED * 20.0); // seconds (speed is per tick)
                    // lead by target velocity * time (rough, but feels good)
                    targetPos = targetPos.add(target.getVelocity().clone().multiply(leadTime));
                }

                org.bukkit.util.Vector toAim = targetPos.subtract(standLoc.toVector());
                if (toAim.lengthSquared() < 1.0e-6) return;

                // Desired yaw/pitch from this aim vector
                org.bukkit.Location tmp = standLoc.clone();
                // fills yaw pitch
                tmp.setDirection(toAim.normalize());
                float desiredYaw = tmp.getYaw();
                float desiredPitch = tmp.getPitch();

                // Yaw and pitch currently
                float curYaw = standLoc.getYaw();
                float curPitch = standLoc.getPitch();

                // clamp is how much it can turn in current tick (smooth curves)
                float deltaYaw = normalizeYaw(desiredYaw - curYaw);
                float deltaPitch = desiredPitch - curPitch;

                float max = (float) MAX_TURN_DEG;
                if (deltaYaw >  max) deltaYaw =  max;
                if (deltaYaw < -max) deltaYaw = -max;
                if (deltaPitch >  max) deltaPitch =  max;
                if (deltaPitch < -max) deltaPitch = -max;

                float newYaw = normalizeYaw(curYaw + deltaYaw);
                float newPitch = clamp(curPitch + deltaPitch, -89.9f, 89.9f);

                // Rotate the body, doesn't teleport it
                entity.setRotation(newYaw, newPitch);

                // flys forward along body facing
                org.bukkit.Location forwardLoc = standLoc.clone();
                forwardLoc.setYaw(newYaw);
                forwardLoc.setPitch(newPitch);
                org.bukkit.util.Vector forward = forwardLoc.getDirection().normalize();
                entity.setVelocity(forward.multiply(SPEED));

                // Makes the HEAD/ORB align with body facing
                //    If the model is built relative to the head, use a fixed pose offset. (Personaly not sure)
                entity.setHeadPose(new EulerAngle(
                        Math.toRadians(MODEL_PITCH_OFFSET_DEG),
                        Math.toRadians(MODEL_YAW_OFFSET_DEG),
                        0
                ));

                // =====================================================================
                // ORB homming end
                // =====================================================================
            }

            // ORB: helpers inside the class
            private float normalizeYaw(float yaw) {
                yaw = yaw % 360f;
                if (yaw <= -180f) yaw += 360f;
                if (yaw > 180f) yaw -= 360f;
                return yaw;
            }
            private float clamp(float v, float min, float max) {
                return v < min ? min : (v > max ? max : v);
            }
            // ORB

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
