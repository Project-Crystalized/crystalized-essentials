package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

import static org.bukkit.Color.PURPLE;
import static org.bukkit.Particle.DUST;
import static org.bukkit.Particle.RAID_OMEN;
import static org.bukkit.damage.DamageType.*;
import static org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED;
import static org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD;
import static org.bukkit.potion.PotionEffectType.GLOWING;


public class CustomArrows{


    public static void onArrowHit(ProjectileHitEvent event){
        if(event.isCancelled()){
            return;
        }
				// TODO does this crash with non custom arrow projectiles?
        ArrowData data = CustomBows.arrows.get(event.getEntity());
        Projectile pro = event.getEntity();
        Arrow arrow;
        Location loc = event.getEntity().getLocation();
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
            Arrow arr = (Arrow)event.getEntity();
            arr.setDamage(1);

            ItemStack item = arr.getItemStack();
            item.setItemMeta(null);
            arr.setItemStack(item);

            Particle.DustOptions options = new Particle.DustOptions(PURPLE, 1);
            AreaEffectCloud cloud = (AreaEffectCloud) event.getEntity().getWorld().spawnEntity(loc, AREA_EFFECT_CLOUD, false);
            cloud.setColor(PURPLE);
            cloud.setParticle(DUST, options);

            DamageSource.Builder builder = DamageSource.builder(DRAGON_BREATH);
            builder.withCausingEntity(data.shooter);
            builder.withDirectEntity(cloud);
            builder.withDamageLocation(loc);
            DamageSource source = builder.build();
            new BukkitRunnable(){
                int i = 0;
                final Location loc = event.getEntity().getLocation();
                public void run(){
                    if(i >= 10){
                        cloud.remove();
                        cancel();
                    }
                    Collection<LivingEntity> collect = loc.getNearbyLivingEntities(2, 1);
                    for(LivingEntity liv : collect){
                        liv.damage(1, source);
                    }
                    i++;
                }
            }.runTaskTimer(crystalized_essentials.getInstance(),1,15);

        } else if(data.arrType == ArrowData.arrowType.explosive){
            arrow.setPickupStatus(DISALLOWED);

            DamageSource.Builder builder = DamageSource.builder(EXPLOSION);
            builder.withCausingEntity(data.shooter);
            builder.withDirectEntity(arrow);
            builder.withDamageLocation(arrow.getLocation());
            DamageSource source = builder.build();

						// if a entity was hit, explode instantly, and dont do creeper sound
						Entity hit_player = event.getHitEntity();
            if(hit_player != null) {
							exploArrowExplosion(loc, source);
							loc.getWorld().playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.AMBIENT, 1, 1));
							arrow.remove();
							return;
						}
						arrow.setGlowing(true);

            new BukkitRunnable() {
                int i = 0;
                public void run() {
                    if(i >= 3){
                        cancel();
												return;
                    }
										loc.getWorld().spawnParticle(RAID_OMEN, loc, 3);
                    loc.getWorld().playSound(Sound.sound(Key.key("entity.creeper.primed"), Sound.Source.AMBIENT, 2, 1));
                    i++;
                }
            }.runTaskTimer(crystalized_essentials.getInstance(), 0, 20);

            new BukkitRunnable() {
                public void run() {
									exploArrowExplosion(loc, source);
									loc.getWorld().playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.AMBIENT, 1, 1));
									arrow.remove();
                }
            }.runTaskLater(crystalized_essentials.getInstance(), 3*20);
        }
    }

	private static void exploArrowExplosion(Location explo_loc, DamageSource source) {
		Collection<LivingEntity> nearby = explo_loc.getNearbyLivingEntities(2);
		Collection<LivingEntity> notSoNearby = explo_loc.getNearbyLivingEntities(4);

		notSoNearby.removeAll(nearby);

		explo_loc.createExplosion(source.getCausingEntity(), (float) 2.3, false, false);

		ParticleBuilder builder = new ParticleBuilder(DUST);
		builder.color(Color.RED);
		builder.offset(1, 1, 1);
		builder.count(300);
		builder.location(explo_loc);
		builder.spawn();
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
