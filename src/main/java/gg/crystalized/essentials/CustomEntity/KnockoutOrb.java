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
import org.bukkit.persistence.PersistentDataType;
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
    public int deflectIFrames = 0; // ticks of "no explode on contact" after a deflect
    public int deflectTurnBoostTicks = 0; // faster turning for a few ticks after deflect
    public double headYawOffsetDeg = 0;   // temporary head yaw offset for the rottation around animation
    public int deflectMeleeLockTicks = 0; // ignore melee deflects for a few ticks after a deflect, so can't be punched again

    public KnockoutOrb(Player o) {
        owner = o;
        List<String> playerAllies = crystalized_essentials.getInstance().getAllies(owner);
        if (playerAllies == null) playerAllies = java.util.Collections.emptyList();

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
            //Needs to be false to be able to deflect
            entity.setInvulnerable(false);

            // Tried making it false didn't work, the thing didn't fly - MT
            entity.setGravity(true);

            entity.setDisabledSlots(EquipmentSlot.HEAD);
            entity.setDisabledSlots(EquipmentSlot.HAND);
            entity.setDisabledSlots(EquipmentSlot.OFF_HAND);


            // Taged it as a knockut orb (so the listener can identify it) ---
            NamespacedKey ORB_TAG = new NamespacedKey(crystalized_essentials.getInstance(), "knockout_orb");
            entity.getPersistentDataContainer().set(ORB_TAG, PersistentDataType.BYTE, (byte) 1);
            // -------------------------------------------------------------------
        });
        //makes sure it gets the instance
        crystalized_essentials.getInstance().knockoutOrbList.add(this);
        //System.out.println("The THING WAS SPAWNED NOOOOO");
        //The old bebug

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
                //Added an imidiate check if it is null or not valid it immidetely gets cannceled and returned
                if (entity == null || !entity.isValid()) { cancel(); return; }
                entity.customName(text("T:" + timerUntilDeath + " | Owner: " + owner.getName() + " | Target: " + target.getName()));

                //Gives time to deflect Iframes
                if (deflectIFrames > 0) deflectIFrames--; // tick down i-frames


                timerUntilDeath--;


                // turn-boost + head swivel easing ----
                //Head swivel means the turn arround annimation for deflect
                if (deflectTurnBoostTicks > 0) deflectTurnBoostTicks--;
                if (Math.abs(headYawOffsetDeg) > 0.5) headYawOffsetDeg *= 0.8; else headYawOffsetDeg = 0;
                if (deflectMeleeLockTicks > 0) deflectMeleeLockTicks--;
                // ---------------------------------------------------
               // only considers contact if target exists
                // Tweak the number to taste
                final double CONTACT = 0.20; // "contact radius" (half the size of the proximity box)
                boolean touchingTarget = target != null && entity.getNearbyEntities(CONTACT, CONTACT, CONTACT).contains(target);


                //Simplified If statement which combines two of the checkers together so less if statements
                if (target == null || !target.isOnline()
                        || target.getGameMode() == GameMode.SPECTATOR
                        || timerUntilDeath <= 0
                        || (touchingTarget && deflectIFrames <= 0)) {

                    entity.getLocation().createExplosion(3, false, false);
                    crystalized_essentials.getInstance().knockoutOrbList.remove(KnockoutOrb.this); // <-- remove by reference
                    entity.remove();
                    cancel();
                    return;
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

                //float max = (float) MAX_TURN_DEG;
                //Changes to be better for deflections turn around
                float max = (float) MAX_TURN_DEG + (deflectTurnBoostTicks > 0 ? 10f : 0f);
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

                // smooth head swivel with a little bit of bank
                double yawOffset = MODEL_YAW_OFFSET_DEG + headYawOffsetDeg;
                double rollDeg   = Math.max(-20, Math.min(20, -headYawOffsetDeg * 0.25));

                // Makes the HEAD/ORB align with body facing
                //    If the model is built relative to the head, use a fixed pose offset. (Personaly not sure)
                entity.setHeadPose(new EulerAngle(
                        Math.toRadians(MODEL_PITCH_OFFSET_DEG),
                        Math.toRadians(yawOffset),
                        Math.toRadians(rollDeg)
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


    // Extra helper (so listener can set both + retarget cleanly)
    public void deflectTo(Player newTarget, Player newOwner) {
        changeTargetAndOwner(newTarget, newOwner);
        deflectIFrames = 10; // 0.4s grace so punch wins the frame
        deflectTurnBoostTicks = 10;   // 0.5s of snappier turning
        headYawOffsetDeg = 180.0;     // start the head turned “backwards”, will ease to 0
        deflectMeleeLockTicks = 10; // 0.5s lock so the new owner can't instantly re-punch
    }
}
