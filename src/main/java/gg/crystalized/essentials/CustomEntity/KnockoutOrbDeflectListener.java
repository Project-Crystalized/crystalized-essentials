package gg.crystalized.essentials.CustomEntity;

import gg.crystalized.essentials.crystalized_essentials;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class KnockoutOrbDeflectListener implements Listener {

    private static final double SEARCH_RADIUS = 80.0;
    private static final double SWING_REACH = 3.0; // blocks
    private static final NamespacedKey ORB_TAG =
            new NamespacedKey(crystalized_essentials.getInstance(), "knockout_orb");

    //This works thanks to two paths, not sure which one wins in the end but both is good, as just punching wasn't working at first
    // Path A : Normal melee damage on the stand
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onOrbMeleeHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ArmorStand as)) return;
        if (!isOurOrb(as)) return; // ensures it’s actually one of our orbs

        if (!(e.getDamager() instanceof Player deflector)) return;

        // melee-only
        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        // If we got here, we definitely punched the orb
        e.setCancelled(true);
        handleDeflect(as, deflector);
    }

    // PATH B: If some plugin blocks ArmorStand damage, detect the punch via arm swing + raytrace
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onSwing(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player p = e.getPlayer();

        //Ray tracing logic
        RayTraceResult rt = p.getWorld().rayTraceEntities(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                SWING_REACH,
                ent -> ent instanceof ArmorStand as && isOurOrb((ArmorStand) as)
        );

        if (rt == null || !(rt.getHitEntity() instanceof ArmorStand as)) return;

        handleDeflect(as, p);
    }

    // helpers methdos

    //Checks if this the orb
    private boolean isOurOrb(ArmorStand as) {
        return as.getPersistentDataContainer().has(ORB_TAG, PersistentDataType.BYTE)
                && crystalized_essentials.getInstance().getKnockoutOrbByEntity(as) != null;
    }

    //This method handels deflection
    private void handleDeflect(ArmorStand as, Player deflector) {
        KnockoutOrb orb = crystalized_essentials.getInstance().getKnockoutOrbByEntity(as);
        if (orb == null) return; // should not happen if tagged + list is correct. Just extra saftey

        // ignored if the puncher is the current owner (prevents owner self-flips)
        if (deflector.equals(orb.owner)) return;

        // brief cooldown after a deflect (prevents instant ping-pong)
        if (orb.deflectMeleeLockTicks > 0) return;


        // pick who it should go after
        Player newTarget = orb.owner;
        if (newTarget == null || !newTarget.isOnline() || newTarget.equals(deflector)) {
            newTarget = findNearestValidPlayer(as.getLocation(), deflector);
        }
        if (newTarget == null) {
            explodeAndRemove(as, orb);
            return;
        }

        // flip ownership + i-frames so it won't insta-pop
        orb.deflectTo(newTarget, deflector);

        // instant re-aim + small boost for feedback
        Location loc = as.getLocation();
        Vector dir = newTarget.getEyeLocation().toVector().subtract(loc.toVector()).normalize();
        Location facing = loc.clone(); facing.setDirection(dir);

        //as.setRotation(facing.getYaw(), facing.getPitch());
        //The line above made it fly under a weird angle

        // Keeps a little nudge so the flip feels responsive:
        as.setVelocity(dir.multiply(1.15));

        World w = as.getWorld();
        w.playSound(as.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.25f);
        w.spawnParticle(Particle.CRIT, as.getLocation().add(0, 0.8, 0), 16, 0.25, 0.25, 0.25, 0.15);

        // debug
        //deflector.sendActionBar(Component.text("ORB DEFLECTED"));
    }

    //This method is used if the true owner is dead and new player needs to be deflecte to
    private Player findNearestValidPlayer(Location origin, Player exclude) {
        double bestDistSq = SEARCH_RADIUS * SEARCH_RADIUS;
        Player best = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(exclude)) continue;
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            if (!p.getWorld().equals(origin.getWorld())) continue;
            double d2 = p.getLocation().distanceSquared(origin);
            if (d2 <= bestDistSq) { bestDistSq = d2; best = p; }
        }
        return best;
    }

    //Explodes and removes
    private void explodeAndRemove(ArmorStand as, KnockoutOrb orb) {
        Location l = as.getLocation();
        l.createExplosion(3f, false, false);
        crystalized_essentials.getInstance().knockoutOrbList.remove(orb);
        as.remove();
    }
}
