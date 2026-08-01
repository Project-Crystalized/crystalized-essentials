package gg.crystalized.essentials;

import com.destroystokyo.paper.ParticleBuilder;
import com.destroystokyo.paper.event.player.PlayerReadyArrowEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;

import static org.bukkit.Material.AIR;
import static org.bukkit.Material.CROSSBOW;

public class CustomBows implements Listener {
	//If anyone except me want to experiment with damage eddit damage here - Mish
	/*
	* Bow min damage is 1 is the least damage it can do when having zero charge
	* Bow max damge is when it is fully charged, now is 7hp 3.5 hearts
	* Crossbow has no charge time so is set to 8 (4 hearts)
	*
	* Extra info:
		* This is raw damage, things like arrmor and resistance will reduce it
		* So it functions as it should
	* */
	private static final double BOW_MIN_DAMAGE = 1.0;
	private static final double BOW_MAX_DAMAGE = 7.0;
	private static final double CROSSBOW_DAMAGE = 8.0;
	//Change the extra damage that the player gets here from the direct hit exploision
	//I had to change it as otherwise player wasn't getting any explosion damage - Mish
	private static final double EXPLOSION_DAMAGE_BONUCE = 3.5;
	//This is extra bonuce that applies to explosion when both explosibe bow and explosive arrow are used
	//So on top of 3.5 damage
	private static final double EXPLOSION_BOW_AND_ARROW_EXTRA_BONUCE = 1.5;
	//This is a value that is minused from the player's eye level location to detect headhsot
	//If headshots are too easy to hit it can be adjusted to 0.20
	private static final double ROUGH_HEAD_START_LOCATION = 0.25;
	public static HashMap<Projectile, ArrowData> arrows = new HashMap<>();

	//This method is used to calculate arrow's base damage, takes in the weapon and it's charge force
	private double calculateArrowDamage(ItemStack weapon, float weaponChargedForce){
		switch (weapon.getType()) {
			case BOW -> {
				//Clamps bow charge between 0 and 1 cleanly
				/*Simple explanations of clamp incase you never saw it - by Mish
					*It takes in the weapon charge value and clamps it between 0.0 and 1.0
					* Meaning that if for some reason it is higher than 1.0 it will return 1.0
					* Or if for some reason is is less than 0.0 it wil return 0.0
					* If it is between those values it leaves the value the same
				* This is just more of a safety check as charge should be between 0 and 1
					* */
				double charge = Math.clamp(weaponChargedForce, 0.0f, 1.0f);
				//This is a basic lerp formula:  min + (max - min) * t
				//It calculates the value between minimum and maximum.
				//In this case it will output the bows damage between the charges
					//Example of zero bow charge: 1 + (7 - 1) * 0 = 1.0
					//Example of half bow charge: 1 + (7 - 1) * 0.5 = 4.0
					//Example of full bow charge: 1 + (7 - 1) * 1 = 7.0
					//You can learn more here about lerp and this formula
						//https://gamedev.net/tutorials/programming/general-and-gameplay-programming/linear-interpolation-explained-r5892
				double baseDamage = BOW_MIN_DAMAGE + (BOW_MAX_DAMAGE - BOW_MIN_DAMAGE) * charge;

				//This is incase any bows in the future have power enchatment, so that it still works
				int powerLevel = weapon.getEnchantmentLevel(Enchantment.POWER);
				//The default multiplier is 1
				double powerMultiplier = 1.0;
				//TODO: If power is added and is too overpowered try another formula (Like 1 extra damge per power)
				if (powerLevel > 0) {
					//Vanila formula: Power increase arrow damage by 25% × (level + 1)
					//https://minecraft.fandom.com/wiki/Power
					powerMultiplier = powerMultiplier + ( 0.25 * (powerLevel + 1));
				}
				//The base damage is multiplied to power multiplier (If no power is just 1)
				return baseDamage * powerMultiplier;
			}

			//Crossbow has no charge force like the bow, so damage will allways be the same
			case CROSSBOW -> {
				return CROSSBOW_DAMAGE;
			}
			//If not bow or cross bow sets damage to zero
			default -> {
				return 0.0;
			}
		}

	}
	//This method calculates if the headhsot took place, and returns true if it did, and false if not
	//Used by weapons capable of dealing headshots
	private boolean isHeadshot(LivingEntity shotEntity, Entity arrow){
		//This calculates the height of the arrow hit.
		//Arrows location - players/entities location feet.
		double heightOfHit = arrow.getLocation().getY() - shotEntity.getLocation().getY();
		//This esnures damage is more consistant as it takes in the players eye height and makes it starts roughly
		//At the head, and if height of the hit is bigger or equal to it then headshot counts
		boolean headshot = (heightOfHit >= (shotEntity.getEyeHeight() - ROUGH_HEAD_START_LOCATION));
		//The check of locations that is printed to the console
		//Changed to logger as the server console was complaining, and it got annoying
		crystalized_essentials.getInstance()
				.getLogger().info("Arrow's y location " + arrow.getLocation().getY() + ", Feet of player y: " + shotEntity.getLocation().getY() +
						", Height of the hit: " + heightOfHit + ", Head shot start location: " + (shotEntity.getEyeHeight() - ROUGH_HEAD_START_LOCATION));
		//System.out.println("Arrow's y location " + arrow.getLocation().getY() + ", Feet of player y: " + shotEntity.getLocation().getY() +
		//		", Height of the hit: " + heightOfHit + ", Head shot start location: " + (shotEntity.getEyeHeight() - ROUGH_HEAD_START_LOCATION));
		return headshot;
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void onBowShot(EntityShootBowEvent event) {
		/* Was here before, moved to allow crosbow clearence, to prevent full crosbow while it is empty visual bug
		if (event.isCancelled()) {
			return;
		}*/
		//As the refund logic happens, the crosbow needs to be cleared anyway to prevent visual bug
		//TODO: check if it is is ok in crystal blitz
		ItemStack bow_item = event.getBow();
		if (bow_item != null && bow_item.getType().equals(CROSSBOW)) {
			bow_item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(0).build());
		}
		if (event.isCancelled()) {
			return;
		}


		if (bow_item == null) {
			return;
		}

		ArrowData.arrowType arrType = get_arrow_type(event.getConsumable());
		ArrowData.bowType bowType = get_bow_type(bow_item);
		if (arrType == ArrowData.arrowType.explosive || bowType == ArrowData.bowType.explosive) {
			((Player) event.getEntity()).setCooldown(bow_item, 20 * 3);
		}

		ArrowData.bowType type = get_bow_type(bow_item);
		switch (type) {
			case charged -> {
				event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(4));
				((Player) event.getEntity()).setCooldown(bow_item, 20 * 5);
				chargedParticleTrail((Projectile) event.getProjectile());
				event.getProjectile().setGravity(false);
			}
			case preciseCrossbow -> {
				//This is to make precise crossbow not be effected by gravity, matching it's description
				//Arrows will go in a straight line and not curve.
				event.getProjectile().setGravity(false);
			}
		}

