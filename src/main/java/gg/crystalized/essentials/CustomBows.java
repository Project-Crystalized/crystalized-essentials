package gg.crystalized.essentials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.logging.Level;


public class CustomBows implements Listener {

    public HashMap<Projectile, ArrowData> arrows = new HashMap<>();
    @EventHandler
    public void onBowShot(EntityShootBowEvent event){
        if(event.isCancelled()){
            return;
        }
        LivingEntity e = event.getEntity();
        ItemStack stack = event.getBow();

        if(stack == null){
            return;
        }

        ItemMeta meta = stack.getItemMeta();

        if(meta == null){
            return;
        }

        int model = meta.getCustomModelData();
        ArrowData.bowType type = null;
        //note: model data of silver bow
        if(stack.getType() == Material.BOW && model == 1){
            type = ArrowData.bowType.marksman;
        }else if(stack.getType() == Material.BOW && model == 3){
            type = ArrowData.bowType.ricochet;
        }

        if(type == null){
            return;
        }

        ArrowData ard = new ArrowData(e, event.getForce(), event.getHand(), type,  event.getProjectile().getVelocity());
        arrows.put((Projectile) event.getProjectile(), ard);
        
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event){
        Projectile pro = event.getEntity();

        if(!(pro instanceof Arrow)){
            return;
        }

        Arrow ar = (Arrow) pro;
        ArrowData data = arrows.get(pro);

        if(data.type == ArrowData.bowType.marksman){
            LivingEntity e = (LivingEntity) event.getHitEntity();
            if(e == null){
                return;
            }
            Location shooterLoc = data.shooter.getLocation();
            Location hitLoc = e.getLocation();
            double distance = Math.floor(shooterLoc.distance(hitLoc)/10);
            data.shooter.sendMessage("damage: "+ distance*0.5);
            e.damage(distance * 0.5, pro);
        }else if(data.type == ArrowData.bowType.ricochet){
            BlockFace b = event.getHitBlockFace();
            if(b == null){
                Bukkit.getServer().getLogger().log(Level.SEVERE, "blockface is null returning...");
                return;
            }
            Vector v = ar.getLocation().getDirection();
            Vector vb = b.getDirection();
            float angel = v.angle(vb);
            data.shooter.sendMessage(v.toString());
            data.shooter.sendMessage("angle: " + angel);
        }
    }
}
