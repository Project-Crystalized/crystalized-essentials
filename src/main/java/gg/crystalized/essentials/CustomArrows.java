package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

import static org.bukkit.Color.PURPLE;
import static org.bukkit.Particle.DUST;
import static org.bukkit.Sound.BLOCK_NOTE_BLOCK_IMITATE_CREEPER;
import static org.bukkit.Sound.ENTITY_GENERIC_EXPLODE;
import static org.bukkit.damage.DamageType.ARROW;
import static org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD;
import static org.bukkit.entity.EntityType.SPECTRAL_ARROW;
import static org.bukkit.potion.PotionEffectType.GLOWING;


public class CustomArrows{

    public static void onArrowHit(ProjectileHitEvent event){
        Bukkit.getLogger().log(Level.SEVERE, "entered onArrowHit");
        if(event.isCancelled()){
            Bukkit.getLogger().log(Level.SEVERE, "arrowHitEvent is canceled");
            return;
        }
        ArrowData data = CustomBows.arrows.get(event.getEntity());
        Projectile pro = event.getEntity();
        Arrow arrow;
        Location loc = event.getEntity().getLocation();
        Bukkit.getLogger().log(Level.SEVERE, data.arrType.toString());
        if(data.arrType == ArrowData.arrowType.spectral){
            ParticleBuilder builder = new ParticleBuilder(DUST);
            builder.color(Color.YELLOW);
            builder.location(loc);
            builder.count(50);
            builder.offset(3, 3,3);
            builder.spawn();
            Collection<LivingEntity> collect = loc.getNearbyLivingEntities(3);
            for(LivingEntity e : collect){
                e.addPotionEffect(new PotionEffect(GLOWING, 10*20, 0, false, false, true));
            }
            return;
        }
        arrow = (Arrow) pro;
        ItemMeta meta = arrow.getItemStack().getItemMeta();

        if(!meta.hasCustomModelData()){
            return;
        }

        if(data.arrType == ArrowData.arrowType.dragon){

            Particle.DustOptions options = new Particle.DustOptions(PURPLE, 1);
            AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity().getWorld().spawnEntity(loc, AREA_EFFECT_CLOUD, false);
            cloud.setColor(PURPLE);
            cloud.setParticle(DUST, options);

            DamageSource.Builder builder = DamageSource.builder(ARROW);
            builder.withCausingEntity(data.shooter);
            builder.withDirectEntity(cloud);
            builder.withDamageLocation(loc);
            DamageSource source = builder.build();
            new BukkitRunnable(){
                int i = 0;
                final Location loc = event.getEntity().getLocation();
                public void run(){
                    if(i >= 20*20){
                        cloud.remove();
                        cancel();
                    }
                    Collection<LivingEntity> collect = loc.getNearbyLivingEntities(2, 1);
                    for(LivingEntity liv : collect){
                        liv.damage(1, source);
                    }
                    i++;
                }
            }.runTaskTimer(crystalized_essentials.getInstance(),1,1);

        }else if(data.arrType == ArrowData.arrowType.explosive){

            Collection<LivingEntity> nearby = loc.getNearbyLivingEntities(3);
            Collection<LivingEntity> notSoNearby = loc.getNearbyLivingEntities(5);
            ArrayList<LivingEntity> removal = new ArrayList<>();

            for(LivingEntity e : notSoNearby){
                if(nearby.contains(e)){
                    removal.add(e);
                }
            }

            for(LivingEntity e : removal){
                notSoNearby.remove(e);
            }

            if(event.getHitEntity() != null){
                for(LivingEntity e : nearby){
                    Location eLoc = e.getLocation();
                    Vector v = new Vector(eLoc.getX() - loc.getX(),eLoc.getY() - loc.getY()+0.5, eLoc.getZ() - loc.getZ());
                    v = v.normalize().multiply(1);
                    e.damage(4, event.getEntity());
                    e.setVelocity(v);
                }

                for(LivingEntity e : notSoNearby){
                    Location eLoc = e.getLocation();
                    Vector v = new Vector(eLoc.getX() - loc.getX(),eLoc.getY() - loc.getY()+0.5, eLoc.getZ() - loc.getZ());
                    v = v.normalize().multiply(0.5);
                    e.damage(2, event.getEntity());
                    e.setVelocity(v);
                }
                ParticleBuilder builder = new ParticleBuilder(DUST);
                builder.color(Color.RED);
                builder.offset(5, 5, 5);
                builder.count(150);
                builder.location(event.getEntity().getLocation());
                builder.spawn();
                loc.getWorld().playSound(loc, ENTITY_GENERIC_EXPLODE, 1, 1);
                return;
            }

            new BukkitRunnable(){
                int i = 0;
                public void run(){
                    if(i >= 3){
                        cancel();
                    }
                    loc.getWorld().playSound(loc, BLOCK_NOTE_BLOCK_IMITATE_CREEPER, 1, 1);
                    i++;
                }
            }.runTaskTimer(crystalized_essentials.getInstance(), 0, 20);

            new BukkitRunnable(){
                public void run(){
                    for(LivingEntity e : nearby){
                        Location eLoc = e.getLocation();
                        Vector v = new Vector(eLoc.getX() - loc.getX(),eLoc.getY() - loc.getY()+0.5, eLoc.getZ() - loc.getZ());
                        v = v.normalize().multiply(1);
                        e.damage(4, event.getEntity());
                        e.setVelocity(v);
                    }

                    for(LivingEntity e : notSoNearby){
                        Location eLoc = e.getLocation();
                        Vector v = new Vector(eLoc.getX() - loc.getX(),eLoc.getY() - loc.getY()+0.5, eLoc.getZ() - loc.getZ());
                        v = v.normalize().multiply(0.5);
                        e.damage(2, event.getEntity());
                        e.setVelocity(v);
                    }
                    ParticleBuilder builder = new ParticleBuilder(DUST);
                    builder.color(Color.RED);
                    builder.offset(5, 5, 5);
                    builder.count(150);
                    builder.location(event.getEntity().getLocation());
                    builder.spawn();
                    loc.getWorld().playSound(loc, ENTITY_GENERIC_EXPLODE, 1, 1);
                    event.getEntity().remove();
                }
            }.runTaskLater(crystalized_essentials.getInstance(), 3*20);
        }
        
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent event){
        ItemMeta meta = event.getArrow().getItemStack().getItemMeta();
        if(meta != null && meta.hasCustomModelData()){
            if(meta.getCustomModelData() == 2){
                event.setCancelled(true);
            }
        }
    }

}
