package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;

import java.util.Collection;
import java.util.logging.Level;

import static org.bukkit.Color.PURPLE;
import static org.bukkit.Particle.DUST;
import static org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD;
import static org.bukkit.entity.EntityType.SPECTRAL_ARROW;
import static org.bukkit.potion.PotionEffectType.GLOWING;
import static org.bukkit.potion.PotionType.WATER_BREATHING;


public class CustomArrows implements Listener {
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event){
        Bukkit.getLogger().log(Level.SEVERE, "arrow hit");
        if(event.isCancelled()){
            return;
        }
        ArrowData data = CustomBows.arrows.get(event.getEntity());
        Projectile pro = event.getEntity();
        Arrow arrow;
        Location loc = event.getEntity().getLocation();
        if(event.getEntity().getType() == SPECTRAL_ARROW){
            Bukkit.getLogger().log(Level.SEVERE, "spectral if");
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

        if(meta.getCustomModelData() == 1){

            Particle.DustOptions options = new Particle.DustOptions(PURPLE, 1);
            AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity().getWorld().spawnEntity(loc, AREA_EFFECT_CLOUD, false);
            cloud.setColor(PURPLE);
            cloud.setParticle(DUST, options);
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
                        liv.damage(1, pro);
                    }
                    i++;
                }
            }.runTaskTimer(crystalized_essentials.getInstance(),1,1);

        }
    }

}