		//TODO: add damage - DONE
		//Added damge here
		double damage = calculateArrowDamage(bow_item, event.getForce());
		ArrowData ard = new ArrowData(event.getEntity(), type, arrType, 0, damage);
		arrows.put((Projectile) event.getProjectile(), ard);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Projectile)) {
			return;
		}

		ArrowData data = arrows.get((Projectile) e.getDamager());
		if (data == null) {
			return;
		}
		//Calculates the default damage first - Mish.
		//Can be over written or added on by bow types below

		e.setDamage(data.damage);





		// deal extra damge for marksman
		//Marksman adjusted by Mish
		switch (data.type) {
			case marksman -> {
				Location shooterLoc = data.shooter.getLocation();
				Location hitLoc = e.getEntity().getLocation();
				double distance = Math.floor(shooterLoc.distance(hitLoc) / 10.0);
				//Was setting extra damage on top of default damage
					//((LivingEntity) e.getEntity()).damage(distance);
				//Now it sets the new default bow damage + distance
				e.setDamage(e.getDamage() + distance);
			}
			case charged -> {
				/*
				* I decided to make it so the event is not canceled and damage is just added on top for the headshot
				* With the body shot being default 8
				* And with the headhsot being 10*/

				LivingEntity shotEntity = (LivingEntity) e.getEntity();
				Entity arrow = e.getDamager();
				//Calls a method that caluclates the headshot based on height taking in the target entity and the arrow
				//Outputs true or false
				boolean headshot = isHeadshot(shotEntity, arrow);
				if (headshot) {
					//When true adds +2 to the damage
					e.setDamage(e.getDamage() + 2);
					crystalized_essentials.getInstance().getLogger()
							.info("headshot:" + e.getDamage() + " damage");
				} else {
					//The default damage of 8 for clarity it will be the same, feel free to reduce by - 2 to mimic the original 6 damage
					//e.setDamage(e.getDamage() - 2); uncoment to be 6 damage - Mish
					crystalized_essentials.getInstance().getLogger()
							.info("Not headshot:" + e.getDamage() + " normal damage");
				}
				arrow.getLocation().getWorld().playSound(e.getDamager().getLocation(),
						Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
				arrow.remove();
				/*The old implementation
				* I tested it - Mish
				* I think the headshot value is a little bit high, and I barely landed any when shooting in the head
				* Also no need to cancel the event, just add extra damage on top with the base being same as other crossbows
				* I left the old implementaion below
				*/
				/*
				e.setCancelled(true);
				Location eloc = e.getEntity().getLocation();
				Location arrloc = e.getDamager().getLocation();
				if (arrloc.getY() - eloc.getY() >= 1.7 && arrloc.getY() - eloc.getY() <= 2) {
					((LivingEntity) e.getEntity()).damage(10);
					//That is so that the player turn red when damaged
					((LivingEntity) e.getEntity()).playHurtAnimation(0.0F);
					System.out.println("Dealt 10 Damage");
				} else {
					((LivingEntity) e.getEntity()).damage(6);
					((LivingEntity) e.getEntity()).playHurtAnimation(0.0F);
					System.out.println("Dealt 6 Damage");
				}
				e.getDamager().getLocation().getWorld().playSound(e.getDamager().getLocation(),
						Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);
				e.getDamager().remove();
				*/
			}
			case preciseCrossbow -> {
				//Changed this to match the headshot logic developed. - Mish
				LivingEntity shotEntity = (LivingEntity) e.getEntity();
				Entity arrow = e.getDamager();
				boolean headshot = isHeadshot(shotEntity, arrow);
				if (headshot) {
					//Nerfed from 16 damage to 12 damage, as it is was pretty strong
					e.setDamage(e.getDamage() * 1.5);
                  	e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(1.2));
				}


				//Orignal implementation.
//				Location eloc = e.getEntity().getLocation();
//				Location arrloc = e.getDamager().getLocation();
//				if (arrloc.getY() - eloc.getY() >= 1.7 && arrloc.getY() - eloc.getY() <= 2) {
//					/*Why is this so op, it adds extra damage on top, so that is like 8 default
//					* + 16 extra damage. So total is 24 damage. I am nerfing to be 16
//					* Cause no way it is was intentional - Mish
//					* */
//					//OLD:
//						//((LivingEntity) e.getEntity()).damage(e.getDamage() * 2);
//
//					/*Now this gets the cross bow default damage and multiplies it by 2.
//					* */
//					e.setDamage(e.getDamage() * 2);
//                    e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(1.2));
//				}

			}
			case grapplingBow -> {
				LivingEntity p = data.shooter;
				Entity en = e.getEntity();

				double x = p.getX() - en.getX();
				double y = (p.getY() - en.getY()) + 0.5;
				double z = p.getZ() - en.getZ();
				e.getEntity().setVelocity(new Vector(x, y, z).normalize().multiply(1.9));
			}
		}
		//Had to move it here so that the bonuce from the explosion won't multiple in headshot
		//Don't know why but the player feels like he doesn't care about explosions anymore
		//Getting no damage from it, I tried everything but this seems like the best robust fix
		//So I made this kinda fix that makes the direct explosive damage consistant
		//Added extra UUID protection, incase on servers people with diffrent ping might experince a lot more damage
		//Or for any other reason the damage just starts applying.
		//In custom arrows
		boolean isExplosiveArrow = data.arrType == ArrowData.arrowType.explosive;
		boolean isExplosiveBow = data.type == ArrowData.bowType.explosive;

		if (isExplosiveArrow || isExplosiveBow) {

			double explosiveDamageBonus = EXPLOSION_DAMAGE_BONUCE;
			//Not sure if this bow is in the game but the explosion will be slightly stronger
			if (isExplosiveArrow && isExplosiveBow) {
				explosiveDamageBonus = explosiveDamageBonus + EXPLOSION_BOW_AND_ARROW_EXTRA_BONUCE;
			}

			e.setDamage(e.getDamage() + explosiveDamageBonus);
		}

		//Will remain visible for now, for testing with people later. - Mish
		if(!e.isCancelled()){
			//Check for marksman distance
			crystalized_essentials.getInstance().getLogger().info("Shot total damage:" + e.getDamage());
		}
	}
	//This makes sure that the explosion damage is zero for the player that got directly hit
	//As the direct hit damage is set manualy
	@EventHandler(priority = EventPriority.HIGH)
	public void onDirectExplosionDamage(EntityDamageByEntityEvent event) {
		//If it is not due to the explosion than nothing happens
		if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
			return;
		}
		//If it is not contained in the map than nothing happens
		if (!CustomArrows.isDirectExplosiveHit(event.getEntity().getUniqueId())) {
			return;
		}
		//Sets the explosion damage to zero to prevent potential double damage from explosion
		//It works without it on my end as damage from the explosion is not registering on direct hit
		//But here it makes sure that for everyone the direct explosion hit would be consistant, accross servers and pings.
		event.setDamage(0.0);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onArrowHit(ProjectileHitEvent event) {
		if (!(event.getEntity() instanceof Arrow || event.getEntity() instanceof SpectralArrow)) {
			return;
		}

		AbstractArrow ar = (AbstractArrow) event.getEntity();
		ArrowData data = arrows.get(event.getEntity());

		if (data == null) {
			return;
		}

		if (data.type == ArrowData.bowType.ricochet) {
			if (ar.isInBlock()) {
				return;
			}
			if (event.getHitEntity() != null && event.getHitEntity() instanceof Player) {
				//Makes sure that arrow custom effect still happens when rechochet bow hits - Mish
				CustomArrows.onArrowHit(event);
				return;
			}
			Location loc = event.getEntity().getLocation();
			Vector velocity = event.getEntity().getVelocity();
			loc.subtract(velocity.clone().multiply(0.1));
			velocity.multiply(0.9);

			if (data.timesBounced >= 3) {
				CustomArrows.onArrowHit(event);
				return;
			}
			event.setCancelled(true);
			BlockFace face = event.getHitBlockFace();
			if (face == BlockFace.UP || face == BlockFace.DOWN) {
				velocity.setY(-velocity.getY());
			} else if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
				velocity.setZ(-velocity.getZ());
			} else if (face == BlockFace.EAST || face == BlockFace.WEST) {
				velocity.setX(-velocity.getX());
			}

			data.timesBounced++;
			//Copying the item stack so that the arrows could stack in player inventory after pick up
			ItemStack theOriginalArrowItem = ar.getItemStack().clone();
			//FIXED: Fix the bug with spectular arrow causing an exception with rechashet bow
			//Using the abstract arrow
			AbstractArrow newArrow;
			if (event.getEntity() instanceof SpectralArrow) {
				//If it a spectral arrow than the abstract arrow is set to spectral arrow, everythin else is idnetical as before
				//Old arrow
					//Arrow arrow = event.getEntity().getWorld().spawnArrow(loc, velocity, (float) velocity.length(), 0);
				newArrow = event.getEntity().getWorld().spawnArrow(loc, velocity, (float) velocity.length(), 0, SpectralArrow.class);
			} else {
				newArrow = event.getEntity().getWorld().spawnArrow(loc, velocity, (float) velocity.length(), 0, Arrow.class);
			}
			//Sets the new arrow item stack to the original arrow item stack, so that it can stack in player inventory
			newArrow.setItemStack(theOriginalArrowItem);
			//Replaced with the new data.damage calculator so, 0.2 is not lost
				//arrow.setDamage(arrow.getDamage() + (0.2 * (float) data.timesBounced));
			data.damage += 0.2  * (float) data.timesBounced;
			newArrow.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
			newArrow.setShooter(event.getEntity().getShooter());
			arrows.remove(event.getEntity());
			event.getEntity().remove();
			arrows.put(newArrow, data);
			return;
		} else if (data.type == ArrowData.bowType.normalCrossbow) {
			//Commented out to ensure that new calculator calculates damage - Mish
				// I have no idea what I just wrote but I hope this works - Someone else
				//((AbstractArrow) event.getEntity()).setDamage(((AbstractArrow) event.getEntity()).getDamage() - 1);
			//It is set to 8 by Default so no need to add anything here - Mish
		}
		CustomArrows.onArrowHit(event);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCrossbowLoad(EntityLoadCrossbowEvent e) {

		//I added this check as if you try to load cross bow between games in LS the whole console is going to be full of exceptions - Mish
		if (e.isCancelled()) {
			return;
		}
		Entity entity = e.getEntity();
		ItemStack item = e.getCrossbow();

		//This needs to be delayed, otherwise the getChargedProjectiles list will be empty
		new BukkitRunnable() {
			public void run() {
				CrossbowMeta itemMeta = (CrossbowMeta) item.getItemMeta();
				ItemStack arrow = itemMeta.getChargedProjectiles().getFirst();

				ArrowData.arrowType arrType = get_arrow_type(arrow);
				if (!e.isCancelled() && item.getType().equals(CROSSBOW)) {
					item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(arrType.cmd).build());
				}
				cancel();
			}
		}.runTaskTimer(crystalized_essentials.getInstance(), 1, 1);
	}

	public ArrowData.arrowType get_arrow_type(ItemStack item) {
		ItemMeta arrowMeta = item.getItemMeta();
		if (arrowMeta == null) {
			return ArrowData.arrowType.normal;
		}
		if (arrowMeta.hasItemModel() && arrowMeta.getItemModel().getNamespace().equals("crystalized")) {
			switch (arrowMeta.getItemModel().getKey()) {
				case "wind_arrow" -> {
					return ArrowData.arrowType.wind;
				}
				case "explosive_arrow" -> {
					return ArrowData.arrowType.explosive;
				}
				case "dragon_arrow" -> {
					return ArrowData.arrowType.dragon;
				}
			}
		} else {
			if (item.getType() == Material.SPECTRAL_ARROW) {
				return ArrowData.arrowType.spectral;
			}
		}
		return ArrowData.arrowType.normal;
	}

	public ArrowData.bowType get_bow_type(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null || !meta.hasItemModel()) {
			if (item.getType().equals(Material.CROSSBOW)) {
				return ArrowData.bowType.normalCrossbow;
			} else {
				return ArrowData.bowType.normal;
			}
		} else if (item.getType() == Material.BOW
				&& meta.getItemModel().equals(new NamespacedKey("crystalized", "marksman_bow"))) {
			return ArrowData.bowType.marksman;
		} else if (item.getType() == Material.BOW
				&& meta.getItemModel().equals(new NamespacedKey("crystalized", "ricochet_bow"))) {
			return ArrowData.bowType.ricochet;
		} else if (item.getType() == Material.CROSSBOW
				&& meta.getItemModel().equals(new NamespacedKey("crystalized", "charged_crossbow"))) {
			return ArrowData.bowType.charged;
		} else if (item.getType() == Material.BOW
				&& meta.getItemModel().equals(new NamespacedKey("crystalized", "explosive_bow"))) {
			return ArrowData.bowType.explosive;
		} else if (item.getType() == Material.BOW
				&& meta.getItemModel().equals(new NamespacedKey("crystalized", "grappling_bow"))) {
			return ArrowData.bowType.grapplingBow;
		} else if (item.getType() == Material.CROSSBOW
				&& meta.getItemModel().equals(new NamespacedKey("crystalized", "precise_crossbow"))) {
			return ArrowData.bowType.preciseCrossbow;
		}
		else {
			if (item.getType().equals(Material.CROSSBOW)) {
				return ArrowData.bowType.normalCrossbow;
			} else {
				return ArrowData.bowType.normal;
			}
		}
	}

	@EventHandler
	public void onArrowPickup(PlayerPickupArrowEvent event) {
		ItemMeta meta = event.getArrow().getItemStack().getItemMeta();
		if (meta != null && meta.hasItemModel() && meta.getItemModel().getNamespace().equals("crystalized")) {
			switch (meta.getItemModel().getKey()) {
				case "explosive_arrow", "wind_arrow" -> {
					event.setCancelled(true);
				}
			}
		}
	}

	public void chargedParticleTrail(Projectile pro) {
		LivingEntity shooter = (LivingEntity) pro.getShooter();
		if (shooter == null) {
			return;
		}
		Location loc = pro.getLocation();
		Vector v = pro.getVelocity().normalize();
		double t = 0;
		Material material = loc.getBlock().getType();

		ParticleBuilder builder = new ParticleBuilder(Particle.SOUL_FIRE_FLAME);
		builder.count(5);
		builder.offset(0, 0, 0);
		builder.extra(0);

		Collection<LivingEntity> collect = loc.getNearbyLivingEntities(1);
		while ((collect.isEmpty() || (collect.size() == 1 && collect.contains(shooter))) && material == AIR && t <= 10) {
			builder.location(loc);
			builder.spawn();
			loc = new Location(loc.getWorld(), lineEquation(loc.getX(), t, v.getX()), lineEquation(loc.getY(), t, v.getY()),
					lineEquation(loc.getZ(), t, v.getZ()));
			t = t + 0.1;
		}
	}

	public static double lineEquation(double g, double t, double v) {
		return g + (t * v);
	}
}
